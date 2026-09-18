/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index;

import com.liferay.mcp.server.rest.internal.search.index.constants.MCPToolFields;
import com.liferay.mcp.server.rest.internal.util.OpenAPIUtil;
import com.liferay.mcp.server.rest.internal.util.ToolSetUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.DigesterUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.TermsAggregation;
import com.liferay.portal.search.aggregation.bucket.TermsAggregationResult;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.DocumentBuilder;
import com.liferay.portal.search.document.DocumentBuilderFactory;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.DeleteByQueryDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.QueriesUtil;
import com.liferay.portal.search.query.TermsQuery;
import com.liferay.portal.vulcan.application.HeadlessApplicationProvider;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = MCPToolIndexWriter.class)
public class MCPToolIndexWriter {

	public void deleteIndex(long companyId) {
		_mcpToolIndexCreator.deleteIfExists(companyId);

		_mcpToolIndexStates.remove(companyId);
	}

	public void invalidate(long companyId) {
		MCPToolIndexState mcpToolIndexState = _getMCPToolIndexState(companyId);

		mcpToolIndexState.markAllToolSetsStale();
	}

	public void invalidate(long companyId, String toolSetName) {
		if (Validator.isBlank(toolSetName)) {
			invalidate(companyId);

			return;
		}

		MCPToolIndexState mcpToolIndexState = _getMCPToolIndexState(companyId);

		mcpToolIndexState.markToolSetStale(toolSetName);
	}

	public void updateIfStale(
		long companyId, HttpServletRequest httpServletRequest) {

		MCPToolIndexState mcpToolIndexState = _getMCPToolIndexState(companyId);

		Map<String, HeadlessApplicationProvider.OpenAPIDocument>
			openAPIDocuments = ToolSetUtil.getOpenAPIDocuments();

		Set<String> toolSetNames = _getToolSetNames(openAPIDocuments);

		if (!_isIndexStale(mcpToolIndexState, toolSetNames)) {
			return;
		}

		synchronized (mcpToolIndexState) {

			// Ensure the previous thread did not update

			if (!_isIndexStale(mcpToolIndexState, toolSetNames)) {
				return;
			}

			_update(
				companyId, httpServletRequest, mcpToolIndexState,
				openAPIDocuments, toolSetNames);
		}
	}

	private void _deleteMCPToolDocuments(
		String indexName, Set<String> removedToolSetNames) {

		if (removedToolSetNames.isEmpty()) {
			return;
		}

		TermsQuery termsQuery = QueriesUtil.terms(MCPToolFields.TOOL_SET_NAME);

		for (String removedToolSetName : removedToolSetNames) {
			termsQuery.addValue(removedToolSetName);
		}

		DeleteByQueryDocumentRequest deleteByQueryDocumentRequest =
			new DeleteByQueryDocumentRequest(termsQuery, indexName);

		deleteByQueryDocumentRequest.setRefresh(true);

		_searchEngineAdapter.execute(deleteByQueryDocumentRequest);
	}

	private void _deleteOutdatedMCPToolDocuments(
		Map<String, String> changedToolSetHashes,
		Set<String> changedToolSetNames, String indexName) {

		if (changedToolSetNames.isEmpty()) {
			return;
		}

		TermsQuery toolSetNamesTermsQuery = QueriesUtil.terms(
			MCPToolFields.TOOL_SET_NAME);

		for (String changedToolSetName : changedToolSetNames) {
			toolSetNamesTermsQuery.addValue(changedToolSetName);
		}

		BooleanQuery booleanQuery = QueriesUtil.booleanQuery();

		booleanQuery.addMustQueryClauses(toolSetNamesTermsQuery);

		if (!changedToolSetHashes.isEmpty()) {
			TermsQuery toolSetHashesTermsQuery = QueriesUtil.terms(
				MCPToolFields.TOOL_SET_HASH);

			for (String changedToolSetHash : changedToolSetHashes.values()) {
				toolSetHashesTermsQuery.addValue(changedToolSetHash);
			}

			booleanQuery.addMustNotQueryClauses(toolSetHashesTermsQuery);
		}

		DeleteByQueryDocumentRequest deleteByQueryDocumentRequest =
			new DeleteByQueryDocumentRequest(booleanQuery, indexName);

		deleteByQueryDocumentRequest.setRefresh(true);

		_searchEngineAdapter.execute(deleteByQueryDocumentRequest);
	}

