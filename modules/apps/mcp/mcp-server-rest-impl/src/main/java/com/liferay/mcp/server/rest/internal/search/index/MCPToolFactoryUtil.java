/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index;

import com.liferay.mcp.server.rest.internal.search.index.constants.MCPToolConstants;
import com.liferay.mcp.server.rest.internal.search.index.constants.MCPToolOperationVariants;
import com.liferay.mcp.server.rest.internal.search.index.util.ExpansionUtil;
import com.liferay.mcp.server.rest.internal.search.index.util.IntentUtil;
import com.liferay.mcp.server.rest.internal.search.index.util.WordUtil;
import com.liferay.mcp.server.rest.internal.util.OpenAPIUtil;
import com.liferay.mcp.server.rest.internal.util.ToolSetUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.application.HeadlessApplicationProvider;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Petteri Karttunen
 */
public class MCPToolFactoryUtil {

	public static List<MCPTool> getMCPTools(
		Set<String> failedToolSetNames, HttpServletRequest httpServletRequest,
		Map<String, HeadlessApplicationProvider.OpenAPIDocument>
			openAPIDocuments,
		Set<String> toolSetNames) {

		List<MCPTool> mcpTools = new ArrayList<>();

		for (String toolSetName : toolSetNames) {
			HeadlessApplicationProvider.OpenAPIDocument openAPIDocument =
				openAPIDocuments.get(toolSetName);

			if (openAPIDocument == null) {
				continue;
			}

			try {
				_addMCPTools(
					httpServletRequest, mcpTools, openAPIDocument, toolSetName);
			}
			catch (Exception exception) {
				failedToolSetNames.add(toolSetName);

				_log.error(
					"Unable to build the tools of tool set \"" + toolSetName +
						"\"",
					exception);
			}
		}

		return mcpTools;
	}

	private static void _addMCPTools(
		HttpServletRequest httpServletRequest, List<MCPTool> mcpTools,
		HeadlessApplicationProvider.OpenAPIDocument openAPIDocument,
		String toolSetName) {

		JSONObject openAPIJSONObject = ToolSetUtil.getOpenAPIJSONObject(
			httpServletRequest, toolSetName);

		JSONObject pathsJSONObject = openAPIJSONObject.getJSONObject("paths");

		if (pathsJSONObject == null) {
			return;
		}

		HeadlessApplicationProvider.Application application =
			openAPIDocument.getApplication();

		for (String path : pathsJSONObject.keySet()) {
			JSONObject pathItemJSONObject = pathsJSONObject.getJSONObject(path);

			for (String method : OpenAPIUtil.METHODS) {
				MCPTool mcpTool = _getMCPTool(
					application, method, openAPIJSONObject, path,
					pathItemJSONObject, toolSetName);

				if (mcpTool != null) {
					mcpTools.add(mcpTool);
				}
			}
		}
	}

	private static void _collectParameters(
		Set<String> parameters, JSONArray parametersJSONArray) {

		if (parametersJSONArray == null) {
			return;
		}

		for (int i = 0; i < parametersJSONArray.length(); i++) {
			JSONObject parameterJSONObject = parametersJSONArray.getJSONObject(
				i);

			if (parameterJSONObject == null) {
				continue;
			}

			String name = parameterJSONObject.getString("name");

			if (Validator.isNotNull(name)) {
				parameters.add(name);
			}
		}
	}

