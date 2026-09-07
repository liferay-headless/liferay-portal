/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.mcp.server.rest.dto.v1_0.Tool;
import com.liferay.mcp.server.rest.dto.v1_0.ToolSet;
import com.liferay.mcp.server.rest.dto.v1_0.ToolSummary;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.vulcan.http.VulcanRequestForwarder;
import com.liferay.portal.vulcan.jackson.databind.ObjectMapperProviderUtil;
import com.liferay.portal.vulcan.pagination.Page;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Alejandro Tardín
 */
public class ToolSetUtil {

	public static Tool getTool(
		HttpServletRequest httpServletRequest, boolean requiredInputSchemaOnly,
		String toolName, String toolSetName) {

		Tool tool = OpenAPIUtil.getTool(
			!Objects.equals(toolSetName, _TOOL_SET_NAME),
			OpenAPIBriefUtil.getOpenAPIJSONObject(
				httpServletRequest,
				OpenAPIBriefUtil.getOpenAPIBrief(toolSetName)),
			toolName);

		if (!requiredInputSchemaOnly) {
			return tool;
		}

		Map<String, Object> inputSchema = toRequiredPropertiesOnly(
			tool.getInputSchema());

		tool.setInputSchema(() -> inputSchema);

		return tool;
	}

	public static Page<ToolSet> getToolSetsPage() {
		Map<String, OpenAPIBrief> openAPIBriefs =
			OpenAPIBriefUtil.getOpenAPIBriefs();

		return Page.of(
			TransformUtil.transform(
				openAPIBriefs.entrySet(),
				entry -> new ToolSet() {
					{
						setDescription(
							() -> {
								OpenAPIBrief openAPIBrief = entry.getValue();

								return openAPIBrief.getDescription();
							});

						setName(entry::getKey);
					}
				}));
	}

	public static Page<ToolSummary> getToolSummariesPage(
		HttpServletRequest httpServletRequest, String toolSetName) {

		return Page.of(
			OpenAPIUtil.getToolSummaries(
				OpenAPIBriefUtil.getOpenAPIJSONObject(
					httpServletRequest,
					OpenAPIBriefUtil.getOpenAPIBrief(toolSetName))));
	}

	public static Response invokeTool(
			List<String> dataMaskExternalReferenceCodes,
			HttpServletRequest httpServletRequest, Object inputObject,
			String toolName, String toolSetName)
		throws Exception {

		JSONObject inputJSONObject = null;

		if (inputObject instanceof JSONObject) {
			inputJSONObject = (JSONObject)inputObject;
		}
		else if (inputObject instanceof Map) {
			inputJSONObject = JSONFactoryUtil.createJSONObject(
				(Map<String, ?>)inputObject);
		}
		else {
			inputJSONObject = JSONFactoryUtil.createJSONObject();
		}

		if (Objects.equals(toolSetName, _TOOL_SET_NAME)) {
			Response response = _invoke(
				dataMaskExternalReferenceCodes, httpServletRequest,
				inputJSONObject, toolName);

			if (response != null) {
				return response;
			}
		}

		VulcanRequestForwarder vulcanRequestForwarder =
			_vulcanRequestForwarderSnapshot.get();

		OpenAPIBrief openAPIBrief = OpenAPIBriefUtil.getOpenAPIBrief(
			toolSetName);

		VulcanRequestForwarder.Response response =
			vulcanRequestForwarder.forward(
				httpServletRequest,
				OpenAPIUtil.getRequest(
					openAPIBrief.getBasePath(),
					HashMapBuilder.put(
						"X-Liferay-Data-Masks",
						() -> StringUtil.merge(
							dataMaskExternalReferenceCodes, StringPool.COMMA)
					).build(),
					inputJSONObject,
					OpenAPIBriefUtil.getOpenAPIJSONObject(
						httpServletRequest, openAPIBrief),
					toolName, _getUser(httpServletRequest)));

		String content = response.getContent();

		if (_isUnsupportedOperationException(
				content, response.getStatusCode())) {

			return _toUnsupportedResponse(content, toolName);
		}

		return Response.status(
			response.getStatusCode()
		).entity(
			Validator.isNull(content) ? null : _getContent(content)
		).type(
			ContentTypes.TEXT_PLAIN_UTF8
		).build();
	}