	private Set<String> _getChangedToolSetNames(
		Set<String> indexedToolSetNames, Map<String, Object> staleToolSetTokens,
		Set<String> toolSetNames) {

		Set<String> changedToolSetNames = new HashSet<>(toolSetNames);

		if (staleToolSetTokens.containsKey(StringPool.STAR)) {
			return changedToolSetNames;
		}

		changedToolSetNames.removeAll(indexedToolSetNames);

		for (String staleToolSetName : staleToolSetTokens.keySet()) {
			if (toolSetNames.contains(staleToolSetName)) {
				changedToolSetNames.add(staleToolSetName);
			}
		}

		return changedToolSetNames;
	}

	private String _getCollectionSegment(MCPTool mcpTool) {
		if (Validator.isNotNull(mcpTool.getOperationVariant()) ||
			!Objects.equals(mcpTool.getMethod(), "get") ||
			!StringUtil.endsWith(mcpTool.getToolName(), "Page")) {

			return null;
		}

		String[] segments = StringUtil.split(mcpTool.getPath(), CharPool.SLASH);

		if (segments.length == 0) {
			return null;
		}

		String segment = segments[segments.length - 1];

		if (OpenAPIUtil.isPathParameter(segment)) {
			return null;
		}

		return segment;
	}

	private Document _getDocument(
		MCPTool mcpTool, String toolSetHash, int toolSetSize) {

		DocumentBuilder documentBuilder = DocumentBuilderFactory.builder();

		String collectionSegment = _getCollectionSegment(mcpTool);

		if (collectionSegment != null) {
			documentBuilder.setString(
				MCPToolFields.COLLECTION_SEGMENT, collectionSegment);
		}

		documentBuilder.setValue(
			MCPToolFields.DEPRECATED, mcpTool.isDeprecated());
		documentBuilder.setString(
			MCPToolFields.DESCRIPTION, mcpTool.getDescription());
		documentBuilder.setString(
			MCPToolFields.ENTITY_NAME, mcpTool.getEntityName());
		documentBuilder.setStrings(
			MCPToolFields.EXPANSION, mcpTool.getExpansions());
		documentBuilder.setString(
			MCPToolFields.IDENTIFIER_TYPE, mcpTool.getIdentifierType());
		documentBuilder.setString(MCPToolFields.INTENT, mcpTool.getIntent());
		documentBuilder.setString(MCPToolFields.METHOD, mcpTool.getMethod());
		documentBuilder.setString(
			MCPToolFields.OPERATION_VARIANT, mcpTool.getOperationVariant());
		documentBuilder.setStrings(
			MCPToolFields.PARAMETERS, mcpTool.getParameters());

		String path = mcpTool.getPath();

		documentBuilder.setString(MCPToolFields.PATH, path);
		documentBuilder.setValue(MCPToolFields.PATH_LENGTH, path.length());
		documentBuilder.setValue(
			MCPToolFields.PATH_PARAMETER_COUNT,
			StringUtil.count(path, CharPool.OPEN_CURLY_BRACE));
		documentBuilder.setValue(
			MCPToolFields.PATH_SEGMENTS_COUNT,
			StringUtil.count(path, CharPool.SLASH));

		documentBuilder.setStrings(
			MCPToolFields.REQUIRED_REFERENCES, mcpTool.getRequiredReferences());
		documentBuilder.setStrings(
			MCPToolFields.SCHEMA_PROPERTIES, mcpTool.getSchemaProperties());
		documentBuilder.setString(
			MCPToolFields.TOOL_NAME, mcpTool.getToolName());
		documentBuilder.setString(MCPToolFields.TOOL_SET_HASH, toolSetHash);
		documentBuilder.setString(
			MCPToolFields.TOOL_SET_NAME, mcpTool.getToolSetName());
		documentBuilder.setValue(MCPToolFields.TOOL_SET_SIZE, toolSetSize);
		documentBuilder.setString(MCPToolFields.UID, mcpTool.getUID());

		return documentBuilder.build();
	}

	private Set<String> _getIndexedToolSetNames(long companyId) {
		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		TermsAggregation termsAggregation = _aggregations.terms(
			MCPToolFields.TOOL_SET_NAME, MCPToolFields.TOOL_SET_NAME);

		termsAggregation.setSize(_INDEXED_TOOL_SET_NAMES_MAX);

		searchSearchRequest.addAggregation(termsAggregation);

		searchSearchRequest.setIndexNames(
			_mcpToolIndexCreator.getIndexName(companyId));
		searchSearchRequest.setQuery(QueriesUtil.matchAll());
		searchSearchRequest.setSize(0);

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		Map<String, AggregationResult> aggregationResultsMap =
			searchSearchResponse.getAggregationResultsMap();

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)aggregationResultsMap.get(
				MCPToolFields.TOOL_SET_NAME);

