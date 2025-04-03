/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.tools.rest.builder.test.client.resource.v1_0;

import com.liferay.portal.tools.rest.builder.test.client.dto.v1_0.ErcAssetLibraryTestEntity;
import com.liferay.portal.tools.rest.builder.test.client.http.HttpInvoker;
import com.liferay.portal.tools.rest.builder.test.client.pagination.Page;
import com.liferay.portal.tools.rest.builder.test.client.problem.Problem;
import com.liferay.portal.tools.rest.builder.test.client.serdes.v1_0.ErcAssetLibraryTestEntitySerDes;

import java.net.URL;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Generated;

/**
 * @author Alejandro Tardín
 * @generated
 */
@Generated("")
public interface ErcAssetLibraryTestEntityResource {

	public static Builder builder() {
		return new Builder();
	}

	public Page<ErcAssetLibraryTestEntity>
			getAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntitiesPage(
				String assetLibraryExternalReferenceCode)
		throws Exception;

	public HttpInvoker.HttpResponse
			getAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntitiesPageHttpResponse(
				String assetLibraryExternalReferenceCode)
		throws Exception;

	public void
			postAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntitiesPageExportBatch(
				String assetLibraryExternalReferenceCode, String callbackURL,
				String contentType, String fieldNames)
		throws Exception;

	public HttpInvoker.HttpResponse
			postAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntitiesPageExportBatchHttpResponse(
				String assetLibraryExternalReferenceCode, String callbackURL,
				String contentType, String fieldNames)
		throws Exception;

	public ErcAssetLibraryTestEntity
			postAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntity(
				String assetLibraryExternalReferenceCode,
				ErcAssetLibraryTestEntity ercAssetLibraryTestEntity)
		throws Exception;

	public HttpInvoker.HttpResponse
			postAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityHttpResponse(
				String assetLibraryExternalReferenceCode,
				ErcAssetLibraryTestEntity ercAssetLibraryTestEntity)
		throws Exception;

	public void
			postAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityBatch(
				String assetLibraryExternalReferenceCode, String callbackURL,
				Object object)
		throws Exception;

	public HttpInvoker.HttpResponse
			postAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityBatchHttpResponse(
				String assetLibraryExternalReferenceCode, String callbackURL,
				Object object)
		throws Exception;

	public ErcAssetLibraryTestEntity
			getAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityByExternalReferenceCode(
				String ErcAssetLibraryTestEntityExternalReferenceCode,
				String assetLibraryExternalReferenceCode)
		throws Exception;

	public HttpInvoker.HttpResponse
			getAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityByExternalReferenceCodeHttpResponse(
				String ErcAssetLibraryTestEntityExternalReferenceCode,
				String assetLibraryExternalReferenceCode)
		throws Exception;

	public ErcAssetLibraryTestEntity
			putAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityByExternalReferenceCode(
				String ErcAssetLibraryTestEntityExternalReferenceCode,
				String assetLibraryExternalReferenceCode,
				ErcAssetLibraryTestEntity ercAssetLibraryTestEntity)
		throws Exception;

	public HttpInvoker.HttpResponse
			putAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityByExternalReferenceCodeHttpResponse(
				String ErcAssetLibraryTestEntityExternalReferenceCode,
				String assetLibraryExternalReferenceCode,
				ErcAssetLibraryTestEntity ercAssetLibraryTestEntity)
		throws Exception;

	public Page<ErcAssetLibraryTestEntity>
			getAssetLibraryErcAssetLibraryTestEntitiesPage(Long assetLibraryId)
		throws Exception;

	public HttpInvoker.HttpResponse
			getAssetLibraryErcAssetLibraryTestEntitiesPageHttpResponse(
				Long assetLibraryId)
		throws Exception;

	public void postAssetLibraryErcAssetLibraryTestEntitiesPageExportBatch(
			Long assetLibraryId, String callbackURL, String contentType,
			String fieldNames)
		throws Exception;

	public HttpInvoker.HttpResponse
			postAssetLibraryErcAssetLibraryTestEntitiesPageExportBatchHttpResponse(
				Long assetLibraryId, String callbackURL, String contentType,
				String fieldNames)
		throws Exception;

	public ErcAssetLibraryTestEntity postAssetLibraryErcAssetLibraryTestEntity(
			Long assetLibraryId,
			ErcAssetLibraryTestEntity ercAssetLibraryTestEntity)
		throws Exception;

	public HttpInvoker.HttpResponse
			postAssetLibraryErcAssetLibraryTestEntityHttpResponse(
				Long assetLibraryId,
				ErcAssetLibraryTestEntity ercAssetLibraryTestEntity)
		throws Exception;

	public void postAssetLibraryErcAssetLibraryTestEntityBatch(
			Long assetLibraryId, String callbackURL, Object object)
		throws Exception;