	public static Map<String, Object> toRequiredPropertiesOnly(
		Map<String, ?> schema) {

		return _toRequiredPropertiesOnly(schema, 0);
	}

	private static String _getContent(String content) {
		if (Validator.isNull(content) || (content.charAt(0) != '{') ||
			!content.contains("\"actions\"")) {

			return content;
		}

		try {
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(content);

			if (!jsonObject.has("actions")) {
				return content;
			}

			jsonObject.remove("actions");

			return jsonObject.toString();
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			return content;
		}
	}

	private static Response _getResponse(Object value) throws Exception {
		ObjectMapper objectMapper = ObjectMapperProviderUtil.getObjectMapper();

		return Response.ok(
			objectMapper.writeValueAsString(value), ContentTypes.TEXT_PLAIN_UTF8
		).build();
	}

	private static String _getUnsupportedReason(String content) {
		try {
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(content);

			String title = jsonObject.getString("title");

			if (Validator.isBlank(title)) {
				return null;
			}

			return StringUtil.removeLast(StringUtil.trim(title), ".");
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			return null;
		}
	}

	private static User _getUser(HttpServletRequest httpServletRequest) {
		return UserLocalServiceUtil.fetchUser(
			GetterUtil.getLong(
				httpServletRequest.getAttribute(WebKeys.USER_ID)));
	}

	private static Response _invoke(
			List<String> dataMaskExternalReferenceCodes,
			HttpServletRequest httpServletRequest, JSONObject inputJSONObject,
			String toolName)
		throws Exception {

		if (Objects.equals(toolName, "getToolSearchPage")) {
			return _getResponse(
				SearchToolUtil.getSearchToolsPage(
					httpServletRequest,
					inputJSONObject.getBoolean("includeRequiredInputSchema"),
					inputJSONObject.getInt("limit"),
					inputJSONObject.getString("search")));
		}

		if (Objects.equals(toolName, "getToolSetToolSetNameTool")) {
			return _getResponse(
				getTool(
					httpServletRequest,
					inputJSONObject.getBoolean("requiredInputSchemaOnly"),
					inputJSONObject.getString("toolName"),
					inputJSONObject.getString("toolSetName")));
		}

		if (Objects.equals(
				toolName, "getToolSetToolSetNameToolSummariesPage")) {

			return _getResponse(
				getToolSummariesPage(
					httpServletRequest,
					inputJSONObject.getString("toolSetName")));
		}

		if (Objects.equals(toolName, "getToolSetsPage")) {
			return _getResponse(getToolSetsPage());
		}

		if (Objects.equals(toolName, "postToolSetToolSetNameToolInvoke")) {
			String nestedToolName = inputJSONObject.getString("toolName");

			if (Objects.equals(
					nestedToolName, "postToolSetToolSetNameToolInvoke") &&
				Objects.equals(
					inputJSONObject.getString("toolSetName"), _TOOL_SET_NAME)) {

				throw new IllegalArgumentException(
					StringBundler.concat(
						"The \"", toolName, "\" tool cannot invoke itself. ",
						"Pass the tool to run as \"toolName\" and its tool ",
						"set as \"toolSetName\"."));
			}

			return invokeTool(
				dataMaskExternalReferenceCodes, httpServletRequest,
				inputJSONObject.opt("body"), nestedToolName,
				inputJSONObject.getString("toolSetName"));
		}

		return null;
	}

