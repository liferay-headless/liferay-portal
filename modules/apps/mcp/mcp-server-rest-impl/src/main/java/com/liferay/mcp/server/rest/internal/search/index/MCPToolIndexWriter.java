/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index;

import com.liferay.mcp.server.rest.internal.constants.MCPToolConstants;
import com.liferay.mcp.server.rest.internal.search.constants.MCPToolFields;
import com.liferay.mcp.server.rest.internal.search.index.util.MCPTool;
import com.liferay.mcp.server.rest.internal.search.index.util.MCPToolFactoryUtil;
import com.liferay.mcp.server.rest.internal.search.index.util.ResolverUtil;
import com.liferay.mcp.server.rest.internal.util.OpenAPIBriefUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.DocumentBuilder;
import com.liferay.portal.search.document.DocumentBuilderFactory;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.DeleteByQueryDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.QueriesUtil;
import com.liferay.portal.search.query.TermsQuery;

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

	public void invalidate(long companyId) {
		MCPToolIndexState mcpToolIndexState = _getMCPToolIndexState(companyId);

		mcpToolIndexState.setAllToolSetsStale();
	}

	public void invalidate(long companyId, String toolSetName) {
		if (Validator.isBlank(toolSetName)) {
			invalidate(companyId);

			return;
		}

		MCPToolIndexState mcpToolIndexState = _getMCPToolIndexState(companyId);

		mcpToolIndexState.setToolSetStale(toolSetName);
	}

	public void removeIndexState(long companyId) {
		_companyIndexStates.remove(companyId);
	}

	public void updateIfStale(
		long companyId, HttpServletRequest httpServletRequest) {

		MCPToolIndexState mcpToolIndexState = _getMCPToolIndexState(companyId);

		if (!_isIndexStale(companyId, mcpToolIndexState)) {
			return;
		}

		synchronized (mcpToolIndexState) {

			// Ensure the previous thread didn't already do the update

			if (!_isIndexStale(companyId, mcpToolIndexState)) {
				return;
			}

			_update(companyId, mcpToolIndexState, httpServletRequest);
		}
	}

	private Document _buildDocument(
		MCPTool mcpTool, Map<String, String> toolSetHashes,
		Map<String, Integer> toolSetSizes) {

		String path = mcpTool.getPath();

		DocumentBuilder documentBuilder = DocumentBuilderFactory.builder();

		documentBuilder.setValue(
			MCPToolFields.DEPRECATED, mcpTool.isDeprecated());
		documentBuilder.setString(
			MCPToolFields.DESCRIPTION, mcpTool.getDescription());
		documentBuilder.setString(
			MCPToolFields.ENTITY_NAME, mcpTool.getEntityName());
		documentBuilder.setStrings(
			MCPToolFields.EXPANSION, mcpTool.getExpansions());
		documentBuilder.setString(
			MCPToolFields.IDENTIFIER, mcpTool.getIdentifier());
		documentBuilder.setString(MCPToolFields.INTENT, mcpTool.getIntent());
		documentBuilder.setString(MCPToolFields.METHOD, mcpTool.getMethod());
		documentBuilder.setString(
			MCPToolFields.MODIFIER, mcpTool.getModifier());
		documentBuilder.setStrings(
			MCPToolFields.PARAMETERS, mcpTool.getParameters());
		documentBuilder.setString(MCPToolFields.PATH, path);
		documentBuilder.setValue(
			MCPToolFields.PATH_PARAMETER_COUNT,
			StringUtil.count(path, CharPool.OPEN_CURLY_BRACE));
		documentBuilder.setValue(
			MCPToolFields.PATH_SEGMENTS_COUNT,
			StringUtil.count(path, CharPool.SLASH));
		documentBuilder.setValue(MCPToolFields.PATH_LENGTH, path.length());
		documentBuilder.setStrings(
			MCPToolFields.REQUIRED_REFERENCES, mcpTool.getRequiredReferences());

		String resolverSegment = ResolverUtil.getResolverSegment(mcpTool);

		if (resolverSegment != null) {
			documentBuilder.setString(
				MCPToolFields.RESOLVER_SEGMENT, resolverSegment);
		}

		documentBuilder.setStrings(
			MCPToolFields.SCHEMA_PROPERTIES, mcpTool.getSchemaProperties());
		documentBuilder.setString(
			MCPToolFields.TOOL_NAME, mcpTool.getToolName());
		documentBuilder.setString(
			MCPToolFields.TOOL_SET_HASH,
			toolSetHashes.get(mcpTool.getToolSetName()));
		documentBuilder.setString(
			MCPToolFields.TOOL_SET_NAME, mcpTool.getToolSetName());
		documentBuilder.setValue(
			MCPToolFields.TOOL_SET_SIZE,
			GetterUtil.getInteger(toolSetSizes.get(mcpTool.getToolSetName())));
		documentBuilder.setString(MCPToolFields.UID, _getUID(mcpTool));

		return documentBuilder.build();
	}

	private void _deleteReplacedToolSets(
		String indexName, Map<String, String> toolSetHashes,
		Set<String> toolSetNames) {

		if (toolSetNames.isEmpty()) {
			return;
		}

		TermsQuery toolSetNamesTermsQuery = QueriesUtil.terms(
			MCPToolFields.TOOL_SET_NAME);

		for (String toolSetName : toolSetNames) {
			toolSetNamesTermsQuery.addValue(toolSetName);
		}

		BooleanQuery booleanQuery = QueriesUtil.booleanQuery();

		booleanQuery.addMustQueryClauses(toolSetNamesTermsQuery);

		if (!toolSetHashes.isEmpty()) {
			TermsQuery toolSetHashesTermsQuery = QueriesUtil.terms(
				MCPToolFields.TOOL_SET_HASH);

			for (String toolSetHash : toolSetHashes.values()) {
				toolSetHashesTermsQuery.addValue(toolSetHash);
			}

			booleanQuery.addMustNotQueryClauses(toolSetHashesTermsQuery);
		}

		_searchEngineAdapter.execute(
			new DeleteByQueryDocumentRequest(booleanQuery, indexName));
	}

	private void _deleteToolSets(String indexName, Set<String> toolSetNames) {
		if (toolSetNames.isEmpty()) {
			return;
		}

		TermsQuery termsQuery = QueriesUtil.terms(MCPToolFields.TOOL_SET_NAME);

		for (String toolSetName : toolSetNames) {
			termsQuery.addValue(toolSetName);
		}

		DeleteByQueryDocumentRequest deleteByQueryDocumentRequest =
			new DeleteByQueryDocumentRequest(termsQuery, indexName);

		_searchEngineAdapter.execute(deleteByQueryDocumentRequest);
	}

	private Set<String> _getChangedToolSetNames(
		Set<String> indexedToolSetNames, MCPToolIndexState mcpToolIndexState,
		Map<String, Object> staleToolSets, Set<String> toolSetNames) {

		if (mcpToolIndexState.isAllToolSetsStale()) {
			return new HashSet<>(toolSetNames);
		}

		Set<String> changedToolSetNames = new HashSet<>(toolSetNames);

		changedToolSetNames.removeAll(indexedToolSetNames);

		Set<String> staleToolSetNames = new HashSet<>(staleToolSets.keySet());

		staleToolSetNames.retainAll(toolSetNames);

		changedToolSetNames.addAll(staleToolSetNames);

		return changedToolSetNames;
	}

	private MCPToolIndexState _getMCPToolIndexState(long companyId) {
		return _companyIndexStates.computeIfAbsent(
			companyId, key -> new MCPToolIndexState());
	}

	private Map<String, String> _getToolSetHashes(
		Collection<MCPTool> mcpTools) {

		Map<String, String> toolSetHashes = new HashMap<>();

		Map<String, Set<String>> uids = new HashMap<>();

		for (MCPTool mcpTool : mcpTools) {
			Set<String> toolSetUIDs = uids.computeIfAbsent(
				mcpTool.getToolSetName(), key -> new TreeSet<>());

			toolSetUIDs.add(_getUID(mcpTool));
		}

		for (Map.Entry<String, Set<String>> entry : uids.entrySet()) {
			Set<String> toolSetUIDs = entry.getValue();

			String seed = StringUtil.merge(toolSetUIDs, StringPool.POUND);

			toolSetHashes.put(entry.getKey(), String.valueOf(seed.hashCode()));
		}

		return toolSetHashes;
	}

	private Set<String> _getToolSetNames(long companyId) {
		Set<String> toolSetNames = new HashSet<>(
			OpenAPIBriefUtil.getOpenAPIBriefs(
				companyId
			).keySet());

		toolSetNames.remove(MCPToolConstants.OPENAPI_TOOL_SET_NAME);

		return toolSetNames;
	}

	private Map<String, Integer> _getToolSetSizes(
		Collection<MCPTool> mcpTools) {

		Map<String, Integer> sizes = new HashMap<>();

		for (MCPTool mcpTool : mcpTools) {
			Integer size = sizes.get(mcpTool.getToolSetName());

			sizes.put(mcpTool.getToolSetName(), (size == null) ? 1 : size + 1);
		}

		return sizes;
	}

	private String _getUID(MCPTool mcpTool) {
		return mcpTool.getToolSetName() + StringPool.COLON +
			mcpTool.getToolName();
	}

	private void _indexToolSets(
		String indexName, Collection<MCPTool> mcpTools,
		Map<String, String> toolSetHashes) {

		Map<String, Integer> toolSetSizes = _getToolSetSizes(mcpTools);

		BulkDocumentRequest bulkDocumentRequest = new BulkDocumentRequest();

		for (MCPTool mcpTool : mcpTools) {
			bulkDocumentRequest.addBulkableDocumentRequest(
				new IndexDocumentRequest(
					indexName, _getUID(mcpTool),
					_buildDocument(mcpTool, toolSetHashes, toolSetSizes)));
		}

		if (!mcpTools.isEmpty()) {
			_searchEngineAdapter.execute(bulkDocumentRequest);
		}
	}

	private boolean _isIndexStale(
		long companyId, MCPToolIndexState mcpToolIndexState) {

		if (!Objects.equals(
				mcpToolIndexState.getIndexedToolSetNames(),
				_getToolSetNames(companyId)) ||
			!_mcpToolIndexCreator.indexExists(companyId)) {

			return true;
		}

		return !mcpToolIndexState.isUpToDate();
	}

	private void _update(
		long companyId, MCPToolIndexState mcpToolIndexState,
		HttpServletRequest httpServletRequest) {

		// Use fresh set of names if other node deleted the index

		Set<String> indexedToolSetNames = new HashSet<>();

		if (_mcpToolIndexCreator.indexExists(companyId)) {
			indexedToolSetNames = mcpToolIndexState.getIndexedToolSetNames();
		}

		_mcpToolIndexCreator.createIfNotExists(companyId);

		Map<String, Object> staleToolSets =
			mcpToolIndexState.getStaleToolSets();

		Set<String> toolSetNames = _getToolSetNames(companyId);

		Set<String> changedToolSetNames = _getChangedToolSetNames(
			indexedToolSetNames, mcpToolIndexState, staleToolSets,
			toolSetNames);

		Set<String> failedToolSetNames = new HashSet<>();

		List<MCPTool> mcpTools = MCPToolFactoryUtil.getMCPTools(
			httpServletRequest, failedToolSetNames, changedToolSetNames);

		changedToolSetNames.removeAll(failedToolSetNames);

		_writeToolSets(
			companyId, changedToolSetNames, indexedToolSetNames, mcpTools,
			toolSetNames);

		_updateIndexState(
			companyId, changedToolSetNames, failedToolSetNames,
			mcpToolIndexState, staleToolSets, toolSetNames);
	}

	private void _updateIndexState(
		long companyId, Set<String> changedToolSetNames,
		Set<String> failedToolSetNames, MCPToolIndexState mcpToolIndexState,
		Map<String, Object> staleToolSets, Set<String> toolSetNames) {

		mcpToolIndexState.setIndexedToolSetNames(toolSetNames);

		// Clear a tool set only if its entry is the one this update saw

		for (String changedToolSetName : changedToolSetNames) {
			mcpToolIndexState.clearStaleToolSet(
				changedToolSetName, staleToolSets.get(changedToolSetName));
		}

		mcpToolIndexState.clearAllToolSetsStale(staleToolSets);

		for (String failedToolSetName : failedToolSetNames) {
			invalidate(companyId, failedToolSetName);
		}
	}

	private void _writeToolSets(
		long companyId, Set<String> changedToolSetNames,
		Set<String> indexedToolSetNames, List<MCPTool> mcpTools,
		Set<String> toolSetNames) {

		String indexName = _mcpToolIndexCreator.getIndexName(companyId);

		Map<String, String> toolSetHashes = _getToolSetHashes(mcpTools);

		_indexToolSets(indexName, mcpTools, toolSetHashes);

		_deleteReplacedToolSets(indexName, toolSetHashes, changedToolSetNames);

		Set<String> removedToolSetNames = new HashSet<>(indexedToolSetNames);

		removedToolSetNames.removeAll(toolSetNames);

		_deleteToolSets(indexName, removedToolSetNames);
	}

	private final Map<Long, MCPToolIndexState> _companyIndexStates =
		new ConcurrentHashMap<>();

	@Reference
	private MCPToolIndexCreator _mcpToolIndexCreator;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

	private static class MCPToolIndexState {

		public void clearAllToolSetsStale(Map<String, Object> staleToolSets) {
			_staleToolSets.remove(
				StringPool.STAR, staleToolSets.get(StringPool.STAR));
		}

		public void clearStaleToolSet(String toolSetName, Object staleToolSet) {
			_staleToolSets.remove(toolSetName, staleToolSet);
		}

		public Set<String> getIndexedToolSetNames() {
			return new HashSet<>(_indexedToolSetNames);
		}

		public Map<String, Object> getStaleToolSets() {
			return new HashMap<>(_staleToolSets);
		}

		public boolean isAllToolSetsStale() {
			return _staleToolSets.containsKey(StringPool.STAR);
		}

		public boolean isUpToDate() {
			return _staleToolSets.isEmpty();
		}

		public void setAllToolSetsStale() {
			_staleToolSets.put(StringPool.STAR, new Object());
		}

		public void setIndexedToolSetNames(Set<String> toolSetNames) {
			_indexedToolSetNames.clear();
			_indexedToolSetNames.addAll(toolSetNames);
		}

		public void setToolSetStale(String toolSetName) {
			_staleToolSets.put(toolSetName, new Object());
		}

		private final Set<String> _indexedToolSetNames =
			ConcurrentHashMap.newKeySet();
		private final Map<String, Object> _staleToolSets =
			new ConcurrentHashMap<>();

	}

}