/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index.util;

import com.liferay.mcp.server.rest.internal.constants.MCPToolConstants;
import com.liferay.mcp.server.rest.internal.search.constants.MCPToolActionMarkers;
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
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Petteri Karttunen
 */
public class MCPToolFactoryUtil {

	public static List<MCPTool> getMCPTools(
		HttpServletRequest httpServletRequest, Set<String> failedToolSetNames,
		Set<String> toolSetNames) {

		List<MCPTool> mcpTools = new ArrayList<>();

		for (Map.Entry<String, OpenAPIBrief> entry :
				OpenAPIBriefUtil.getOpenAPIBriefs(
				).entrySet()) {

			String toolSetName = entry.getKey();

			if (((toolSetNames != null) &&
				 !toolSetNames.contains(toolSetName)) ||
				Objects.equals(
					toolSetName, MCPToolConstants.OPENAPI_TOOL_SET_NAME)) {

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
				String modifier = ModifierExtractorUtil.getModifier(
					entityName, path);

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
						ExpansionUtil.getExpansion(
							Objects.equals(
								modifier, MCPToolModifiers.MODIFIER_BATCH),
							marker, method, path, _getTags(operationJSONObject),
							toolName),
						_getIdentifier(path),
						IntentExtractorUtil.getIntent(marker, method, toolName),
						method, modifier,
						SchemaUtil.getParameters(
							operationJSONObject, pathItemJSONObject),
						"/o" + openAPIBrief.getBasePath() + path,
						SchemaUtil.getRequiredReferences(
							openAPIJSONObject, operationJSONObject, path),
						SchemaUtil.getSchemaProperties(
							openAPIJSONObject, operationJSONObject),
						toolName, toolSetName));
			}
		}
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
				WordUtil.humanize(
					segment.substring(3)
				).replace(
					CharPool.SPACE, CharPool.DASH
				));
		}

		return StringPool.BLANK;
	}

	private static String _getTags(JSONObject operationJSONObject) {
		JSONArray tagsJSONArray = operationJSONObject.getJSONArray("tags");

		if (tagsJSONArray == null) {
			return StringPool.BLANK;
		}

		StringBundler sb = new StringBundler(tagsJSONArray.length() * 2);

		for (int i = 0; i < tagsJSONArray.length(); i++) {
			sb.append(WordUtil.humanize(tagsJSONArray.getString(i)));
			sb.append(StringPool.SPACE);
		}

		return sb.toString();
	}

	private static String _getTargetEntityName(
		String entityName, String path, String toolName) {

		String comparableToolName = WordUtil.toComparable(toolName);

		if (!comparableToolName.contains(WordUtil.toComparable(entityName))) {
			return entityName;
		}

		String[] segments = StringUtil.split(path, CharPool.SLASH);

		for (int i = segments.length - 1; i >= 0; i--) {
			String segment = segments[i];

			if (segment.startsWith("{") ||
				StringUtil.startsWith(segment, "by-") ||
				MCPToolModifiers.pathSegmentModifiers.containsKey(segment) ||
				!WordUtil.isPlural(segment)) {

				continue;
			}

			StringBundler sb = new StringBundler();

			for (String word : StringUtil.split(segment, CharPool.DASH)) {
				sb.append(StringUtil.upperCaseFirstLetter(word));
			}

			return WordUtil.toSingular(sb.toString());
		}

		return entityName;
	}

	private static boolean _isPathMarker(String path, String marker) {
		String markerName = WordUtil.toComparable(
			StringUtil.removeLast(marker, "Page"));

		for (String segment : StringUtil.split(path, CharPool.SLASH)) {
			if (Validator.isNull(segment) || segment.startsWith("{")) {
				continue;
			}

			int index = segment.indexOf(CharPool.PERIOD);

			if (index > 0) {
				segment = segment.substring(0, index);
			}

			String segmentName = WordUtil.toComparable(segment);

			if (segmentName.equals(markerName) ||
				segmentName.endsWith(markerName) ||
				markerName.startsWith(WordUtil.toSingular(segmentName))) {

				return true;
			}
		}

		return false;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		MCPToolFactoryUtil.class);

}