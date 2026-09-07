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
import com.liferay.mcp.server.rest.internal.search.index.util.SchemaUtil;
import com.liferay.mcp.server.rest.internal.util.OpenAPIBriefUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.document.DocumentBuilder;
import com.liferay.portal.search.document.DocumentBuilderFactory;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.DeleteByQueryDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.DocumentRequest;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.TermsQuery;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Petteri Karttunen
 */
public class MCPToolIndexWriterUtil {

	public static void invalidate(long companyId) {
		_changeCounts.remove(companyId);

		_indexedToolSetNames.remove(companyId);

		_schemaPropertyCounts.remove(companyId);

		_staleToolSetNames.remove(companyId);

		_toolSetSizes.remove(companyId);
	}

	public static void invalidate(long companyId, String toolSetName) {
		if (Validator.isBlank(toolSetName)) {
			invalidate(companyId);

			return;
		}

		_changeCounts.remove(companyId);

		Set<String> staleToolSetNames = _staleToolSetNames.computeIfAbsent(
			companyId, key -> ConcurrentHashMap.newKeySet());

		staleToolSetNames.add(toolSetName);
	}

	public static void rebuildIfStale(
		long companyId, HttpServletRequest httpServletRequest,
		long changeCount) {

		if (!_isStale(companyId, changeCount)) {
			return;
		}

		synchronized (_rebuildLocks.computeIfAbsent(
			companyId, key -> new Object())) {

			if (!_isStale(companyId, changeCount)) {
				return;
			}

			_rebuild(companyId, httpServletRequest, changeCount);
		}
	}

	private static void _deleteToolSets(
		String indexName, Set<String> toolSetNames) {

		if (toolSetNames.isEmpty()) {
			return;
		}

		TermsQuery termsQuery = new TermsQuery(MCPToolFields.TOOL_SET_NAME);

		for (String toolSetName : toolSetNames) {
			termsQuery.addValue(toolSetName);
		}

		DeleteByQueryDocumentRequest deleteByQueryDocumentRequest =
			new DeleteByQueryDocumentRequest(termsQuery, indexName);

		deleteByQueryDocumentRequest.setRefresh(true);

		_execute(deleteByQueryDocumentRequest);
	}

	private static void _execute(DocumentRequest<?> documentRequest) {
		SearchEngineAdapter searchEngineAdapter =
			_searchEngineAdapterSnapshot.get();

		searchEngineAdapter.execute(documentRequest);
	}

	private static Set<String> _getStaleToolSetNames(long companyId) {
		Set<String> staleToolSetNames = _staleToolSetNames.get(companyId);

		if (staleToolSetNames == null) {
			return Collections.emptySet();
		}

		return new HashSet<>(staleToolSetNames);
	}

	private static Set<String> _getToolSetNames(Collection<MCPTool> mcpTools) {
		Set<String> toolSetNames = new HashSet<>();

		for (MCPTool mcpTool : mcpTools) {
			toolSetNames.add(mcpTool.getToolSetName());
		}

		return toolSetNames;
	}

	private static Map<String, Integer> _getToolSetSizes(
		Collection<MCPTool> mcpTools) {

		Map<String, Integer> sizes = new HashMap<>();

		for (MCPTool mcpTool : mcpTools) {
			Integer size = sizes.get(mcpTool.getToolSetName());

			sizes.put(mcpTool.getToolSetName(), (size == null) ? 1 : size + 1);
		}

		return sizes;
	}

	private static int _getTotalSize(Map<String, Integer> toolSetSizes) {
		int size = 0;

		for (Integer toolSetSize : toolSetSizes.values()) {
			size += toolSetSize;
		}

		return size;
	}

	private static String _getUID(MCPTool mcpTool) {
		return mcpTool.getToolSetName() + StringPool.COLON +
			mcpTool.getToolName();
	}