	public HttpInvoker.HttpResponse
			postAssetLibraryErcAssetLibraryTestEntityBatchHttpResponse(
				Long assetLibraryId, String callbackURL, Object object)
		throws Exception;

	public static class Builder {

		public Builder authentication(String login, String password) {
			_login = login;
			_password = password;

			return this;
		}

		public Builder bearerToken(String token) {
			return header("Authorization", "Bearer " + token);
		}

		public ErcAssetLibraryTestEntityResource build() {
			return new ErcAssetLibraryTestEntityResourceImpl(this);
		}

		public Builder contextPath(String contextPath) {
			_contextPath = contextPath;

			return this;
		}

		public Builder endpoint(String address, String scheme) {
			String[] addressParts = address.split(":");

			String host = addressParts[0];

			int port = 443;

			if (addressParts.length > 1) {
				String portString = addressParts[1];

				try {
					port = Integer.parseInt(portString);
				}
				catch (NumberFormatException numberFormatException) {
					throw new IllegalArgumentException(
						"Unable to parse port from " + portString);
				}
			}

			return endpoint(host, port, scheme);
		}

		public Builder endpoint(String host, int port, String scheme) {
			_host = host;
			_port = port;
			_scheme = scheme;

			return this;
		}

		public Builder endpoint(URL url) {
			return endpoint(url.getHost(), url.getPort(), url.getProtocol());
		}

		public Builder header(String key, String value) {
			_headers.put(key, value);

			return this;
		}

		public Builder locale(Locale locale) {
			_locale = locale;

			return this;
		}

		public Builder parameter(String key, String value) {
			_parameters.put(key, value);

			return this;
		}

		public Builder parameters(String... parameters) {
			if ((parameters.length % 2) != 0) {
				throw new IllegalArgumentException(
					"Parameters length is not an even number");
			}

			for (int i = 0; i < parameters.length; i += 2) {
				String parameterName = String.valueOf(parameters[i]);
				String parameterValue = String.valueOf(parameters[i + 1]);

				_parameters.put(parameterName, parameterValue);
			}

			return this;
		}

		private Builder() {
		}

		private String _contextPath = "";
		private Map<String, String> _headers = new LinkedHashMap<>();
		private String _host = "localhost";
		private Locale _locale;
		private String _login;
		private String _password;
		private Map<String, String> _parameters = new LinkedHashMap<>();
		private int _port = 8080;
		private String _scheme = "http";

	}

