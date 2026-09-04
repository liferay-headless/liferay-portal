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
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.vulcan.http.VulcanRequestForwarder;
import com.liferay.portal.vulcan.jackson.databind.ObjectMapperProviderUtil;
import com.liferay.portal.vulcan.pagination.Page;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jaxrs.runtime.JaxrsServiceRuntime;
import org.osgi.service.jaxrs.runtime.dto.ApplicationDTO;
import org.osgi.service.jaxrs.runtime.dto.ResourceDTO;
import org.osgi.service.jaxrs.runtime.dto.ResourceMethodInfoDTO;
import org.osgi.service.jaxrs.runtime.dto.RuntimeDTO;

/**
 * @author Alejandro Tardín
 */
public class ToolSetUtil {

	public static void clearNumberOfTools(long companyId, String toolSetName) {
		Map<String, Integer> numberOfToolsMap = _numberOfToolsMaps.get(
			companyId);

		if (numberOfToolsMap != null) {
			numberOfToolsMap.remove(toolSetName);
		}
	}

	public static void clearNumberOfToolsMapCache(long companyId) {
		_numberOfToolsMaps.remove(companyId);
	}

	public static void clearOpenAPIJSONObjectCache(long companyId) {
		Set<String> keys = _openAPIJSONObjects.keySet();

		keys.removeIf(key -> key.startsWith(companyId + StringPool.POUND));
	}

	public static Tool getTool(
		HttpServletRequest httpServletRequest, String toolName,
		String toolSetName) {

		return OpenAPIUtil.getTool(
			!Objects.equals(toolSetName, _TOOL_SET_NAME),
			_getOpenAPIJSONObject(
				httpServletRequest, _getOpenAPIBrief(toolSetName)),
			toolName);
	}

	public static String getToolSetName(String restContextPath) {
		if (Validator.isNull(restContextPath)) {
			return null;
		}

		String path = restContextPath;

		if (path.startsWith(Portal.PATH_MODULE)) {
			path = path.substring(Portal.PATH_MODULE.length());
		}

		if (path.startsWith(StringPool.SLASH)) {
			path = path.substring(1);
		}

		Map<String, OpenAPIBrief> openAPIBriefs = _getOpenAPIBriefs();

		String[] parts = StringUtil.split(path, CharPool.SLASH);

		for (int i = parts.length; i > 0; i--) {
			String toolSetName = StringUtil.merge(
				ArrayUtil.subset(parts, 0, i), StringPool.DASH);

			if (openAPIBriefs.containsKey(toolSetName)) {
				return toolSetName;
			}
		}

		return null;
	}

	public static Page<ToolSet> getToolSetsPage(
		HttpServletRequest httpServletRequest) {

		Map<String, OpenAPIBrief> openAPIBriefs = _getOpenAPIBriefs();

		Map<String, Integer> numberOfToolsMap = _getNumberOfToolsMap(
			httpServletRequest, openAPIBriefs);

		return Page.of(
			TransformUtil.transform(
				openAPIBriefs.entrySet(),
				entry -> new ToolSet() {
					{
						setDescription(
							() -> {
								OpenAPIBrief openAPIBrief = entry.getValue();

								return openAPIBrief._description;
							});

						setName(entry::getKey);

						setNumberOfTools(
							() -> _getNumberOfTools(
								httpServletRequest, numberOfToolsMap,
								entry.getValue(), entry.getKey()));
					}
				}));
	}

