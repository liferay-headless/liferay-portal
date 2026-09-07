/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.template;

import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.template.TemplateContextContributor;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.vulcan.http.VulcanRequestForwarder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alejandro Tardín
 */
@Component(
	property = "type=" + TemplateContextContributor.TYPE_GLOBAL,
	service = TemplateContextContributor.class
)
public class RESTClientTemplateContextContributor
	implements TemplateContextContributor {

	@Override
	public void prepare(
		Map<String, Object> contextObjects,
		HttpServletRequest httpServletRequest) {

		contextObjects.put(
			"restClient", new RESTClient(contextObjects, httpServletRequest));
	}

	public class RESTClient {

		public RESTClient(
			Map<String, Object> contextObjects,
			HttpServletRequest httpServletRequest) {

			_contextObjects = contextObjects;
			_httpServletRequest = httpServletRequest;

			_results = _getResults(contextObjects, httpServletRequest);
		}

		public Object get(String path) throws Exception {
			Object result = _results.get(path);

			if (result != null) {
				return result;
			}

			VulcanRequestForwarder.Response response;

			try {
				response = _forward(path);
			}
			catch (Throwable throwable) {
				_log.error(throwable, throwable);

				throw throwable;
			}

			result = response.getContent();

			if (Objects.equals(
					response.getContentType(), ContentTypes.APPLICATION_JSON)) {

				result = _jsonFactory.looseDeserialize(response.getContent());
			}

			if (response.getStatusCode() < HttpServletResponse.SC_BAD_REQUEST) {
				_results.put(path, result);
			}

			return result;
		}

		private VulcanRequestForwarder.Response _forward(String path)
			throws Exception {

			return _vulcanRequestForwarder.forward(
				_httpServletRequest,
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
						return (User)_contextObjects.get("user");
					}

				});
		}

		@SuppressWarnings("unchecked")
		private Map<String, Object> _getResults(
			Map<String, Object> contextObjects,
			HttpServletRequest httpServletRequest) {

			if (httpServletRequest == null) {
				return (Map<String, Object>)contextObjects.computeIfAbsent(
					_RESULTS_KEY, key -> new HashMap<>());
			}

			Map<String, Object> results =
				(Map<String, Object>)httpServletRequest.getAttribute(
					_RESULTS_KEY);

			if (results == null) {
				results = new HashMap<>();

				httpServletRequest.setAttribute(_RESULTS_KEY, results);
			}

			return results;
		}

		private final Map<String, Object> _contextObjects;
		private final HttpServletRequest _httpServletRequest;
		private final Map<String, Object> _results;

	}

	private static final String _RESULTS_KEY =
		RESTClientTemplateContextContributor.class.getName() + "#results";

	private static final Log _log = LogFactoryUtil.getLog(
		RESTClientTemplateContextContributor.class);

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private VulcanRequestForwarder _vulcanRequestForwarder;

}