	private static void _collectPropertyNames(
		JSONObject openAPIJSONObject, Set<String> propertyNames,
		JSONObject schemaJSONObject) {

		if (schemaJSONObject == null) {
			return;
		}

		JSONObject itemsJSONObject = schemaJSONObject.getJSONObject("items");

		if (itemsJSONObject != null) {
			_collectPropertyNames(
				openAPIJSONObject, propertyNames, itemsJSONObject);

			return;
		}

		String ref = schemaJSONObject.getString("$ref");

		if (Validator.isNotNull(ref)) {
			schemaJSONObject = _getComponentSchemaJSONObject(
				openAPIJSONObject, ref);

			if (schemaJSONObject == null) {
				return;
			}
		}

		JSONObject propertiesJSONObject = schemaJSONObject.getJSONObject(
			"properties");

		if (propertiesJSONObject == null) {
			return;
		}

		if (_isPageProperties(propertiesJSONObject)) {
			_collectPropertyNames(
				openAPIJSONObject, propertyNames,
				propertiesJSONObject.getJSONObject("items"));

			return;
		}

		for (String propertyName : propertiesJSONObject.keySet()) {
			if (propertyName.startsWith("x-") ||
				_isEntityReference(
					propertiesJSONObject.getJSONObject(propertyName))) {

				continue;
			}

			propertyNames.add(propertyName);
		}
	}

	private static String _getActionMarker(
		String method, String path, String toolName) {

		if (Objects.equals(method, "get") || Objects.equals(method, "head") ||
			Objects.equals(method, "options")) {

			return null;
		}

		for (String actionMarker :
				MCPToolConstants.actionMarkerVerbs.keySet()) {

			if (toolName.endsWith(actionMarker) &&
				_hasActionMarkerSegment(actionMarker, path)) {

				return actionMarker;
			}
		}

		return null;
	}

	private static JSONObject _getComponentSchemaJSONObject(
		JSONObject openAPIJSONObject, String ref) {

		JSONObject componentsJSONObject = openAPIJSONObject.getJSONObject(
			"components");

		if (componentsJSONObject == null) {
			return null;
		}

		JSONObject schemasJSONObject = componentsJSONObject.getJSONObject(
			"schemas");

		if (schemasJSONObject == null) {
			return null;
		}

		int index = ref.lastIndexOf(CharPool.SLASH);

		if (index < 0) {
			return null;
		}

		return schemasJSONObject.getJSONObject(ref.substring(index + 1));
	}

	private static JSONObject _getContentSchemaJSONObject(
		JSONObject requestBodyOrResponseJSONObject) {

		if (requestBodyOrResponseJSONObject == null) {
			return null;
		}

		JSONObject contentJSONObject =
			requestBodyOrResponseJSONObject.getJSONObject("content");

		if (contentJSONObject == null) {
			return null;
		}

		for (String mediaType : contentJSONObject.keySet()) {
			JSONObject mediaTypeJSONObject = contentJSONObject.getJSONObject(
				mediaType);

			if (mediaTypeJSONObject == null) {
				continue;
			}

			JSONObject schemaJSONObject = mediaTypeJSONObject.getJSONObject(
				"schema");

			if (schemaJSONObject != null) {
				return schemaJSONObject;
			}
		}

		return null;
	}