	public static class ErcAssetLibraryTestEntityResourceImpl
		implements ErcAssetLibraryTestEntityResource {

		public Page<ErcAssetLibraryTestEntity>
				getAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntitiesPage(
					String assetLibraryExternalReferenceCode)
			throws Exception {

			HttpInvoker.HttpResponse httpResponse =
				getAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntitiesPageHttpResponse(
					assetLibraryExternalReferenceCode);

			String content = httpResponse.getContent();

			if ((httpResponse.getStatusCode() / 100) != 2) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response content: " + content);
				_logger.log(
					Level.WARNING,
					"HTTP response message: " + httpResponse.getMessage());
				_logger.log(
					Level.WARNING,
					"HTTP response status code: " +
						httpResponse.getStatusCode());

				Problem.ProblemException problemException = null;

				if (Objects.equals(
						httpResponse.getContentType(), "application/json")) {

					problemException = new Problem.ProblemException(
						Problem.toDTO(content));
				}
				else {
					_logger.log(
						Level.WARNING,
						"Unable to process content type: " +
							httpResponse.getContentType());

					Problem problem = new Problem();

					problem.setStatus(
						String.valueOf(httpResponse.getStatusCode()));

					problemException = new Problem.ProblemException(problem);
				}

				throw problemException;
			}
			else {
				_logger.fine("HTTP response content: " + content);
				_logger.fine(
					"HTTP response message: " + httpResponse.getMessage());
				_logger.fine(
					"HTTP response status code: " +
						httpResponse.getStatusCode());
			}

			try {
				return Page.of(content, ErcAssetLibraryTestEntitySerDes::toDTO);
			}
			catch (Exception e) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response: " + content, e);

				throw new Problem.ProblemException(Problem.toDTO(content));
			}
		}

		public HttpInvoker.HttpResponse
				getAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntitiesPageHttpResponse(
					String assetLibraryExternalReferenceCode)
			throws Exception {

			HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

			if (_builder._locale != null) {
				httpInvoker.header(
					"Accept-Language", _builder._locale.toLanguageTag());
			}

			for (Map.Entry<String, String> entry :
					_builder._headers.entrySet()) {

				httpInvoker.header(entry.getKey(), entry.getValue());
			}

			for (Map.Entry<String, String> entry :
					_builder._parameters.entrySet()) {

				httpInvoker.parameter(entry.getKey(), entry.getValue());
			}

			httpInvoker.httpMethod(HttpInvoker.HttpMethod.GET);

			httpInvoker.path(
				_builder._scheme + "://" + _builder._host + ":" +
					_builder._port + _builder._contextPath +
						"/o/test/v1.0/asset-libraries/{assetLibraryExternalReferenceCode}/erc-asset-library-test-entities");

			httpInvoker.path(
				"assetLibraryExternalReferenceCode",
				assetLibraryExternalReferenceCode);

			if ((_builder._login != null) && (_builder._password != null)) {
				httpInvoker.userNameAndPassword(
					_builder._login + ":" + _builder._password);
			}

			return httpInvoker.invoke();
		}

		public void
				postAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntitiesPageExportBatch(
					String assetLibraryExternalReferenceCode,
					String callbackURL, String contentType, String fieldNames)
			throws Exception {

			HttpInvoker.HttpResponse httpResponse =
				postAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntitiesPageExportBatchHttpResponse(
					assetLibraryExternalReferenceCode, callbackURL, contentType,
					fieldNames);

			String content = httpResponse.getContent();

			if ((httpResponse.getStatusCode() / 100) != 2) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response content: " + content);
				_logger.log(
					Level.WARNING,
					"HTTP response message: " + httpResponse.getMessage());
				_logger.log(
					Level.WARNING,
					"HTTP response status code: " +
						httpResponse.getStatusCode());

				Problem.ProblemException problemException = null;

				if (Objects.equals(
						httpResponse.getContentType(), "application/json")) {

					problemException = new Problem.ProblemException(
						Problem.toDTO(content));
				}
				else {
					_logger.log(
						Level.WARNING,
						"Unable to process content type: " +
							httpResponse.getContentType());

					Problem problem = new Problem();

					problem.setStatus(
						String.valueOf(httpResponse.getStatusCode()));

					problemException = new Problem.ProblemException(problem);
				}

				throw problemException;
			}
			else {
				_logger.fine("HTTP response content: " + content);
				_logger.fine(
					"HTTP response message: " + httpResponse.getMessage());
				_logger.fine(
					"HTTP response status code: " +
						httpResponse.getStatusCode());
			}
		}

		public HttpInvoker.HttpResponse
				postAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntitiesPageExportBatchHttpResponse(
					String assetLibraryExternalReferenceCode,
					String callbackURL, String contentType, String fieldNames)
			throws Exception {

			HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

			httpInvoker.body("[]", "application/json");

			if (_builder._locale != null) {
				httpInvoker.header(
					"Accept-Language", _builder._locale.toLanguageTag());
			}

			for (Map.Entry<String, String> entry :
					_builder._headers.entrySet()) {

				httpInvoker.header(entry.getKey(), entry.getValue());
			}

			for (Map.Entry<String, String> entry :
					_builder._parameters.entrySet()) {

				httpInvoker.parameter(entry.getKey(), entry.getValue());
			}

			httpInvoker.httpMethod(HttpInvoker.HttpMethod.POST);

			if (callbackURL != null) {
				httpInvoker.parameter(
					"callbackURL", String.valueOf(callbackURL));
			}

			if (contentType != null) {
				httpInvoker.parameter(
					"contentType", String.valueOf(contentType));
			}

			if (fieldNames != null) {
				httpInvoker.parameter("fieldNames", String.valueOf(fieldNames));
			}

			httpInvoker.path(
				_builder._scheme + "://" + _builder._host + ":" +
					_builder._port + _builder._contextPath +
						"/o/test/v1.0/asset-libraries/{assetLibraryExternalReferenceCode}/erc-asset-library-test-entities/export-batch");

			httpInvoker.path(
				"assetLibraryExternalReferenceCode",
				assetLibraryExternalReferenceCode);

			if ((_builder._login != null) && (_builder._password != null)) {
				httpInvoker.userNameAndPassword(
					_builder._login + ":" + _builder._password);
			}

			return httpInvoker.invoke();
		}

		public ErcAssetLibraryTestEntity
				postAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntity(
					String assetLibraryExternalReferenceCode,
					ErcAssetLibraryTestEntity ercAssetLibraryTestEntity)
			throws Exception {

			HttpInvoker.HttpResponse httpResponse =
				postAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityHttpResponse(
					assetLibraryExternalReferenceCode,
					ercAssetLibraryTestEntity);

			String content = httpResponse.getContent();

			if ((httpResponse.getStatusCode() / 100) != 2) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response content: " + content);
				_logger.log(
					Level.WARNING,
					"HTTP response message: " + httpResponse.getMessage());
				_logger.log(
					Level.WARNING,
					"HTTP response status code: " +
						httpResponse.getStatusCode());

				Problem.ProblemException problemException = null;

				if (Objects.equals(
						httpResponse.getContentType(), "application/json")) {

					problemException = new Problem.ProblemException(
						Problem.toDTO(content));
				}
				else {
					_logger.log(
						Level.WARNING,
						"Unable to process content type: " +
							httpResponse.getContentType());

					Problem problem = new Problem();

					problem.setStatus(
						String.valueOf(httpResponse.getStatusCode()));

					problemException = new Problem.ProblemException(problem);
				}

				throw problemException;
			}
			else {
				_logger.fine("HTTP response content: " + content);
				_logger.fine(
					"HTTP response message: " + httpResponse.getMessage());
				_logger.fine(
					"HTTP response status code: " +
						httpResponse.getStatusCode());
			}

			try {
				return ErcAssetLibraryTestEntitySerDes.toDTO(content);
			}
			catch (Exception e) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response: " + content, e);

				throw new Problem.ProblemException(Problem.toDTO(content));
			}
		}

		public HttpInvoker.HttpResponse
				postAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityHttpResponse(
					String assetLibraryExternalReferenceCode,
					ErcAssetLibraryTestEntity ercAssetLibraryTestEntity)
			throws Exception {

			HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

			httpInvoker.body(
				ercAssetLibraryTestEntity.toString(), "application/json");

			if (_builder._locale != null) {
				httpInvoker.header(
					"Accept-Language", _builder._locale.toLanguageTag());
			}

			for (Map.Entry<String, String> entry :
					_builder._headers.entrySet()) {

				httpInvoker.header(entry.getKey(), entry.getValue());
			}

			for (Map.Entry<String, String> entry :
					_builder._parameters.entrySet()) {

				httpInvoker.parameter(entry.getKey(), entry.getValue());
			}

			httpInvoker.httpMethod(HttpInvoker.HttpMethod.POST);

			httpInvoker.path(
				_builder._scheme + "://" + _builder._host + ":" +
					_builder._port + _builder._contextPath +
						"/o/test/v1.0/asset-libraries/{assetLibraryExternalReferenceCode}/erc-asset-library-test-entities");

			httpInvoker.path(
				"assetLibraryExternalReferenceCode",
				assetLibraryExternalReferenceCode);

			if ((_builder._login != null) && (_builder._password != null)) {
				httpInvoker.userNameAndPassword(
					_builder._login + ":" + _builder._password);
			}

			return httpInvoker.invoke();
		}

		public void
				postAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityBatch(
					String assetLibraryExternalReferenceCode,
					String callbackURL, Object object)
			throws Exception {

			HttpInvoker.HttpResponse httpResponse =
				postAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityBatchHttpResponse(
					assetLibraryExternalReferenceCode, callbackURL, object);

			String content = httpResponse.getContent();

			if ((httpResponse.getStatusCode() / 100) != 2) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response content: " + content);
				_logger.log(
					Level.WARNING,
					"HTTP response message: " + httpResponse.getMessage());
				_logger.log(
					Level.WARNING,
					"HTTP response status code: " +
						httpResponse.getStatusCode());

				Problem.ProblemException problemException = null;

				if (Objects.equals(
						httpResponse.getContentType(), "application/json")) {

					problemException = new Problem.ProblemException(
						Problem.toDTO(content));
				}
				else {
					_logger.log(
						Level.WARNING,
						"Unable to process content type: " +
							httpResponse.getContentType());

					Problem problem = new Problem();

					problem.setStatus(
						String.valueOf(httpResponse.getStatusCode()));

					problemException = new Problem.ProblemException(problem);
				}

				throw problemException;
			}
			else {
				_logger.fine("HTTP response content: " + content);
				_logger.fine(
					"HTTP response message: " + httpResponse.getMessage());
				_logger.fine(
					"HTTP response status code: " +
						httpResponse.getStatusCode());
			}
		}

		public HttpInvoker.HttpResponse
				postAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityBatchHttpResponse(
					String assetLibraryExternalReferenceCode,
					String callbackURL, Object object)
			throws Exception {

			HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

			httpInvoker.body(object.toString(), "application/json");

			if (_builder._locale != null) {
				httpInvoker.header(
					"Accept-Language", _builder._locale.toLanguageTag());
			}

			for (Map.Entry<String, String> entry :
					_builder._headers.entrySet()) {

				httpInvoker.header(entry.getKey(), entry.getValue());
			}

			for (Map.Entry<String, String> entry :
					_builder._parameters.entrySet()) {

				httpInvoker.parameter(entry.getKey(), entry.getValue());
			}

			httpInvoker.httpMethod(HttpInvoker.HttpMethod.POST);

			if (callbackURL != null) {
				httpInvoker.parameter(
					"callbackURL", String.valueOf(callbackURL));
			}

			httpInvoker.path(
				_builder._scheme + "://" + _builder._host + ":" +
					_builder._port + _builder._contextPath +
						"/o/test/v1.0/asset-libraries/{assetLibraryExternalReferenceCode}/erc-asset-library-test-entities/batch");

			httpInvoker.path(
				"assetLibraryExternalReferenceCode",
				assetLibraryExternalReferenceCode);

			if ((_builder._login != null) && (_builder._password != null)) {
				httpInvoker.userNameAndPassword(
					_builder._login + ":" + _builder._password);
			}

			return httpInvoker.invoke();
		}

		public ErcAssetLibraryTestEntity
				getAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityByExternalReferenceCode(
					String ErcAssetLibraryTestEntityExternalReferenceCode,
					String assetLibraryExternalReferenceCode)
			throws Exception {

			HttpInvoker.HttpResponse httpResponse =
				getAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityByExternalReferenceCodeHttpResponse(
					ErcAssetLibraryTestEntityExternalReferenceCode,
					assetLibraryExternalReferenceCode);

			String content = httpResponse.getContent();

			if ((httpResponse.getStatusCode() / 100) != 2) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response content: " + content);
				_logger.log(
					Level.WARNING,
					"HTTP response message: " + httpResponse.getMessage());
				_logger.log(
					Level.WARNING,
					"HTTP response status code: " +
						httpResponse.getStatusCode());

				Problem.ProblemException problemException = null;

				if (Objects.equals(
						httpResponse.getContentType(), "application/json")) {

					problemException = new Problem.ProblemException(
						Problem.toDTO(content));
				}
				else {
					_logger.log(
						Level.WARNING,
						"Unable to process content type: " +
							httpResponse.getContentType());

					Problem problem = new Problem();

					problem.setStatus(
						String.valueOf(httpResponse.getStatusCode()));

					problemException = new Problem.ProblemException(problem);
				}

				throw problemException;
			}
			else {
				_logger.fine("HTTP response content: " + content);
				_logger.fine(
					"HTTP response message: " + httpResponse.getMessage());
				_logger.fine(
					"HTTP response status code: " +
						httpResponse.getStatusCode());
			}

			try {
				return ErcAssetLibraryTestEntitySerDes.toDTO(content);
			}
			catch (Exception e) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response: " + content, e);

				throw new Problem.ProblemException(Problem.toDTO(content));
			}
		}

		public HttpInvoker.HttpResponse
				getAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityByExternalReferenceCodeHttpResponse(
					String ErcAssetLibraryTestEntityExternalReferenceCode,
					String assetLibraryExternalReferenceCode)
			throws Exception {

			HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

			if (_builder._locale != null) {
				httpInvoker.header(
					"Accept-Language", _builder._locale.toLanguageTag());
			}

			for (Map.Entry<String, String> entry :
					_builder._headers.entrySet()) {

				httpInvoker.header(entry.getKey(), entry.getValue());
			}

			for (Map.Entry<String, String> entry :
					_builder._parameters.entrySet()) {

				httpInvoker.parameter(entry.getKey(), entry.getValue());
			}

			httpInvoker.httpMethod(HttpInvoker.HttpMethod.GET);

			httpInvoker.path(
				_builder._scheme + "://" + _builder._host + ":" +
					_builder._port + _builder._contextPath +
						"/o/test/v1.0/asset-libraries/{assetLibraryExternalReferenceCode}/erc-asset-library-test-entities/{ErcAssetLibraryTestEntityExternalReferenceCode}");

			httpInvoker.path(
				"ErcAssetLibraryTestEntityExternalReferenceCode",
				ErcAssetLibraryTestEntityExternalReferenceCode);
			httpInvoker.path(
				"assetLibraryExternalReferenceCode",
				assetLibraryExternalReferenceCode);

			if ((_builder._login != null) && (_builder._password != null)) {
				httpInvoker.userNameAndPassword(
					_builder._login + ":" + _builder._password);
			}

			return httpInvoker.invoke();
		}

		public ErcAssetLibraryTestEntity
				putAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityByExternalReferenceCode(
					String ErcAssetLibraryTestEntityExternalReferenceCode,
					String assetLibraryExternalReferenceCode,
					ErcAssetLibraryTestEntity ercAssetLibraryTestEntity)
			throws Exception {

			HttpInvoker.HttpResponse httpResponse =
				putAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityByExternalReferenceCodeHttpResponse(
					ErcAssetLibraryTestEntityExternalReferenceCode,
					assetLibraryExternalReferenceCode,
					ercAssetLibraryTestEntity);

			String content = httpResponse.getContent();

			if ((httpResponse.getStatusCode() / 100) != 2) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response content: " + content);
				_logger.log(
					Level.WARNING,
					"HTTP response message: " + httpResponse.getMessage());
				_logger.log(
					Level.WARNING,
					"HTTP response status code: " +
						httpResponse.getStatusCode());

				Problem.ProblemException problemException = null;

				if (Objects.equals(
						httpResponse.getContentType(), "application/json")) {

					problemException = new Problem.ProblemException(
						Problem.toDTO(content));
				}
				else {
					_logger.log(
						Level.WARNING,
						"Unable to process content type: " +
							httpResponse.getContentType());

					Problem problem = new Problem();

					problem.setStatus(
						String.valueOf(httpResponse.getStatusCode()));

					problemException = new Problem.ProblemException(problem);
				}

				throw problemException;
			}
			else {
				_logger.fine("HTTP response content: " + content);
				_logger.fine(
					"HTTP response message: " + httpResponse.getMessage());
				_logger.fine(
					"HTTP response status code: " +
						httpResponse.getStatusCode());
			}

			try {
				return ErcAssetLibraryTestEntitySerDes.toDTO(content);
			}
			catch (Exception e) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response: " + content, e);

				throw new Problem.ProblemException(Problem.toDTO(content));
			}
		}

		public HttpInvoker.HttpResponse
				putAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityByExternalReferenceCodeHttpResponse(
					String ErcAssetLibraryTestEntityExternalReferenceCode,
					String assetLibraryExternalReferenceCode,
					ErcAssetLibraryTestEntity ercAssetLibraryTestEntity)
			throws Exception {

			HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

			httpInvoker.body(
				ercAssetLibraryTestEntity.toString(), "application/json");

			if (_builder._locale != null) {
				httpInvoker.header(
					"Accept-Language", _builder._locale.toLanguageTag());
			}

			for (Map.Entry<String, String> entry :
					_builder._headers.entrySet()) {

				httpInvoker.header(entry.getKey(), entry.getValue());
			}

			for (Map.Entry<String, String> entry :
					_builder._parameters.entrySet()) {

				httpInvoker.parameter(entry.getKey(), entry.getValue());
			}

			httpInvoker.httpMethod(HttpInvoker.HttpMethod.PUT);

			httpInvoker.path(
				_builder._scheme + "://" + _builder._host + ":" +
					_builder._port + _builder._contextPath +
						"/o/test/v1.0/asset-libraries/{assetLibraryExternalReferenceCode}/erc-asset-library-test-entities/{ErcAssetLibraryTestEntityExternalReferenceCode}");

			httpInvoker.path(
				"ErcAssetLibraryTestEntityExternalReferenceCode",
				ErcAssetLibraryTestEntityExternalReferenceCode);
			httpInvoker.path(
				"assetLibraryExternalReferenceCode",
				assetLibraryExternalReferenceCode);

			if ((_builder._login != null) && (_builder._password != null)) {
				httpInvoker.userNameAndPassword(
					_builder._login + ":" + _builder._password);
			}

			return httpInvoker.invoke();
		}

		public Page<ErcAssetLibraryTestEntity>
				getAssetLibraryErcAssetLibraryTestEntitiesPage(
					Long assetLibraryId)
			throws Exception {

			HttpInvoker.HttpResponse httpResponse =
				getAssetLibraryErcAssetLibraryTestEntitiesPageHttpResponse(
					assetLibraryId);

			String content = httpResponse.getContent();

			if ((httpResponse.getStatusCode() / 100) != 2) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response content: " + content);
				_logger.log(
					Level.WARNING,
					"HTTP response message: " + httpResponse.getMessage());
				_logger.log(
					Level.WARNING,
					"HTTP response status code: " +
						httpResponse.getStatusCode());

				Problem.ProblemException problemException = null;

				if (Objects.equals(
						httpResponse.getContentType(), "application/json")) {

					problemException = new Problem.ProblemException(
						Problem.toDTO(content));
				}
				else {
					_logger.log(
						Level.WARNING,
						"Unable to process content type: " +
							httpResponse.getContentType());

					Problem problem = new Problem();

					problem.setStatus(
						String.valueOf(httpResponse.getStatusCode()));

					problemException = new Problem.ProblemException(problem);
				}

				throw problemException;
			}
			else {
				_logger.fine("HTTP response content: " + content);
				_logger.fine(
					"HTTP response message: " + httpResponse.getMessage());
				_logger.fine(
					"HTTP response status code: " +
						httpResponse.getStatusCode());
			}

			try {
				return Page.of(content, ErcAssetLibraryTestEntitySerDes::toDTO);
			}
			catch (Exception e) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response: " + content, e);

				throw new Problem.ProblemException(Problem.toDTO(content));
			}
		}

		public HttpInvoker.HttpResponse
				getAssetLibraryErcAssetLibraryTestEntitiesPageHttpResponse(
					Long assetLibraryId)
			throws Exception {

			HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

			if (_builder._locale != null) {
				httpInvoker.header(
					"Accept-Language", _builder._locale.toLanguageTag());
			}

			for (Map.Entry<String, String> entry :
					_builder._headers.entrySet()) {

				httpInvoker.header(entry.getKey(), entry.getValue());
			}

			for (Map.Entry<String, String> entry :
					_builder._parameters.entrySet()) {

				httpInvoker.parameter(entry.getKey(), entry.getValue());
			}

			httpInvoker.httpMethod(HttpInvoker.HttpMethod.GET);

			httpInvoker.path(
				_builder._scheme + "://" + _builder._host + ":" +
					_builder._port + _builder._contextPath +
						"/o/test/v1.0/asset-libraries/{assetLibraryId}/erc-asset-library-test-entities");

			httpInvoker.path("assetLibraryId", assetLibraryId);

			if ((_builder._login != null) && (_builder._password != null)) {
				httpInvoker.userNameAndPassword(
					_builder._login + ":" + _builder._password);
			}

			return httpInvoker.invoke();
		}

		public void postAssetLibraryErcAssetLibraryTestEntitiesPageExportBatch(
				Long assetLibraryId, String callbackURL, String contentType,
				String fieldNames)
			throws Exception {

			HttpInvoker.HttpResponse httpResponse =
				postAssetLibraryErcAssetLibraryTestEntitiesPageExportBatchHttpResponse(
					assetLibraryId, callbackURL, contentType, fieldNames);

			String content = httpResponse.getContent();

			if ((httpResponse.getStatusCode() / 100) != 2) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response content: " + content);
				_logger.log(
					Level.WARNING,
					"HTTP response message: " + httpResponse.getMessage());
				_logger.log(
					Level.WARNING,
					"HTTP response status code: " +
						httpResponse.getStatusCode());

				Problem.ProblemException problemException = null;

				if (Objects.equals(
						httpResponse.getContentType(), "application/json")) {

					problemException = new Problem.ProblemException(
						Problem.toDTO(content));
				}
				else {
					_logger.log(
						Level.WARNING,
						"Unable to process content type: " +
							httpResponse.getContentType());

					Problem problem = new Problem();

					problem.setStatus(
						String.valueOf(httpResponse.getStatusCode()));

					problemException = new Problem.ProblemException(problem);
				}

				throw problemException;
			}
			else {
				_logger.fine("HTTP response content: " + content);
				_logger.fine(
					"HTTP response message: " + httpResponse.getMessage());
				_logger.fine(
					"HTTP response status code: " +
						httpResponse.getStatusCode());
			}
		}

		public HttpInvoker.HttpResponse
				postAssetLibraryErcAssetLibraryTestEntitiesPageExportBatchHttpResponse(
					Long assetLibraryId, String callbackURL, String contentType,
					String fieldNames)
			throws Exception {

			HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

			httpInvoker.body("[]", "application/json");

			if (_builder._locale != null) {
				httpInvoker.header(
					"Accept-Language", _builder._locale.toLanguageTag());
			}

			for (Map.Entry<String, String> entry :
					_builder._headers.entrySet()) {

				httpInvoker.header(entry.getKey(), entry.getValue());
			}

			for (Map.Entry<String, String> entry :
					_builder._parameters.entrySet()) {

				httpInvoker.parameter(entry.getKey(), entry.getValue());
			}

			httpInvoker.httpMethod(HttpInvoker.HttpMethod.POST);

			if (callbackURL != null) {
				httpInvoker.parameter(
					"callbackURL", String.valueOf(callbackURL));
			}

			if (contentType != null) {
				httpInvoker.parameter(
					"contentType", String.valueOf(contentType));
			}

			if (fieldNames != null) {
				httpInvoker.parameter("fieldNames", String.valueOf(fieldNames));
			}

			httpInvoker.path(
				_builder._scheme + "://" + _builder._host + ":" +
					_builder._port + _builder._contextPath +
						"/o/test/v1.0/asset-libraries/{assetLibraryId}/erc-asset-library-test-entities/export-batch");

			httpInvoker.path("assetLibraryId", assetLibraryId);

			if ((_builder._login != null) && (_builder._password != null)) {
				httpInvoker.userNameAndPassword(
					_builder._login + ":" + _builder._password);
			}

			return httpInvoker.invoke();
		}

		public ErcAssetLibraryTestEntity
				postAssetLibraryErcAssetLibraryTestEntity(
					Long assetLibraryId,
					ErcAssetLibraryTestEntity ercAssetLibraryTestEntity)
			throws Exception {

			HttpInvoker.HttpResponse httpResponse =
				postAssetLibraryErcAssetLibraryTestEntityHttpResponse(
					assetLibraryId, ercAssetLibraryTestEntity);

			String content = httpResponse.getContent();

			if ((httpResponse.getStatusCode() / 100) != 2) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response content: " + content);
				_logger.log(
					Level.WARNING,
					"HTTP response message: " + httpResponse.getMessage());
				_logger.log(
					Level.WARNING,
					"HTTP response status code: " +
						httpResponse.getStatusCode());

				Problem.ProblemException problemException = null;

				if (Objects.equals(
						httpResponse.getContentType(), "application/json")) {

					problemException = new Problem.ProblemException(
						Problem.toDTO(content));
				}
				else {
					_logger.log(
						Level.WARNING,
						"Unable to process content type: " +
							httpResponse.getContentType());

					Problem problem = new Problem();

					problem.setStatus(
						String.valueOf(httpResponse.getStatusCode()));

					problemException = new Problem.ProblemException(problem);
				}

				throw problemException;
			}
			else {
				_logger.fine("HTTP response content: " + content);
				_logger.fine(
					"HTTP response message: " + httpResponse.getMessage());
				_logger.fine(
					"HTTP response status code: " +
						httpResponse.getStatusCode());
			}

			try {
				return ErcAssetLibraryTestEntitySerDes.toDTO(content);
			}
			catch (Exception e) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response: " + content, e);

				throw new Problem.ProblemException(Problem.toDTO(content));
			}
		}

		public HttpInvoker.HttpResponse
				postAssetLibraryErcAssetLibraryTestEntityHttpResponse(
					Long assetLibraryId,
					ErcAssetLibraryTestEntity ercAssetLibraryTestEntity)
			throws Exception {

			HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

			httpInvoker.body(
				ercAssetLibraryTestEntity.toString(), "application/json");

			if (_builder._locale != null) {
				httpInvoker.header(
					"Accept-Language", _builder._locale.toLanguageTag());
			}

			for (Map.Entry<String, String> entry :
					_builder._headers.entrySet()) {

				httpInvoker.header(entry.getKey(), entry.getValue());
			}

			for (Map.Entry<String, String> entry :
					_builder._parameters.entrySet()) {

				httpInvoker.parameter(entry.getKey(), entry.getValue());
			}

			httpInvoker.httpMethod(HttpInvoker.HttpMethod.POST);

			httpInvoker.path(
				_builder._scheme + "://" + _builder._host + ":" +
					_builder._port + _builder._contextPath +
						"/o/test/v1.0/asset-libraries/{assetLibraryId}/erc-asset-library-test-entities");

			httpInvoker.path("assetLibraryId", assetLibraryId);

			if ((_builder._login != null) && (_builder._password != null)) {
				httpInvoker.userNameAndPassword(
					_builder._login + ":" + _builder._password);
			}

			return httpInvoker.invoke();
		}

		public void postAssetLibraryErcAssetLibraryTestEntityBatch(
				Long assetLibraryId, String callbackURL, Object object)
			throws Exception {

			HttpInvoker.HttpResponse httpResponse =
				postAssetLibraryErcAssetLibraryTestEntityBatchHttpResponse(
					assetLibraryId, callbackURL, object);

			String content = httpResponse.getContent();

			if ((httpResponse.getStatusCode() / 100) != 2) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response content: " + content);
				_logger.log(
					Level.WARNING,
					"HTTP response message: " + httpResponse.getMessage());
				_logger.log(
					Level.WARNING,
					"HTTP response status code: " +
						httpResponse.getStatusCode());

				Problem.ProblemException problemException = null;

				if (Objects.equals(
						httpResponse.getContentType(), "application/json")) {

					problemException = new Problem.ProblemException(
						Problem.toDTO(content));
				}
				else {
					_logger.log(
						Level.WARNING,
						"Unable to process content type: " +
							httpResponse.getContentType());

					Problem problem = new Problem();

					problem.setStatus(
						String.valueOf(httpResponse.getStatusCode()));

					problemException = new Problem.ProblemException(problem);
				}

				throw problemException;
			}
			else {
				_logger.fine("HTTP response content: " + content);
				_logger.fine(
					"HTTP response message: " + httpResponse.getMessage());
				_logger.fine(
					"HTTP response status code: " +
						httpResponse.getStatusCode());
			}
		}

		public HttpInvoker.HttpResponse
				postAssetLibraryErcAssetLibraryTestEntityBatchHttpResponse(
					Long assetLibraryId, String callbackURL, Object object)
			throws Exception {

			HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

			httpInvoker.body(object.toString(), "application/json");

			if (_builder._locale != null) {
				httpInvoker.header(
					"Accept-Language", _builder._locale.toLanguageTag());
			}

			for (Map.Entry<String, String> entry :
					_builder._headers.entrySet()) {

				httpInvoker.header(entry.getKey(), entry.getValue());
			}

			for (Map.Entry<String, String> entry :
					_builder._parameters.entrySet()) {

				httpInvoker.parameter(entry.getKey(), entry.getValue());
			}

			httpInvoker.httpMethod(HttpInvoker.HttpMethod.POST);

			if (callbackURL != null) {
				httpInvoker.parameter(
					"callbackURL", String.valueOf(callbackURL));
			}

			httpInvoker.path(
				_builder._scheme + "://" + _builder._host + ":" +
					_builder._port + _builder._contextPath +
						"/o/test/v1.0/asset-libraries/{assetLibraryId}/erc-asset-library-test-entities/batch");

			httpInvoker.path("assetLibraryId", assetLibraryId);

			if ((_builder._login != null) && (_builder._password != null)) {
				httpInvoker.userNameAndPassword(
					_builder._login + ":" + _builder._password);
			}

			return httpInvoker.invoke();
		}

		private ErcAssetLibraryTestEntityResourceImpl(Builder builder) {
			_builder = builder;
		}

		private static final Logger _logger = Logger.getLogger(
			ErcAssetLibraryTestEntityResource.class.getName());

		private Builder _builder;

	}

}