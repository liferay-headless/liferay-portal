/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index.util;

import com.liferay.mcp.server.rest.internal.constants.MCPToolConstants;
import com.liferay.mcp.server.rest.internal.search.constants.MCPToolActionMarkers;
import com.liferay.mcp.server.rest.internal.search.constants.MCPToolFields;
import com.liferay.mcp.server.rest.internal.search.constants.MCPToolModifiers;
import com.liferay.mcp.server.rest.internal.util.OpenAPIBrief;
import com.liferay.mcp.server.rest.internal.util.OpenAPIBriefUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
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

import java.util.ArrayList;
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

	private static void _addMCPTools(
		HttpServletRequest httpServletRequest, List<MCPTool> mcpTools,
		OpenAPIBrief openAPIBrief, String toolSetName) {

		JSONObject openAPIJSONObject = OpenAPIBriefUtil.getOpenAPIJSONObject(
			httpServletRequest, openAPIBrief);

		JSONObject pathsJSONObject = openAPIJSONObject.getJSONObject("paths");

		if (pathsJSONObject == null) {
			return;
		}

		for (String path : pathsJSONObject.keySet()) {
			JSONObject pathItemJSONObject = pathsJSONObject.getJSONObject(path);

			for (String method : MCPToolConstants.METHODS) {
				JSONObject operationJSONObject =
					pathItemJSONObject.getJSONObject(method);

				if (operationJSONObject == null) {
					continue;
				}

				String toolName = operationJSONObject.getString("operationId");

				if (Validator.isBlank(toolName)) {
					continue;
				}

				String entityName = _getEntityName(operationJSONObject);
				String marker = _getActionMarker(method, path, toolName);
				String modifier = _getModifier(entityName, path);

				if (Objects.equals(
						modifier, MCPToolModifiers.MODIFIER_TRAVERSAL)) {

					entityName = _getTargetEntityName(
						entityName, path, toolName);
				}

				mcpTools.add(
					new MCPTool(
						operationJSONObject.getBoolean("deprecated"),
						StringUtil.trim(_getDescription(operationJSONObject)),
						entityName,
						MCPToolExpansionUtil.getExpansion(
							Objects.equals(
								modifier, MCPToolModifiers.MODIFIER_BATCH),
							marker, method, path, _getTags(operationJSONObject),
							toolName),
						_getIdentifier(path),
						MCPToolIntentExtractorUtil.getIntent(
							marker, method, toolName),
						method, modifier,
						MCPToolSchemaUtil.getParameters(
							operationJSONObject, pathItemJSONObject),
						"/o" + openAPIBrief.getBasePath() + path,
						MCPToolSchemaUtil.getRequiredReferences(
							openAPIJSONObject, operationJSONObject, path),
						MCPToolSchemaUtil.getSchemaProperties(
							openAPIJSONObject, operationJSONObject),
						toolName, toolSetName));
			}
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

	private static String _getActionMarker(
		String method, String path, String toolName) {

		if (Objects.equals(method, "get") || Objects.equals(method, "head") ||
			Objects.equals(method, "options")) {

			return null;
		}

		for (String marker : MCPToolActionMarkers.verbs.keySet()) {
			if (toolName.endsWith(marker) && _isPathMarker(path, marker)) {
				return marker;
			}
		}

		return null;
	}

	private static String _getDescription(JSONObject operationJSONObject) {
		String description = operationJSONObject.getString("description");
		String summary = operationJSONObject.getString("summary");

		if (!Validator.isBlank(description) && !Validator.isBlank(summary)) {
			return summary + ". " + description;
		}

		if (!Validator.isBlank(description)) {
			return description;
		}

		if (!Validator.isBlank(summary)) {
			return summary;
		}

		return StringPool.BLANK;
	}

	private static String _getEntityName(JSONObject operationJSONObject) {
		JSONArray tagsJSONArray = operationJSONObject.getJSONArray("tags");

		if (JSONUtil.isEmpty(tagsJSONArray)) {
			return StringPool.BLANK;
		}

		return tagsJSONArray.getString(0);
	}

	private static String _getIdentifier(String path) {
		String[] segments = StringUtil.split(path, CharPool.SLASH);

		for (int i = segments.length - 1; i >= 0; i--) {
			String segment = segments[i];

			if (!StringUtil.startsWith(
					StringUtil.toLowerCase(segment), "by-")) {

				continue;
			}

			return StringUtil.toLowerCase(
				MCPToolWordUtil.humanize(
					segment.substring(3)
				).replace(
					CharPool.SPACE, CharPool.DASH
				));
		}

		return StringPool.BLANK;
	}

	private static List<MCPTool> _getMCPTools(
		HttpServletRequest httpServletRequest, Set<String> failedToolSetNames,
		Set<String> toolSetNames) {

		List<MCPTool> mcpTools = new ArrayList<>();

		for (Map.Entry<String, OpenAPIBrief> entry :
				OpenAPIBriefUtil.getOpenAPIBriefs(
				).entrySet()) {

			String toolSetName = entry.getKey();

			if (((toolSetNames != null) &&
				 !toolSetNames.contains(toolSetName)) ||
				Objects.equals(toolSetName, _OPENAPI_TOOL_SET_NAME)) {

				continue;
			}

			try {
				_addMCPTools(
					httpServletRequest, mcpTools, entry.getValue(),
					toolSetName);
			}
			catch (Exception exception) {
				failedToolSetNames.add(toolSetName);

				_log.error(
					"Unable to index the \"" + toolSetName + "\"", exception);
			}
		}

		return mcpTools;
	}

	private static String _getModifier(String path) {
		String[] segments = StringUtil.split(path, CharPool.SLASH);

		for (int i = segments.length - 1; i >= 0; i--) {
			String segment = StringUtil.toLowerCase(segments[i]);

			if (Validator.isNull(segment) || segment.startsWith("{")) {
				continue;
			}

			int index = segment.indexOf(CharPool.PERIOD);

			if (index > 0) {
				segment = segment.substring(0, index);
			}

			String modifier = MCPToolModifiers.pathSegmentModifiers.get(
				StringUtil.removeLast(segment, "-replace"));

			if (modifier != null) {
				return modifier;
			}
		}

		int count = 0;

		for (String segment : segments) {
			if (StringUtil.startsWith(
					StringUtil.toLowerCase(segment), "by-external")) {

				count++;
			}
		}

		if (count > 1) {
			return MCPToolModifiers.MODIFIER_NESTED;
		}

		return StringPool.BLANK;
	}

	private static String _getModifier(String entityName, String path) {
		String modifier = _getModifier(path);

		if (Validator.isNotNull(modifier)) {
			return modifier;
		}

		if (_isTraversal(entityName, path)) {
			return MCPToolModifiers.MODIFIER_TRAVERSAL;
		}

		return StringPool.BLANK;
	}

	private static Set<String> _getStaleToolSetNames(long companyId) {
		Set<String> staleToolSetNames = _staleToolSetNames.get(companyId);

		if (staleToolSetNames == null) {
			return Collections.emptySet();
		}

		return new HashSet<>(staleToolSetNames);
	}

	private static String _getTags(JSONObject operationJSONObject) {
		JSONArray tagsJSONArray = operationJSONObject.getJSONArray("tags");

		if (tagsJSONArray == null) {
			return StringPool.BLANK;
		}

		StringBundler sb = new StringBundler(tagsJSONArray.length() * 2);

		for (int i = 0; i < tagsJSONArray.length(); i++) {
			sb.append(MCPToolWordUtil.humanize(tagsJSONArray.getString(i)));
			sb.append(StringPool.SPACE);
		}

		return sb.toString();
	}

	/**
	 * Answers the entity a traversal path actually targets. The operation's
	 * first tag names the resource the operation is grouped under, which for a
	 * nested path is the parent: "POST
	 * /sites/{id}/site-pages/{id}/page-specifications" is tagged "SitePage"
	 * while the GET on the same path is tagged "PageSpecification". The last
	 * plural segment names the target, so it harmonizes the two.
	 */
	private static String _getTargetEntityName(
		String entityName, String path, String toolName) {

		// Only when the tool name already carries the parent. Where it does
		// not, the parent is matched by the path alone, so handing the entity
		// field to the target would drop the only strong signal for it:
		// "CMSBlog" would become the generic "Version".

		String comparableToolName = MCPToolWordUtil.toComparable(toolName);

		if (!comparableToolName.contains(
				MCPToolWordUtil.toComparable(entityName))) {

			return entityName;
		}

		String[] segments = StringUtil.split(path, CharPool.SLASH);

		for (int i = segments.length - 1; i >= 0; i--) {
			String segment = segments[i];

			if (segment.startsWith("{") ||
				StringUtil.startsWith(segment, "by-") ||
				MCPToolModifiers.pathSegmentModifiers.containsKey(segment) ||
				!_isPlural(segment)) {

				continue;
			}

			StringBundler sb = new StringBundler();

			for (String word : StringUtil.split(segment, CharPool.DASH)) {
				sb.append(StringUtil.upperCaseFirstLetter(word));
			}

			return MCPToolWordUtil.toSingular(sb.toString());
		}

		return entityName;
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
				MCPToolSchemaUtil.getIndexableSchemaProperties(
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

	private static boolean _isEntitySegment(String entityName, String segment) {
		String entity = MCPToolWordUtil.toComparable(entityName);
		String comparableSegment = MCPToolWordUtil.toComparable(segment);

		if (comparableSegment.equals(entity) ||
			comparableSegment.equals(entity + "s") ||
			comparableSegment.equals(entity + "es")) {

			return true;
		}

		if (entity.endsWith("y")) {
			return comparableSegment.equals(
				entity.substring(0, entity.length() - 1) + "ies");
		}

		return false;
	}

	private static boolean _isPathMarker(String path, String marker) {
		String markerName = MCPToolWordUtil.toComparable(
			StringUtil.removeLast(marker, "Page"));

		for (String segment : StringUtil.split(path, CharPool.SLASH)) {
			if (Validator.isNull(segment) || segment.startsWith("{")) {
				continue;
			}

			int index = segment.indexOf(CharPool.PERIOD);

			if (index > 0) {
				segment = segment.substring(0, index);
			}

			String segmentName = MCPToolWordUtil.toComparable(segment);

			if (segmentName.equals(markerName) ||
				segmentName.endsWith(markerName) ||
				markerName.startsWith(
					MCPToolWordUtil.toSingular(segmentName))) {

				return true;
			}
		}

		return false;
	}

	private static boolean _isPlural(String segment) {
		return StringUtil.endsWith(
			MCPToolWordUtil.toComparable(segment), CharPool.LOWER_CASE_S);
	}

	private static boolean _isStale(long companyId, long changeCount) {
		if (!MCPToolIndexCreatorUtil.indexExists(companyId)) {
			return true;
		}

		return !Objects.equals(changeCount, _changeCounts.get(companyId));
	}

	private static boolean _isTraversal(String entityName, String path) {
		if (Validator.isNull(entityName)) {
			return false;
		}

		List<String> segments = new ArrayList<>();
		boolean parameter = false;

		for (String segment : StringUtil.split(path, CharPool.SLASH)) {
			if (Validator.isNull(segment)) {
				continue;
			}

			if (segment.charAt(0) == CharPool.OPEN_CURLY_BRACE) {
				parameter = true;

				continue;
			}

			if (parameter) {
				segments.add(segment);
			}
		}

		if (segments.isEmpty()) {
			return false;
		}

		String segment = segments.get(segments.size() - 1);

		if (_isEntitySegment(entityName, segment)) {
			return false;
		}

		if (_isPlural(segment)) {
			return true;
		}

		if (segments.size() < 2) {
			return false;
		}

		segment = segments.get(segments.size() - 2);

		if (_isPlural(segment) && !_isEntitySegment(entityName, segment)) {
			return true;
		}

		return false;
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

		Set<String> indexedToolSetNames = _indexedToolSetNames.get(companyId);

		if (indexedToolSetNames != null) {
			Set<String> toolSetNames = new HashSet<>(
				OpenAPIBriefUtil.getOpenAPIBriefs(
				).keySet());

			toolSetNames.remove(_OPENAPI_TOOL_SET_NAME);

			Set<String> addedToolSetNames = new HashSet<>(toolSetNames);

			addedToolSetNames.removeAll(indexedToolSetNames);

			Set<String> removedToolSetNames = new HashSet<>(
				indexedToolSetNames);

			removedToolSetNames.removeAll(toolSetNames);

			if (removedToolSetNames.isEmpty()) {
				Set<String> replacedToolSetNames = _getStaleToolSetNames(
					companyId);

				replacedToolSetNames.retainAll(toolSetNames);

				replacedToolSetNames.removeAll(addedToolSetNames);

				Set<String> changedToolSetNames = new HashSet<>(
					addedToolSetNames);

				changedToolSetNames.addAll(replacedToolSetNames);

				List<MCPTool> mcpTools = _getMCPTools(
					httpServletRequest, failedToolSetNames,
					changedToolSetNames);

				replacedToolSetNames.removeAll(failedToolSetNames);

				if (_replaceToolSets(
						companyId, mcpTools, replacedToolSetNames,
						toolSetNames)) {

					_updateStaleness(
						companyId, changeCount, failedToolSetNames);

					return;
				}
			}
		}

		_replaceAll(
			companyId, failedToolSetNames,
			_getMCPTools(httpServletRequest, failedToolSetNames, null));

		_updateStaleness(companyId, changeCount, failedToolSetNames);
	}

	private static void _replaceAll(
		long companyId, Set<String> failedToolSetNames,
		Collection<MCPTool> mcpTools) {

		MCPToolIndexCreatorUtil.createIfNotExists(companyId);

		String indexName = MCPToolIndexCreatorUtil.getIndexName(companyId);

		Map<String, Map<String, Integer>> schemaPropertyCounts =
			MCPToolSchemaUtil.getSchemaPropertyCounts(mcpTools);

		_schemaPropertyCounts.put(companyId, schemaPropertyCounts);

		Map<String, Integer> toolSetSizes = _getToolSetSizes(mcpTools);

		_toolSetSizes.put(companyId, toolSetSizes);

		_indexedToolSetNames.put(companyId, _getToolSetNames(mcpTools));

		Set<String> envelopePropertyNames =
			MCPToolSchemaUtil.getEnvelopePropertyNames(
				MCPToolSchemaUtil.getSchemaPropertyTotalCounts(
					schemaPropertyCounts),
				mcpTools.size());

		MCPToolResolverUtil.replace(companyId, mcpTools, toolSetSizes);

		_index(envelopePropertyNames, indexName, mcpTools);

		_prune(failedToolSetNames, indexName, mcpTools);

		_staleToolSetNames.remove(companyId);
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
			MCPToolSchemaUtil.getSchemaPropertyCounts(mcpTools));

		updatedToolSetSizes.putAll(_getToolSetSizes(mcpTools));

		Set<String> envelopePropertyNames =
			MCPToolSchemaUtil.getEnvelopePropertyNames(
				MCPToolSchemaUtil.getSchemaPropertyTotalCounts(
					updatedSchemaPropertyCounts),
				_getTotalSize(updatedToolSetSizes));

		if (!Objects.equals(
				envelopePropertyNames,
				MCPToolSchemaUtil.getEnvelopePropertyNames(
					MCPToolSchemaUtil.getSchemaPropertyTotalCounts(
						schemaPropertyCounts),
					_getTotalSize(toolSetSizes)))) {

			return false;
		}

		String indexName = MCPToolIndexCreatorUtil.getIndexName(companyId);

		_deleteToolSets(indexName, replacedToolSetNames);

		_index(envelopePropertyNames, indexName, mcpTools);

		MCPToolResolverUtil.merge(companyId, mcpTools, updatedToolSetSizes);

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

	private static final String _OPENAPI_TOOL_SET_NAME = "openapi";

	private static final Log _log = LogFactoryUtil.getLog(
		MCPToolIndexWriterUtil.class);

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