		Set<String> indexedToolSetNames = new HashSet<>();

		for (Bucket bucket : termsAggregationResult.getBuckets()) {
			indexedToolSetNames.add(bucket.getKey());
		}

		return indexedToolSetNames;
	}

	private MCPToolIndexState _getMCPToolIndexState(long companyId) {
		return _mcpToolIndexStates.computeIfAbsent(
			companyId, key -> new MCPToolIndexState());
	}

	private Map<String, String> _getToolSetHashes(
		Collection<MCPTool> mcpTools) {

		Map<String, String> toolSetHashes = new HashMap<>();

		Map<String, Set<String>> toolSetUIDsMap = new HashMap<>();

		for (MCPTool mcpTool : mcpTools) {
			Set<String> toolSetUIDs = toolSetUIDsMap.computeIfAbsent(
				mcpTool.getToolSetName(), key -> new TreeSet<>());

			toolSetUIDs.add(mcpTool.getUID());
		}

		for (Map.Entry<String, Set<String>> entry : toolSetUIDsMap.entrySet()) {
			String toolSetUIDsString = StringUtil.merge(
				entry.getValue(), StringPool.POUND);

			toolSetHashes.put(
				entry.getKey(), DigesterUtil.digest(toolSetUIDsString));
		}

		return toolSetHashes;
	}

	private Set<String> _getToolSetNames(
		Map<String, HeadlessApplicationProvider.OpenAPIDocument>
			openAPIDocuments) {

		Set<String> toolSetNames = new HashSet<>(openAPIDocuments.keySet());

		toolSetNames.remove("openapi");

		return toolSetNames;
	}

	private Map<String, Integer> _getToolSetSizes(
		Collection<MCPTool> mcpTools) {

		Map<String, Integer> toolSetSizes = new HashMap<>();

		for (MCPTool mcpTool : mcpTools) {
			int toolSetSize = GetterUtil.getInteger(
				toolSetSizes.get(mcpTool.getToolSetName()));

			toolSetSizes.put(mcpTool.getToolSetName(), toolSetSize + 1);
		}

		return toolSetSizes;
	}

	private void _indexMCPTools(
		String indexName, Collection<MCPTool> mcpTools,
		Map<String, String> toolSetHashes) {

		if (mcpTools.isEmpty()) {
			return;
		}

		Map<String, Integer> toolSetSizes = _getToolSetSizes(mcpTools);

		BulkDocumentRequest bulkDocumentRequest = new BulkDocumentRequest();

		for (MCPTool mcpTool : mcpTools) {
			bulkDocumentRequest.addBulkableDocumentRequest(
				new IndexDocumentRequest(
					indexName, mcpTool.getUID(),
					_getDocument(
						mcpTool, toolSetHashes.get(mcpTool.getToolSetName()),
						GetterUtil.getInteger(
							toolSetSizes.get(mcpTool.getToolSetName())))));
		}

		// The search triggering the index needs to see the results immediately

		bulkDocumentRequest.setRefresh(true);

		_searchEngineAdapter.execute(bulkDocumentRequest);
	}

	private boolean _isIndexStale(
		MCPToolIndexState mcpToolIndexState, Set<String> toolSetNames) {

		if (!Objects.equals(
				mcpToolIndexState.getIndexedToolSetNames(), toolSetNames)) {

			return true;
		}

		return mcpToolIndexState.hasStaleToolSets();
	}

	private void _update(
		long companyId, HttpServletRequest httpServletRequest,
		MCPToolIndexState mcpToolIndexState,
		Map<String, HeadlessApplicationProvider.OpenAPIDocument>
			openAPIDocuments,
		Set<String> toolSetNames) {

		Set<String> indexedToolSetNames = new HashSet<>();

		if (_mcpToolIndexCreator.indexExists(companyId)) {
			indexedToolSetNames = mcpToolIndexState.getIndexedToolSetNames();

			// A node that just joined the cluster, takes the tool sets the
			// index already holds instead of rebuilding every one of them

			if (indexedToolSetNames.isEmpty()) {
				indexedToolSetNames = _getIndexedToolSetNames(companyId);
			}
		}

		_mcpToolIndexCreator.createIfNotExists(companyId);

		Map<String, Object> staleToolSetTokens =
			mcpToolIndexState.getStaleToolSetTokens();

		Set<String> changedToolSetNames = _getChangedToolSetNames(
			indexedToolSetNames, staleToolSetTokens, toolSetNames);

		Set<String> failedToolSetNames = new HashSet<>();

		List<MCPTool> mcpTools = MCPToolFactoryUtil.getMCPTools(
			failedToolSetNames, httpServletRequest, openAPIDocuments,
			changedToolSetNames);

		changedToolSetNames.removeAll(failedToolSetNames);

		_updateIndex(
			changedToolSetNames, companyId, indexedToolSetNames, mcpTools,
			toolSetNames);

		_updateIndexState(
			failedToolSetNames, mcpToolIndexState, staleToolSetTokens,
			toolSetNames);
	}

	private void _updateIndex(
		Set<String> changedToolSetNames, long companyId,
		Set<String> indexedToolSetNames, List<MCPTool> mcpTools,
		Set<String> toolSetNames) {

		String indexName = _mcpToolIndexCreator.getIndexName(companyId);

		Map<String, String> changedToolSetHashes = _getToolSetHashes(mcpTools);

		_indexMCPTools(indexName, mcpTools, changedToolSetHashes);

		_deleteOutdatedMCPToolDocuments(
			changedToolSetHashes, changedToolSetNames, indexName);

		Set<String> removedToolSetNames = new HashSet<>(indexedToolSetNames);

		removedToolSetNames.removeAll(toolSetNames);

		_deleteMCPToolDocuments(indexName, removedToolSetNames);
	}

	private void _updateIndexState(
		Set<String> failedToolSetNames, MCPToolIndexState mcpToolIndexState,
		Map<String, Object> staleToolSetTokens, Set<String> toolSetNames) {

		mcpToolIndexState.setIndexedToolSetNames(toolSetNames);

		for (Map.Entry<String, Object> entry : staleToolSetTokens.entrySet()) {
			if (!failedToolSetNames.contains(entry.getKey())) {
				mcpToolIndexState.clearStaleToolSet(
					entry.getKey(), entry.getValue());
			}
		}

		for (String failedToolSetName : failedToolSetNames) {
			mcpToolIndexState.markToolSetFailed(failedToolSetName);
		}
	}

	private static final long _FAILED_TOOL_SET_RETRY_INTERVAL = Time.MINUTE;

	private static final int _INDEXED_TOOL_SET_NAMES_MAX = 1000;

	@Reference
	private Aggregations _aggregations;

	@Reference
	private MCPToolIndexCreator _mcpToolIndexCreator;

	private final Map<Long, MCPToolIndexState> _mcpToolIndexStates =
		new ConcurrentHashMap<>();

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

	private static class MCPToolIndexState {

		public void clearStaleToolSet(String toolSetName, Object token) {
			_staleToolSetTokens.remove(toolSetName, token);
		}

		public Set<String> getIndexedToolSetNames() {
			return new HashSet<>(_indexedToolSetNames);
		}

		public Map<String, Object> getStaleToolSetTokens() {
			Map<String, Object> staleToolSetTokens = new HashMap<>();

			long time = System.currentTimeMillis();

			for (Map.Entry<String, Object> entry :
					_staleToolSetTokens.entrySet()) {

				Long retryTime = _failedToolSetRetryTimes.get(entry.getKey());

				if ((retryTime == null) || (retryTime <= time)) {
					staleToolSetTokens.put(entry.getKey(), entry.getValue());
				}
			}

			return staleToolSetTokens;
		}

		public boolean hasStaleToolSets() {
			Map<String, Object> staleToolSetTokens = getStaleToolSetTokens();

			return !staleToolSetTokens.isEmpty();
		}

		public void markAllToolSetsStale() {
			_failedToolSetRetryTimes.clear();

			_staleToolSetTokens.put(StringPool.STAR, new Object());
		}

		public void markToolSetFailed(String toolSetName) {
			_failedToolSetRetryTimes.put(
				toolSetName,
				System.currentTimeMillis() + _FAILED_TOOL_SET_RETRY_INTERVAL);

			_staleToolSetTokens.putIfAbsent(toolSetName, new Object());
		}

		public void markToolSetStale(String toolSetName) {
			_failedToolSetRetryTimes.remove(toolSetName);

			_staleToolSetTokens.put(toolSetName, new Object());
		}

		public void setIndexedToolSetNames(Set<String> toolSetNames) {
			_indexedToolSetNames.clear();
			_indexedToolSetNames.addAll(toolSetNames);
		}

		private final Map<String, Long> _failedToolSetRetryTimes =
			new ConcurrentHashMap<>();
		private final Set<String> _indexedToolSetNames =
			ConcurrentHashMap.newKeySet();
		private final Map<String, Object> _staleToolSetTokens =
			new ConcurrentHashMap<>();

	}

}