	private static void _index(
		Set<String> envelopePropertyNames, String indexName,
		Collection<MCPTool> mcpTools) {

		BulkDocumentRequest bulkDocumentRequest = new BulkDocumentRequest();

		bulkDocumentRequest.setRefresh(true);

		for (MCPTool mcpTool : mcpTools) {
			String uid = _getUID(mcpTool);

			DocumentBuilder documentBuilder = DocumentBuilderFactory.builder();

			documentBuilder.setString(
				MCPToolFields.DESCRIPTION, mcpTool.getDescription());
			documentBuilder.setStrings(
				MCPToolFields.EXPANSION, mcpTool.getExpansions());
			documentBuilder.setString(
				MCPToolFields.METHOD, mcpTool.getMethod());
			documentBuilder.setStrings(
				MCPToolFields.PARAMETERS, mcpTool.getParameters());
			documentBuilder.setString(MCPToolFields.PATH, mcpTool.getPath());
			documentBuilder.setValue(
				MCPToolFields.PATH_SEGMENTS_COUNT,
				StringUtil.count(mcpTool.getPath(), CharPool.SLASH));
			documentBuilder.setStrings(
				MCPToolFields.REQUIRED_REFERENCES,
				mcpTool.getRequiredReferences());
			documentBuilder.setStrings(
				MCPToolFields.SCHEMA_PROPERTIES,
				SchemaUtil.getIndexableSchemaProperties(
					envelopePropertyNames, mcpTool.getSchemaProperties()));
			documentBuilder.setString(
				MCPToolFields.ENTITY_NAME, mcpTool.getEntityName());
			documentBuilder.setString(
				MCPToolFields.TOOL_NAME, mcpTool.getToolName());
			documentBuilder.setString(
				MCPToolFields.TOOL_SET_NAME, mcpTool.getToolSetName());
			documentBuilder.setString(MCPToolFields.UID, uid);
			documentBuilder.setString(
				MCPToolFields.IDENTIFIER, mcpTool.getIdentifier());
			documentBuilder.setString(
				MCPToolFields.INTENT, mcpTool.getIntent());
			documentBuilder.setValue(
				MCPToolFields.DEPRECATED, mcpTool.isDeprecated());
			documentBuilder.setString(
				MCPToolFields.MODIFIER, mcpTool.getModifier());

			bulkDocumentRequest.addBulkableDocumentRequest(
				new IndexDocumentRequest(
					indexName, uid, documentBuilder.build()));
		}

		if (!mcpTools.isEmpty()) {
			_execute(bulkDocumentRequest);
		}
	}

	private static boolean _isStale(long companyId, long changeCount) {
		if (!MCPToolIndexCreatorUtil.indexExists(companyId)) {
			return true;
		}

		return !Objects.equals(changeCount, _changeCounts.get(companyId));
	}

	private static void _prune(
		Set<String> failedToolSetNames, String indexName,
		Collection<MCPTool> mcpTools) {

		if (mcpTools.isEmpty()) {
			return;
		}

		TermsQuery termsQuery = new TermsQuery(MCPToolFields.UID);

		for (MCPTool mcpTool : mcpTools) {
			termsQuery.addValue(_getUID(mcpTool));
		}

		BooleanQuery booleanQuery = new BooleanQuery();

		booleanQuery.addMustNotQueryClauses(termsQuery);

		if (!failedToolSetNames.isEmpty()) {
			TermsQuery failedTermsQuery = new TermsQuery(
				MCPToolFields.TOOL_SET_NAME);

			for (String failedToolSetName : failedToolSetNames) {
				failedTermsQuery.addValue(failedToolSetName);
			}

			booleanQuery.addMustNotQueryClauses(failedTermsQuery);
		}

		DeleteByQueryDocumentRequest deleteByQueryDocumentRequest =
			new DeleteByQueryDocumentRequest(booleanQuery, indexName);

		deleteByQueryDocumentRequest.setRefresh(true);

		_execute(deleteByQueryDocumentRequest);
	}

	private static void _rebuild(
		long companyId, HttpServletRequest httpServletRequest,
		long changeCount) {

		Set<String> failedToolSetNames = new HashSet<>();

		if (!_replaceChangedToolSets(
				companyId, failedToolSetNames, httpServletRequest)) {

			_replaceAll(
				companyId, failedToolSetNames,
				MCPToolFactoryUtil.getMCPTools(
					httpServletRequest, failedToolSetNames, null));
		}

		_updateStaleness(companyId, changeCount, failedToolSetNames);
	}

	private static void _replaceAll(
		long companyId, Set<String> failedToolSetNames,
		Collection<MCPTool> mcpTools) {

		MCPToolIndexCreatorUtil.createIfNotExists(companyId);

		String indexName = MCPToolIndexCreatorUtil.getIndexName(companyId);

		Map<String, Map<String, Integer>> schemaPropertyCounts =
			SchemaUtil.getSchemaPropertyCounts(mcpTools);

		_schemaPropertyCounts.put(companyId, schemaPropertyCounts);

		Map<String, Integer> toolSetSizes = _getToolSetSizes(mcpTools);

		_toolSetSizes.put(companyId, toolSetSizes);

		_indexedToolSetNames.put(companyId, _getToolSetNames(mcpTools));

		Set<String> envelopePropertyNames = SchemaUtil.getEnvelopePropertyNames(
			SchemaUtil.getSchemaPropertyTotalCounts(schemaPropertyCounts),
			mcpTools.size());

		ResolverUtil.replace(companyId, mcpTools, toolSetSizes);

		_index(envelopePropertyNames, indexName, mcpTools);

		_prune(failedToolSetNames, indexName, mcpTools);

		_staleToolSetNames.remove(companyId);
	}