	private static boolean _isUnsupportedOperationException(
		String content, int statusCode) {

		if ((statusCode != Response.Status.BAD_REQUEST.getStatusCode()) ||
			Validator.isNull(content)) {

			return false;
		}

		try {
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(content);

			return Objects.equals(
				jsonObject.getString("type"), "UnsupportedOperationException");
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			return false;
		}
	}

	private static Map<String, Object> _toNamedProperties(
		Map<String, Object> schema) {

		Map<String, Object> properties = (Map<String, Object>)schema.get(
			"properties");

		if (properties == null) {
			return schema;
		}

		Map<String, Object> namedProperties = new LinkedHashMap<>();

		for (Map.Entry<String, Object> entry : properties.entrySet()) {
			Object value = entry.getValue();

			if (value instanceof Map) {
				Map<String, Object> valueMap = (Map<String, Object>)value;

				if (valueMap.containsKey("properties") ||
					valueMap.containsKey("items")) {

					value = HashMapBuilder.<String, Object>put(
						"type", valueMap.get("type")
					).build();
				}
			}

			namedProperties.put(entry.getKey(), value);
		}

		return HashMapBuilder.<String, Object>putAll(
			schema
		).put(
			"properties", namedProperties
		).build();
	}

	private static Map<String, Object> _toRequiredPropertiesOnly(
		Map<String, ?> schema, int depth) {

		if ((schema == null) || (depth > _MAX_SCHEMA_DEPTH)) {
			return null;
		}

		List<Object> requiredPropertyNames = (List<Object>)schema.get(
			"required");

		Map<String, Object> properties = (Map<String, Object>)schema.get(
			"properties");

		if (ListUtil.isEmpty(requiredPropertyNames) || (properties == null)) {
			return null;
		}

		Map<String, Object> requiredProperties = new LinkedHashMap<>();

		for (Object object : requiredPropertyNames) {
			String requiredPropertyName = String.valueOf(object);

			Object property = properties.get(requiredPropertyName);

			if (!(property instanceof Map)) {
				continue;
			}

			Map<String, Object> propertyMap = (Map<String, Object>)property;

			Map<String, Object> nested = _toRequiredPropertiesOnly(
				propertyMap, depth + 1);

			if (nested != null) {
				propertyMap = HashMapBuilder.<String, Object>putAll(
					nested
				).put(
					"description", propertyMap.get("description")
				).build();
			}
			else if (propertyMap.containsKey("properties")) {
				propertyMap = _toNamedProperties(propertyMap);
			}

			requiredProperties.put(requiredPropertyName, propertyMap);
		}

		return HashMapBuilder.<String, Object>put(
			"properties", requiredProperties
		).put(
			"required", requiredPropertyNames
		).put(
			"type", schema.get("type")
		).build();
	}

	private static Response _toUnsupportedResponse(
		String content, String toolName) {

		StringBundler sb = new StringBundler(5);

		String reason = _getUnsupportedReason(content);

		if (reason != null) {
			sb.append(reason);
			sb.append(". ");
		}

		sb.append("\"");
		sb.append(toolName);

		sb.append(
			StringBundler.concat(
				"\" is unavailable on this instance. Stop this line of work ",
				"now. Do not call it again, do not try an operation elsewhere ",
				"whose name looks like it, and do not try the batch form: all ",
				"of them reach the same switch. Tell the user which feature ",
				"is turned off and which step was left undone because of it."));

		return Response.status(
			Response.Status.BAD_REQUEST
		).entity(
			sb.toString()
		).type(
			ContentTypes.TEXT_PLAIN_UTF8
		).build();
	}

	private static final int _MAX_SCHEMA_DEPTH = 4;

	private static final String _TOOL_SET_NAME = "mcp-server-v1.0";

	private static final Log _log = LogFactoryUtil.getLog(ToolSetUtil.class);

	private static final Snapshot<VulcanRequestForwarder>
		_vulcanRequestForwarderSnapshot = new Snapshot<>(
			ToolSetUtil.class, VulcanRequestForwarder.class);

}