	private static String _getDescription(JSONObject operationJSONObject) {
		String description = operationJSONObject.getString("description");
		String summary = operationJSONObject.getString("summary");

		if (!Validator.isBlank(description) && !Validator.isBlank(summary)) {
			if (StringUtil.endsWith(summary, CharPool.PERIOD)) {
				return summary + StringPool.SPACE + description;
			}

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

	private static String _getEntityWords(JSONObject operationJSONObject) {
		JSONArray tagsJSONArray = operationJSONObject.getJSONArray("tags");

		if (tagsJSONArray == null) {
			return StringPool.BLANK;
		}

		StringBundler sb = new StringBundler(tagsJSONArray.length() * 2);

		for (int i = 0; i < tagsJSONArray.length(); i++) {
			sb.append(WordUtil.toWords(tagsJSONArray.getString(i)));
			sb.append(StringPool.SPACE);
		}

		return sb.toString();
	}

	private static String _getIdentifierType(String path) {
		String[] segments = StringUtil.split(path, CharPool.SLASH);

		for (int i = segments.length - 1; i >= 0; i--) {
			String segment = segments[i];

			if (!StringUtil.startsWith(
					StringUtil.toLowerCase(segment), "by-")) {

				continue;
			}

			return StringUtil.replace(
				WordUtil.toWords(segment.substring(3)), CharPool.SPACE,
				CharPool.DASH);
		}

		return StringPool.BLANK;
	}

	private static MCPTool _getMCPTool(
		HeadlessApplicationProvider.Application application, String method,
		JSONObject openAPIJSONObject, String path,
		JSONObject pathItemJSONObject, String toolSetName) {

		JSONObject operationJSONObject = pathItemJSONObject.getJSONObject(
			method);

		if (operationJSONObject == null) {
			return null;
		}

		String toolName = operationJSONObject.getString("operationId");

		if (Validator.isBlank(toolName)) {
			return null;
		}

		String actionMarker = _getActionMarker(method, path, toolName);

		String entityName = _getEntityName(operationJSONObject);

		String operationVariant = _getOperationVariant(entityName, path);

		if (Objects.equals(
				operationVariant,
				MCPToolOperationVariants.OPERATION_VARIANT_TRAVERSAL)) {

			entityName = _getTraversalEntityName(entityName, path, toolName);
		}

		boolean batch = Objects.equals(
			operationVariant, MCPToolOperationVariants.OPERATION_VARIANT_BATCH);

		String[] expansions = ExpansionUtil.getExpansions(
			actionMarker, batch, _getEntityWords(operationJSONObject), method,
			path, toolName);

		String identifierType = _getIdentifierType(path);
		String intent = IntentUtil.getIntent(actionMarker, method, toolName);
		String[] parameters = _getParameters(
			operationJSONObject, pathItemJSONObject);
		String toolPath = Portal.PATH_MODULE + application.getBasePath() + path;
		String[] requiredReferences = _getRequiredReferences(
			openAPIJSONObject, operationJSONObject, path);
		String[] schemaProperties = _getSchemaProperties(
			openAPIJSONObject, operationJSONObject);

		return new MCPTool(
			operationJSONObject.getBoolean("deprecated"),
			StringUtil.trim(_getDescription(operationJSONObject)), entityName,
			expansions, identifierType, intent, method, operationVariant,
			parameters, toolPath, requiredReferences, schemaProperties,
			toolName, toolSetName);
	}

	private static String _getOperationVariant(String entityName, String path) {
		String operationVariant = _getPathSegmentOperationVariant(path);

		if (Validator.isNotNull(operationVariant)) {
			return operationVariant;
		}

		if (_isTraversal(entityName, path)) {
			return MCPToolOperationVariants.OPERATION_VARIANT_TRAVERSAL;
		}

		return StringPool.BLANK;
	}

	private static String[] _getParameters(
		JSONObject operationJSONObject, JSONObject pathItemJSONObject) {

		Set<String> parameters = new LinkedHashSet<>();

		_collectParameters(
			parameters, operationJSONObject.getJSONArray("parameters"));
		_collectParameters(
			parameters, pathItemJSONObject.getJSONArray("parameters"));

		return parameters.toArray(new String[0]);
	}

	private static String _getPathSegmentOperationVariant(String path) {
		String[] segments = StringUtil.split(path, CharPool.SLASH);

		for (int i = segments.length - 1; i >= 0; i--) {
			String segment = StringUtil.toLowerCase(segments[i]);

			if (Validator.isNull(segment) ||
				OpenAPIUtil.isPathParameter(segment)) {

				continue;
			}

			int index = segment.indexOf(CharPool.PERIOD);

			if (index > 0) {
				segment = segment.substring(0, index);
			}

			String operationVariant =
				MCPToolOperationVariants.pathSegmentOperationVariants.get(
					StringUtil.removeLast(segment, "-replace"));

			if (operationVariant != null) {
				return operationVariant;
			}
		}

		int byExternalReferenceCodeSegmentsCount = 0;

		for (String segment : segments) {
			if (StringUtil.startsWith(
					StringUtil.toLowerCase(segment), "by-external")) {

				byExternalReferenceCodeSegmentsCount++;
			}
		}

		if (byExternalReferenceCodeSegmentsCount > 1) {
			return MCPToolOperationVariants.OPERATION_VARIANT_NESTED;
		}

		return StringPool.BLANK;
	}

	private static String _getReferenceCollectionSegment(
		String description, String propertyName) {

		String referencedEntityName = null;

		if (Validator.isNotNull(description)) {
			Matcher matcher = _referencePattern.matcher(description);

			if (matcher.find()) {
				referencedEntityName = matcher.group(1);
			}
		}

		if (referencedEntityName == null) {
			for (String suffix : _REFERENCE_PROPERTY_NAME_SUFFIXES) {
				if (propertyName.endsWith(suffix) &&
					(propertyName.length() > suffix.length())) {

					referencedEntityName = propertyName.substring(
						0, propertyName.length() - suffix.length());

					break;
				}
			}
		}

		if (referencedEntityName == null) {
			return null;
		}

		String referencedEntityWords = WordUtil.toWords(referencedEntityName);

		return StringUtil.replace(
			WordUtil.toPlural(StringUtil.trim(referencedEntityWords)),
			CharPool.SPACE, CharPool.DASH);
	}

	private static String[] _getRequiredReferences(
		JSONObject openAPIJSONObject, JSONObject operationJSONObject,
		String path) {

		JSONObject contentSchemaJSONObject = _getContentSchemaJSONObject(
			operationJSONObject.getJSONObject("requestBody"));

		if (contentSchemaJSONObject == null) {
			return new String[0];
		}

		String ref = contentSchemaJSONObject.getString("$ref");

		if (Validator.isNotNull(ref)) {
			contentSchemaJSONObject = _getComponentSchemaJSONObject(
				openAPIJSONObject, ref);
		}

		if (contentSchemaJSONObject == null) {
			return new String[0];
		}

		JSONArray requiredJSONArray = contentSchemaJSONObject.getJSONArray(
			"required");

		JSONObject propertiesJSONObject = contentSchemaJSONObject.getJSONObject(
			"properties");

		if ((requiredJSONArray == null) || (propertiesJSONObject == null)) {
			return new String[0];
		}

		List<String> requiredReferences = new ArrayList<>();

		for (int i = 0; i < requiredJSONArray.length(); i++) {
			String propertyName = requiredJSONArray.getString(i);

			if ((propertyName == null) || path.contains("{" + propertyName)) {
				continue;
			}

			JSONObject propertyJSONObject = propertiesJSONObject.getJSONObject(
				propertyName);

			if (propertyJSONObject == null) {
				continue;
			}

			String collectionSegment = _getReferenceCollectionSegment(
				propertyJSONObject.getString("description"), propertyName);

			if (collectionSegment != null) {
				requiredReferences.add(
					propertyName + StringPool.POUND + collectionSegment);
			}
		}

		return requiredReferences.toArray(new String[0]);
	}

	private static String[] _getSchemaProperties(
		JSONObject openAPIJSONObject, JSONObject operationJSONObject) {

		Set<String> propertyNames = new LinkedHashSet<>();

		_collectPropertyNames(
			openAPIJSONObject, propertyNames,
			_getContentSchemaJSONObject(
				operationJSONObject.getJSONObject("requestBody")));
		_collectPropertyNames(
			openAPIJSONObject, propertyNames,
			_getContentSchemaJSONObject(
				_getSuccessResponseJSONObject(
					operationJSONObject.getJSONObject("responses"))));

		return propertyNames.toArray(new String[0]);
	}

	private static JSONObject _getSuccessResponseJSONObject(
		JSONObject responsesJSONObject) {

		if (responsesJSONObject == null) {
			return null;
		}

		for (String status : _SUCCESS_STATUSES) {
			JSONObject responseJSONObject = responsesJSONObject.getJSONObject(
				status);

			if (responseJSONObject != null) {
				return responseJSONObject;
			}
		}

		return null;
	}

	private static String _getTraversalEntityName(
		String entityName, String path, String toolName) {

		String normalizedToolName = WordUtil.normalize(toolName);

		if (!normalizedToolName.contains(WordUtil.normalize(entityName))) {
			return entityName;
		}

		String[] segments = StringUtil.split(path, CharPool.SLASH);

		for (int i = segments.length - 1; i >= 0; i--) {
			String segment = segments[i];

			if (OpenAPIUtil.isPathParameter(segment) ||
				StringUtil.startsWith(segment, "by-") ||
				MCPToolOperationVariants.pathSegmentOperationVariants.
					containsKey(segment) ||
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

	private static boolean _hasActionMarkerSegment(
		String actionMarker, String path) {

		String normalizedActionMarker = WordUtil.normalize(
			StringUtil.removeLast(actionMarker, "Page"));

		for (String segment : StringUtil.split(path, CharPool.SLASH)) {
			if (Validator.isNull(segment) ||
				OpenAPIUtil.isPathParameter(segment)) {

				continue;
			}

			int index = segment.indexOf(CharPool.PERIOD);

			if (index > 0) {
				segment = segment.substring(0, index);
			}

			String normalizedSegment = WordUtil.normalize(segment);

			if (normalizedSegment.equals(normalizedActionMarker) ||
				normalizedSegment.endsWith(normalizedActionMarker) ||
				normalizedActionMarker.startsWith(
					WordUtil.toSingular(normalizedSegment))) {

				return true;
			}
		}

		return false;
	}

	private static boolean _isEntityReference(JSONObject propertyJSONObject) {
		if (propertyJSONObject == null) {
			return false;
		}

		if (Validator.isNotNull(propertyJSONObject.getString("$ref"))) {
			return true;
		}

		JSONObject itemsJSONObject = propertyJSONObject.getJSONObject("items");

		if ((itemsJSONObject != null) &&
			Validator.isNotNull(itemsJSONObject.getString("$ref"))) {

			return true;
		}

		return false;
	}

	private static boolean _isEntitySegment(String entityName, String segment) {
		String normalizedSegment = WordUtil.normalize(segment);

		if (normalizedSegment.equals(WordUtil.normalize(entityName)) ||
			normalizedSegment.equals(
				WordUtil.normalize(WordUtil.toPlural(entityName)))) {

			return true;
		}

		return false;
	}

	private static boolean _isPageProperties(JSONObject propertiesJSONObject) {
		if (propertiesJSONObject.has("items") &&
			propertiesJSONObject.has("pageSize") &&
			propertiesJSONObject.has("totalCount")) {

			return true;
		}

		return false;
	}

	private static boolean _isTraversal(String entityName, String path) {
		if (Validator.isNull(entityName)) {
			return false;
		}

		List<String> segmentsAfterParameter = new ArrayList<>();

		boolean parameterSeen = false;

		for (String segment : StringUtil.split(path, CharPool.SLASH)) {
			if (Validator.isNull(segment)) {
				continue;
			}

			if (OpenAPIUtil.isPathParameter(segment)) {
				parameterSeen = true;

				continue;
			}

			if (parameterSeen) {
				segmentsAfterParameter.add(segment);
			}
		}

		if (segmentsAfterParameter.isEmpty()) {
			return false;
		}

		String segment = segmentsAfterParameter.get(
			segmentsAfterParameter.size() - 1);

		if (_isEntitySegment(entityName, segment)) {
			return false;
		}

		if (WordUtil.isPlural(segment)) {
			return true;
		}

		if (segmentsAfterParameter.size() < 2) {
			return false;
		}

		segment = segmentsAfterParameter.get(segmentsAfterParameter.size() - 2);

		if (WordUtil.isPlural(segment) &&
			!_isEntitySegment(entityName, segment)) {

			return true;
		}

		return false;
	}

	private static final String[] _REFERENCE_PROPERTY_NAME_SUFFIXES = {
		"ExternalReferenceCode", "Key", "Id"
	};

	private static final String[] _SUCCESS_STATUSES = {"200", "201", "default"};

	private static final Log _log = LogFactoryUtil.getLog(
		MCPToolFactoryUtil.class);

	private static final Pattern _referencePattern = Pattern.compile(
		"`([A-Z]\\w+)`");

}