	private static boolean _replaceChangedToolSets(
		long companyId, Set<String> failedToolSetNames,
		HttpServletRequest httpServletRequest) {

		Set<String> indexedToolSetNames = _indexedToolSetNames.get(companyId);

		if (indexedToolSetNames == null) {
			return false;
		}

		Set<String> toolSetNames = new HashSet<>(
			OpenAPIBriefUtil.getOpenAPIBriefs(
			).keySet());

		toolSetNames.remove(MCPToolConstants.OPENAPI_TOOL_SET_NAME);

		if (!toolSetNames.containsAll(indexedToolSetNames)) {
			return false;
		}

		Set<String> addedToolSetNames = new HashSet<>(toolSetNames);

		addedToolSetNames.removeAll(indexedToolSetNames);

		Set<String> replacedToolSetNames = new HashSet<>(
			_getStaleToolSetNames(companyId));

		replacedToolSetNames.retainAll(toolSetNames);
		replacedToolSetNames.removeAll(addedToolSetNames);

		Set<String> changedToolSetNames = new HashSet<>(addedToolSetNames);

		changedToolSetNames.addAll(replacedToolSetNames);

		List<MCPTool> mcpTools = MCPToolFactoryUtil.getMCPTools(
			httpServletRequest, failedToolSetNames, changedToolSetNames);

		replacedToolSetNames.removeAll(failedToolSetNames);

		return _replaceToolSets(
			companyId, mcpTools, replacedToolSetNames, toolSetNames);
	}

	private static boolean _replaceToolSets(
		long companyId, Collection<MCPTool> mcpTools,
		Set<String> replacedToolSetNames, Set<String> toolSetNames) {

		Map<String, Map<String, Integer>> schemaPropertyCounts =
			_schemaPropertyCounts.get(companyId);

		Map<String, Integer> toolSetSizes = _toolSetSizes.get(companyId);

		if ((schemaPropertyCounts == null) || (toolSetSizes == null) ||
			!MCPToolIndexCreatorUtil.indexExists(companyId)) {

			return false;
		}

		Map<String, Map<String, Integer>> updatedSchemaPropertyCounts =
			new HashMap<>(schemaPropertyCounts);

		Map<String, Integer> updatedToolSetSizes = new HashMap<>(toolSetSizes);

		for (String replacedToolSetName : replacedToolSetNames) {
			updatedSchemaPropertyCounts.remove(replacedToolSetName);
			updatedToolSetSizes.remove(replacedToolSetName);
		}

		updatedSchemaPropertyCounts.putAll(
			SchemaUtil.getSchemaPropertyCounts(mcpTools));

		updatedToolSetSizes.putAll(_getToolSetSizes(mcpTools));

		Set<String> envelopePropertyNames = SchemaUtil.getEnvelopePropertyNames(
			SchemaUtil.getSchemaPropertyTotalCounts(
				updatedSchemaPropertyCounts),
			_getTotalSize(updatedToolSetSizes));

		if (!Objects.equals(
				envelopePropertyNames,
				SchemaUtil.getEnvelopePropertyNames(
					SchemaUtil.getSchemaPropertyTotalCounts(
						schemaPropertyCounts),
					_getTotalSize(toolSetSizes)))) {

			return false;
		}

		String indexName = MCPToolIndexCreatorUtil.getIndexName(companyId);

		_deleteToolSets(indexName, replacedToolSetNames);

		_index(envelopePropertyNames, indexName, mcpTools);

		ResolverUtil.merge(companyId, mcpTools, updatedToolSetSizes);

		_indexedToolSetNames.put(companyId, toolSetNames);
		_schemaPropertyCounts.put(companyId, updatedSchemaPropertyCounts);
		_toolSetSizes.put(companyId, updatedToolSetSizes);

		Set<String> staleToolSetNames = _staleToolSetNames.get(companyId);

		if (staleToolSetNames != null) {
			staleToolSetNames.removeAll(replacedToolSetNames);
		}

		return true;
	}

	private static void _updateStaleness(
		long companyId, long changeCount, Set<String> failedToolSetNames) {

		if (failedToolSetNames.isEmpty()) {
			_changeCounts.put(companyId, changeCount);

			return;
		}

		for (String failedToolSetName : failedToolSetNames) {
			invalidate(companyId, failedToolSetName);
		}
	}

	private static final Map<Long, Long> _changeCounts =
		new ConcurrentHashMap<>();
	private static final Map<Long, Set<String>> _indexedToolSetNames =
		new ConcurrentHashMap<>();
	private static final Map<Long, Object> _rebuildLocks =
		new ConcurrentHashMap<>();
	private static final Map<Long, Map<String, Map<String, Integer>>>
		_schemaPropertyCounts = new ConcurrentHashMap<>();
	private static final Snapshot<SearchEngineAdapter>
		_searchEngineAdapterSnapshot = new Snapshot<>(
			MCPToolIndexWriterUtil.class, SearchEngineAdapter.class);
	private static final Map<Long, Set<String>> _staleToolSetNames =
		new ConcurrentHashMap<>();
	private static final Map<Long, Map<String, Integer>> _toolSetSizes =
		new ConcurrentHashMap<>();

}