	public static Page<ToolSummary> getToolSummariesPage(
		HttpServletRequest httpServletRequest, String toolSetName) {

		return Page.of(
			OpenAPIUtil.getToolSummaries(
				_getOpenAPIJSONObject(
					httpServletRequest, _getOpenAPIBrief(toolSetName))));
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
			if (Objects.equals(toolName, "getToolSetToolSetNameTool")) {
				return _getResponse(
					getTool(
						httpServletRequest,
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
				return _getResponse(getToolSetsPage(httpServletRequest));
			}

			if (Objects.equals(toolName, "postToolSetToolSetNameToolInvoke")) {
				return invokeTool(
					dataMaskExternalReferenceCodes, httpServletRequest,
					inputJSONObject.opt("body"),
					inputJSONObject.getString("toolName"),
					inputJSONObject.getString("toolSetName"));
			}
		}

		VulcanRequestForwarder vulcanRequestForwarder =
			_vulcanRequestForwarderSnapshot.get();

		OpenAPIBrief openAPIBrief = _getOpenAPIBrief(toolSetName);

		VulcanRequestForwarder.Response response =
			vulcanRequestForwarder.forward(
				httpServletRequest,
				OpenAPIUtil.getRequest(
					openAPIBrief._basePath,
					HashMapBuilder.put(
						"X-Liferay-Data-Masks",
						() -> StringUtil.merge(
							dataMaskExternalReferenceCodes, StringPool.COMMA)
					).build(),
					inputJSONObject,
					_getOpenAPIJSONObject(httpServletRequest, openAPIBrief),
					toolName, _getUser(httpServletRequest)));

		String content = response.getContent();

		return Response.status(
			response.getStatusCode()
		).entity(
			Validator.isNull(content) ? null : _getContent(content)
		).type(
			ContentTypes.TEXT_PLAIN_UTF8
		).build();
	}

	private static Map<String, Integer> _createNumberOfToolsMap(
		HttpServletRequest httpServletRequest,
		Map<String, OpenAPIBrief> openAPIBriefs) {

		OpenAPIBrief openAPIBrief = openAPIBriefs.get(_AGGREGATE_TOOL_SET_NAME);

		if (openAPIBrief == null) {
			return Collections.emptyMap();
		}

		try {
			return OpenAPIUtil.getNumberOfToolsMap(
				_getOpenAPIJSONObject(httpServletRequest, openAPIBrief),
				openAPIBriefs.keySet());
		}
		catch (RuntimeException runtimeException) {
			if (_log.isWarnEnabled()) {
				_log.warn(runtimeException);
			}

			return Collections.emptyMap();
		}
	}

	private static long _getChangeCount() {
		Bundle bundle = FrameworkUtil.getBundle(ToolSetUtil.class);

		BundleContext bundleContext = bundle.getBundleContext();

		ServiceReference<JaxrsServiceRuntime> serviceReference =
			bundleContext.getServiceReference(JaxrsServiceRuntime.class);

		if (serviceReference == null) {
			return -1;
		}

		return GetterUtil.getLong(
			serviceReference.getProperty("service.changecount"), -1);
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

	private static String _getDescription(Object service) {
		if (service == null) {
			return null;
		}

		Class<?> serviceClass = service.getClass();

		OpenAPIDefinition openAPIDefinition = serviceClass.getAnnotation(
			OpenAPIDefinition.class);

		if (openAPIDefinition == null) {
			return null;
		}

		Info info = openAPIDefinition.info();

		String description = info.description();

		if (description == null) {
			return null;
		}

		return description;
	}

	private static Integer _getNumberOfTools(
		HttpServletRequest httpServletRequest,
		Map<String, Integer> numberOfToolsMap, OpenAPIBrief openAPIBrief,
		String toolSetName) {

		if (Objects.equals(toolSetName, _AGGREGATE_TOOL_SET_NAME)) {
			return null;
		}

		Integer numberOfTools = numberOfToolsMap.get(toolSetName);

		if (numberOfTools != null) {
			return numberOfTools;
		}

		// The entry was invalidated on its own, so refill it from this tool
		// set's document instead of regenerating the aggregate

		try {
			List<ToolSummary> toolSummaries = OpenAPIUtil.getToolSummaries(
				_getOpenAPIJSONObject(httpServletRequest, openAPIBrief));

			numberOfTools = toolSummaries.size();

			numberOfToolsMap.put(toolSetName, numberOfTools);

			return numberOfTools;
		}
		catch (RuntimeException runtimeException) {
			if (_log.isWarnEnabled()) {
				_log.warn(runtimeException);
			}

			return null;
		}
	}

	private static Map<String, Integer> _getNumberOfToolsMap(
		HttpServletRequest httpServletRequest,
		Map<String, OpenAPIBrief> openAPIBriefs) {

		return _numberOfToolsMaps.computeIfAbsent(
			PortalUtil.getCompanyId(httpServletRequest),
			companyId -> new ConcurrentHashMap<>(
				_createNumberOfToolsMap(httpServletRequest, openAPIBriefs)));
	}

	private static OpenAPIBrief _getOpenAPIBrief(String toolSetName) {
		Map<String, OpenAPIBrief> openAPIBriefs = _getOpenAPIBriefs();

		OpenAPIBrief openAPIBrief = openAPIBriefs.get(toolSetName);

		if (openAPIBrief == null) {
			throw new IllegalArgumentException(
				"No tool-set was found with name \"" + toolSetName + "\"");
		}

		return openAPIBrief;
	}

	private static Map<String, OpenAPIBrief> _getOpenAPIBriefs() {
		long changeCount = _getChangeCount();

		Map<String, OpenAPIBrief> openAPIBriefs = _openAPIBriefs.get(
			changeCount);

		if (openAPIBriefs != null) {
			return openAPIBriefs;
		}

		openAPIBriefs = new TreeMap<>();

		JaxrsServiceRuntime jaxrsServiceRuntime =
			_jaxrsServiceRuntimeSnapshot.get();

		// The JAX-RS runtime is unavailable while the batch engine seeds the
		// system object definitions at startup, so report no tool sets rather
		// than failing the caller

		if (jaxrsServiceRuntime == null) {
			return openAPIBriefs;
		}

		RuntimeDTO runtimeDTO = jaxrsServiceRuntime.getRuntimeDTO();

		Map<String, String> toolSetDescriptions = _getToolSetDescriptions();

		for (ApplicationDTO applicationDTO : runtimeDTO.applicationDTOs) {
			String base = applicationDTO.base;

			if (Validator.isNull(base)) {
				continue;
			}

			if (!base.startsWith(StringPool.SLASH)) {
				base = StringPool.SLASH + base;
			}

			for (String openAPIPath : _getOpenAPIPaths(applicationDTO)) {
				String basePath = base + _getVersionPath(openAPIPath);

				openAPIBriefs.put(
					StringUtil.replace(
						basePath.substring(1), CharPool.SLASH, CharPool.DASH),
					new OpenAPIBrief(
						base, toolSetDescriptions.get(basePath), openAPIPath));
			}
		}

		if (changeCount != -1) {
			_openAPIBriefs.clear();

			_openAPIBriefs.put(changeCount, openAPIBriefs);
		}

		return openAPIBriefs;
	}

	private static JSONObject _getOpenAPIJSONObject(
		HttpServletRequest httpServletRequest, OpenAPIBrief openAPIBrief) {

		return _openAPIJSONObjects.computeIfAbsent(
			StringBundler.concat(
				PortalUtil.getCompanyId(httpServletRequest), StringPool.POUND,
				openAPIBrief._basePath, openAPIBrief._openAPIPath),
			key -> {
				String path =
					openAPIBrief._basePath + openAPIBrief._openAPIPath;

				try {
					VulcanRequestForwarder vulcanRequestForwarder =
						_vulcanRequestForwarderSnapshot.get();

					VulcanRequestForwarder.Response response =
						vulcanRequestForwarder.forward(
							httpServletRequest,
							new VulcanRequestForwarder.Request() {

								@Override
								public String getMethod() {
									return "GET";
								}

								@Override
								public String getPath() {
									return path;
								}

								@Override
								public User getUser() {
									return _getUser(httpServletRequest);
								}

							});

					if (response.getStatusCode() >= 300) {
						throw new RuntimeException(
							StringBundler.concat(
								"HTTP ", response.getStatusCode(), " for ",
								path, ": ", response.getContent()));
					}

					return JSONFactoryUtil.createJSONObject(
						response.getContent());
				}
				catch (Exception exception) {
					throw new RuntimeException(exception);
				}
			});
	}

	private static Set<String> _getOpenAPIPaths(ApplicationDTO applicationDTO) {
		Set<String> openAPIPaths = new TreeSet<>();

		for (ResourceDTO resourceDTO : applicationDTO.resourceDTOs) {
			openAPIPaths.addAll(_getOpenAPIPaths(resourceDTO.resourceMethods));
		}

		if (openAPIPaths.isEmpty()) {
			openAPIPaths.addAll(
				_getOpenAPIPaths(applicationDTO.resourceMethods));
		}

		return openAPIPaths;
	}

	private static Set<String> _getOpenAPIPaths(
		ResourceMethodInfoDTO[] resourceMethodInfoDTOs) {

		Set<String> openAPIPaths = new TreeSet<>();

		if (resourceMethodInfoDTOs == null) {
			return openAPIPaths;
		}

		for (ResourceMethodInfoDTO resourceMethodInfoDTO :
				resourceMethodInfoDTOs) {

			String path = resourceMethodInfoDTO.path;

			if ((path != null) && path.contains("/openapi")) {
				openAPIPaths.add(
					StringUtil.replace(path, "{type:json|yaml}", "json"));
			}
		}

		return openAPIPaths;
	}

	private static Response _getResponse(Object value) throws Exception {
		ObjectMapper objectMapper = ObjectMapperProviderUtil.getObjectMapper();

		return Response.ok(
			objectMapper.writeValueAsString(value), ContentTypes.TEXT_PLAIN_UTF8
		).build();
	}

	private static Map<String, String> _getToolSetDescriptions() {
		Map<String, String> toolSetDescriptions = new HashMap<>();

		Bundle bundle = FrameworkUtil.getBundle(ToolSetUtil.class);

		BundleContext bundleContext = bundle.getBundleContext();

		ServiceReference<?>[] serviceReferences;

		try {
			serviceReferences = bundleContext.getAllServiceReferences(
				null, "(openapi.resource=true)");
		}
		catch (InvalidSyntaxException invalidSyntaxException) {
			if (_log.isWarnEnabled()) {
				_log.warn(invalidSyntaxException);
			}

			return toolSetDescriptions;
		}

		if (serviceReferences == null) {
			return toolSetDescriptions;
		}

		for (ServiceReference<?> serviceReference : serviceReferences) {
			String path = GetterUtil.getString(
				serviceReference.getProperty("openapi.resource.path"));

			if (Validator.isNull(path)) {
				continue;
			}

			String version = GetterUtil.getString(
				serviceReference.getProperty("api.version"));

			if (Validator.isNotNull(version)) {
				path = path + StringPool.SLASH + version;
			}

			Object service = bundleContext.getService(serviceReference);

			try {
				toolSetDescriptions.putIfAbsent(path, _getDescription(service));
			}
			finally {
				bundleContext.ungetService(serviceReference);
			}
		}

		return toolSetDescriptions;
	}

	private static User _getUser(HttpServletRequest httpServletRequest) {
		return UserLocalServiceUtil.fetchUser(
			GetterUtil.getLong(
				httpServletRequest.getAttribute(WebKeys.USER_ID)));
	}

	private static String _getVersionPath(String openAPIPath) {
		int index = openAPIPath.lastIndexOf("/openapi");

		if (index <= 0) {
			return StringPool.BLANK;
		}

		return openAPIPath.substring(0, index);
	}

	private static final String _AGGREGATE_TOOL_SET_NAME = "openapi";

	private static final String _TOOL_SET_NAME = "mcp-server-v1.0";

	private static final Log _log = LogFactoryUtil.getLog(ToolSetUtil.class);

	private static final Snapshot<JaxrsServiceRuntime>
		_jaxrsServiceRuntimeSnapshot = new Snapshot<>(
			ToolSetUtil.class, JaxrsServiceRuntime.class);
	private static final Map<Long, Map<String, Integer>> _numberOfToolsMaps =
		new ConcurrentHashMap<>();
	private static final Map<Long, Map<String, OpenAPIBrief>> _openAPIBriefs =
		new ConcurrentHashMap<>();
	private static final Map<String, JSONObject> _openAPIJSONObjects =
		new ConcurrentHashMap<>();
	private static final Snapshot<VulcanRequestForwarder>
		_vulcanRequestForwarderSnapshot = new Snapshot<>(
			ToolSetUtil.class, VulcanRequestForwarder.class);

	private static class OpenAPIBrief {

		private OpenAPIBrief(
			String basePath, String description, String openAPIPath) {

			_basePath = basePath;
			_description = description;
			_openAPIPath = openAPIPath;
		}

		private final String _basePath;
		private final String _description;
		private final String _openAPIPath;

	}

}