/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index.util;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Petteri Karttunen
 */
public class SchemaUtil {

	public static Set<String> getEnvelopePropertyNames(
		Map<String, Integer> counts, int size) {

		Set<String> envelopePropertyNames = new HashSet<>();

		double threshold = size * _ENVELOPE_SHARE;

		for (Map.Entry<String, Integer> entry : counts.entrySet()) {
			if (entry.getValue() > threshold) {
				envelopePropertyNames.add(entry.getKey());
			}
		}

		return envelopePropertyNames;
	}

	public static String[] getIndexableSchemaProperties(
		Set<String> envelopePropertyNames, String[] schemaProperties) {

		List<String> indexableSchemaProperties = new ArrayList<>(
			schemaProperties.length);

		for (String schemaProperty : schemaProperties) {
			if (!envelopePropertyNames.contains(schemaProperty)) {
				indexableSchemaProperties.add(schemaProperty);
			}
		}

		return indexableSchemaProperties.toArray(new String[0]);
	}

	public static String[] getParameters(
		JSONObject operationJSONObject, JSONObject pathItemJSONObject) {

		Set<String> parameters = new LinkedHashSet<>();

		_collectParameters(
			parameters, operationJSONObject.getJSONArray("parameters"));
		_collectParameters(
			parameters, pathItemJSONObject.getJSONArray("parameters"));

		return parameters.toArray(new String[0]);
	}

	public static String[] getRequiredReferences(
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

			String segment = _getReferenceSegment(
				propertyJSONObject.getString("description"), propertyName);

			if (segment != null) {
				requiredReferences.add(
					propertyName + StringPool.POUND + segment);
			}
		}

		return requiredReferences.toArray(new String[0]);
	}

	public static String[] getSchemaProperties(
		JSONObject openAPIJSONObject, JSONObject operationJSONObject) {

		Set<String> propertyNames = new LinkedHashSet<>();

		_collectPropertyNames(
			_getContentSchemaJSONObject(
				operationJSONObject.getJSONObject("requestBody")),
			openAPIJSONObject, propertyNames);

		_collectPropertyNames(
			_getContentSchemaJSONObject(
				_getSuccessResponseJSONObject(
					operationJSONObject.getJSONObject("responses"))),
			openAPIJSONObject, propertyNames);

		return propertyNames.toArray(new String[0]);
	}

	public static Map<String, Map<String, Integer>> getSchemaPropertyCounts(
		Collection<MCPTool> mcpTools) {

		Map<String, Map<String, Integer>> schemaPropertyCounts =
			new HashMap<>();

		for (MCPTool mcpTool : mcpTools) {
			Map<String, Integer> counts = schemaPropertyCounts.computeIfAbsent(
				mcpTool.getToolSetName(), key -> new HashMap<>());

			for (String schemaProperty : mcpTool.getSchemaProperties()) {
				Integer count = counts.get(schemaProperty);

				counts.put(schemaProperty, (count == null) ? 1 : count + 1);
			}
		}

		return schemaPropertyCounts;
	}

	public static Map<String, Integer> getSchemaPropertyTotalCounts(
		Map<String, Map<String, Integer>> schemaPropertyCounts) {

		Map<String, Integer> counts = new HashMap<>();

		for (Map<String, Integer> toolSetCounts :
				schemaPropertyCounts.values()) {

			for (Map.Entry<String, Integer> entry : toolSetCounts.entrySet()) {
				Integer count = counts.get(entry.getKey());

				counts.put(
					entry.getKey(),
					(count == null) ? entry.getValue() :
						count + entry.getValue());
			}
		}

		return counts;
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
		JSONObject jsonObject, JSONObject openAPIJSONObject,
		Set<String> propertyNames) {

		if (jsonObject == null) {
			return;
		}

		JSONObject itemsJSONObject = jsonObject.getJSONObject("items");

		if (itemsJSONObject != null) {
			_collectPropertyNames(
				itemsJSONObject, openAPIJSONObject, propertyNames);

			return;
		}

		String ref = jsonObject.getString("$ref");

		if (Validator.isNotNull(ref)) {
			jsonObject = _getComponentSchemaJSONObject(openAPIJSONObject, ref);

			if (jsonObject == null) {
				return;
			}
		}

		JSONObject propertiesJSONObject = jsonObject.getJSONObject(
			"properties");

		if (propertiesJSONObject == null) {
			return;
		}

		if (_isPageProperties(propertiesJSONObject)) {
			_collectPropertyNames(
				propertiesJSONObject.getJSONObject("items"), openAPIJSONObject,
				propertyNames);

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
		JSONObject jsonObject) {

		if (jsonObject == null) {
			return null;
		}

		JSONObject contentJSONObject = jsonObject.getJSONObject("content");

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

	private static String _getReferenceSegment(
		String description, String propertyName) {

		String entity = null;

		if (Validator.isNotNull(description)) {
			Matcher matcher = _referencePattern.matcher(description);

			if (matcher.find()) {
				entity = matcher.group(1);
			}
		}

		if (entity == null) {
			for (String suffix : _REFERENCE_SUFFIXES) {
				if (propertyName.endsWith(suffix) &&
					(propertyName.length() > suffix.length())) {

					entity = propertyName.substring(
						0, propertyName.length() - suffix.length());

					break;
				}
			}
		}

		if (entity == null) {
			return null;
		}

		String words = StringUtil.toLowerCase(WordUtil.humanize(entity));

		return StringUtil.replace(
			WordUtil.toPlural(StringUtil.trim(words)), CharPool.SPACE,
			CharPool.DASH);
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

	private static boolean _isPageProperties(JSONObject propertiesJSONObject) {
		if (propertiesJSONObject.has("items") &&
			propertiesJSONObject.has("pageSize") &&
			propertiesJSONObject.has("totalCount")) {

			return true;
		}

		return false;
	}

	private static final double _ENVELOPE_SHARE = 0.05;

	private static final String[] _REFERENCE_SUFFIXES = {
		"ExternalReferenceCode", "Key", "Id"
	};

	private static final String[] _SUCCESS_STATUSES = {"200", "201", "default"};

	private static final Pattern _referencePattern = Pattern.compile(
		"`([A-Z]\\w+)`");

}