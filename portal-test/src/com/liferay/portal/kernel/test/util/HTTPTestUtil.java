/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.test.util;

import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.HttpUtil;

import java.nio.charset.StandardCharsets;

/**
 * @author Luis Miguel Barcos
 * @author Alberto Javier Moreno Lage
 */
public class HTTPTestUtil {

	public static int invokeToHttpCode(
			String body, String endpoint, Http.Method httpMethod)
		throws Exception {

		Http.Options options = _getHttpOptions(body, endpoint, httpMethod);

		HttpUtil.URLtoString(options);

		Http.Response response = options.getResponse();

		return response.getResponseCode();
	}

	public static JSONObject invokeToJSONObject(
			String body, String endpoint, Http.Method httpMethod)
		throws Exception {

		Http.Options options = _getHttpOptions(body, endpoint, httpMethod);

		return JSONFactoryUtil.createJSONObject(HttpUtil.URLtoString(options));
	}

	public static <T extends Throwable> void withCredentials(
			String emailAddress, String password,
			UnsafeRunnable<T> unsafeRunnable)
		throws T {

		String credentials = _credentials;

		_credentials = emailAddress + StringPool.COLON + password;

		try {
			unsafeRunnable.run();
		}
		finally {
			_credentials = credentials;
		}
	}

	public static class Customizer {

		public <T extends Throwable> void apply(
				UnsafeRunnable<T> unsafeRunnable)
			throws T {

			Company defaultCompany = _company;
			_company = _newCompany;

			String defaultCredentials = _credentials;
			_credentials = _newCredentials;

			try {
				unsafeRunnable.run();
			}
			finally {
				_company = defaultCompany;
				_credentials = defaultCredentials;
			}
		}

		public Customizer company(Company company) {
			_newCompany = company;

			return this;
		}

		public Customizer credentials(String emailAddress, String password) {
			_newCredentials = emailAddress + StringPool.COLON + password;

			return this;
		}

		private Company _newCompany = _company;
		private String _newCredentials = _credentials;

	}

	private static Http.Options _getHttpOptions(
			String body, String endpoint, Http.Method httpMethod)
		throws Exception {

		Http.Options options = new Http.Options();

		options.addHeader(
			HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON);
		options.addHeader(
			"Authorization", "Basic " + Base64.encode(_credentials.getBytes()));
		options.setLocation(
			_company.getPortalURL(_company.getGroupId()) + "/o/" + endpoint);
		options.setMethod(httpMethod);

		if (body != null) {
			options.setBody(
				body, ContentTypes.APPLICATION_JSON,
				StandardCharsets.UTF_8.name());
		}

		return options;
	}

	private static Company _company;
	private static String _credentials = "test@liferay.com:test";

	static {
		try {
			_company = CompanyLocalServiceUtil.getCompanyById(
				TestPropsValues.getCompanyId());
		}
		catch (PortalException portalException) {
			throw new RuntimeException(portalException);
		}
	}

}