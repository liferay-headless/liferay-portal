/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.util;

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
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.vulcan.http.VulcanRequestForwarder;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
 * @author Petteri Karttunen
 */
public class OpenAPIBriefUtil {

	public static void clearOpenAPIJSONObjectCache(long companyId) {
		Set<String> keys = _openAPIJSONObjects.keySet();

		keys.removeIf(key -> key.startsWith(companyId + StringPool.POUND));
	}

	public static void clearOpenAPIJSONObjectCache(
		long companyId, String restContextPath) {

		String toolSetName = getToolSetName(restContextPath);

		if (toolSetName == null) {
			clearOpenAPIJSONObjectCache(companyId);

			return;
		}

		String prefix = _getPrefix(companyId, restContextPath, toolSetName);

		Set<String> keys = _openAPIJSONObjects.keySet();

		keys.removeIf(key -> key.startsWith(prefix));
	}

	public static long getChangeCount() {
		Bundle bundle = FrameworkUtil.getBundle(OpenAPIBriefUtil.class);

		BundleContext bundleContext = bundle.getBundleContext();

		ServiceReference<?> serviceReference =
			bundleContext.getServiceReference(JaxrsServiceRuntime.class);

		if (serviceReference == null) {
			return -1;
		}

		return GetterUtil.getLong(
			serviceReference.getProperty("service.changecount"), -1);
	}

	public static OpenAPIBrief getOpenAPIBrief(String toolSetName) {
		Map<String, OpenAPIBrief> openAPIBriefs = getOpenAPIBriefs();

		OpenAPIBrief openAPIBrief = openAPIBriefs.get(toolSetName);

		if (openAPIBrief == null) {
			throw new IllegalArgumentException(
				"No tool-set was found with name \"" + toolSetName + "\"");
		}

		return openAPIBrief;
	}

	public static Map<String, OpenAPIBrief> getOpenAPIBriefs() {
		long changeCount = getChangeCount();

		Map<String, OpenAPIBrief> openAPIBriefs = _openAPIBriefs;

		if ((openAPIBriefs != null) && (changeCount != -1) &&
			(changeCount == _changeCount)) {

			return openAPIBriefs;
		}

		openAPIBriefs = _buildOpenAPIBriefs();

		_openAPIBriefs = openAPIBriefs;

		_changeCount = changeCount;

		return openAPIBriefs;
	}

	public static JSONObject getOpenAPIJSONObject(
		HttpServletRequest httpServletRequest, OpenAPIBrief openAPIBrief) {

		return _openAPIJSONObjects.computeIfAbsent(
			StringBundler.concat(
				PortalUtil.getCompanyId(httpServletRequest), StringPool.POUND,
				openAPIBrief.getBasePath(), openAPIBrief.getOpenAPIPath()),
			key -> {
				String path =
					openAPIBrief.getBasePath() + openAPIBrief.getOpenAPIPath();

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

	public static String getToolSetName(String restContextPath) {
		if (Validator.isBlank(restContextPath)) {
			return null;
		}

		String basePath = null;
		String toolSetName = null;

		for (Map.Entry<String, OpenAPIBrief> entry :
				_getCachedOpenAPIBriefs().entrySet()) {

			OpenAPIBrief openAPIBrief = entry.getValue();

			String openAPIBriefBasePath = openAPIBrief.getBasePath();

			if (!restContextPath.equals(openAPIBriefBasePath) &&
				!restContextPath.startsWith(
					openAPIBriefBasePath + StringPool.SLASH)) {

				continue;
			}

			if ((basePath == null) ||
				(openAPIBriefBasePath.length() > basePath.length())) {

				basePath = openAPIBriefBasePath;
				toolSetName = entry.getKey();
			}
		}

		if ((toolSetName == null) &&
			restContextPath.startsWith(_OBJECT_PATH_PREFIX)) {

			toolSetName = _toToolSetName(restContextPath);
		}

		return toolSetName;
	}

	private static Map<String, OpenAPIBrief> _buildOpenAPIBriefs() {
		Map<String, OpenAPIBrief> openAPIBriefs = new TreeMap<>();

		JaxrsServiceRuntime jaxrsServiceRuntime =
			_jaxrsServiceRuntimeSnapshot.get();

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

			String openAPIPath = _getOpenAPIPath(applicationDTO);

			if (openAPIPath == null) {
				continue;
			}

			String basePath = base + _getVersionPath(openAPIPath);

			openAPIBriefs.put(
				_toToolSetName(basePath),
				new OpenAPIBrief(
					base, toolSetDescriptions.get(basePath), openAPIPath));
		}

		return openAPIBriefs;
	}

	private static Map<String, OpenAPIBrief> _getCachedOpenAPIBriefs() {
		Map<String, OpenAPIBrief> openAPIBriefs = _openAPIBriefs;

		if (openAPIBriefs == null) {
			return Collections.emptyMap();
		}

		return openAPIBriefs;
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

	private static String _getOpenAPIPath(ApplicationDTO applicationDTO) {
		for (ResourceDTO resourceDTO : applicationDTO.resourceDTOs) {
			String openAPIPath = _getOpenAPIPath(resourceDTO.resourceMethods);

			if (openAPIPath != null) {
				return openAPIPath;
			}
		}

		return _getOpenAPIPath(applicationDTO.resourceMethods);
	}

	private static String _getOpenAPIPath(
		ResourceMethodInfoDTO[] resourceMethodInfoDTOs) {

		if (resourceMethodInfoDTOs == null) {
			return null;
		}

		for (ResourceMethodInfoDTO resourceMethodInfoDTO :
				resourceMethodInfoDTOs) {

			String path = resourceMethodInfoDTO.path;

			if ((path != null) && path.contains("/openapi")) {
				return StringUtil.replace(path, "{type:json|yaml}", "json");
			}
		}

		return null;
	}

	private static String _getPrefix(
		long companyId, String restContextPath, String toolSetName) {

		OpenAPIBrief openAPIBrief = _getCachedOpenAPIBriefs().get(toolSetName);

		if (openAPIBrief == null) {
			return StringBundler.concat(
				companyId, StringPool.POUND, restContextPath, StringPool.SLASH);
		}

		return StringBundler.concat(
			companyId, StringPool.POUND, openAPIBrief.getBasePath(),
			StringPool.SLASH);
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

	private static String _toToolSetName(String basePath) {
		return StringUtil.replace(
			basePath.substring(1), CharPool.SLASH, CharPool.DASH);
	}

	private static final String _OBJECT_PATH_PREFIX = "/c/";

	private static final Log _log = LogFactoryUtil.getLog(
		OpenAPIBriefUtil.class);

	private static volatile long _changeCount = -1;
	private static final Snapshot<JaxrsServiceRuntime>
		_jaxrsServiceRuntimeSnapshot = new Snapshot<>(
			OpenAPIBriefUtil.class, JaxrsServiceRuntime.class);
	private static volatile Map<String, OpenAPIBrief> _openAPIBriefs;
	private static final Map<String, JSONObject> _openAPIJSONObjects =
		new ConcurrentHashMap<>();
	private static final Snapshot<VulcanRequestForwarder>
		_vulcanRequestForwarderSnapshot = new Snapshot<>(
			OpenAPIBriefUtil.class, VulcanRequestForwarder.class);

}