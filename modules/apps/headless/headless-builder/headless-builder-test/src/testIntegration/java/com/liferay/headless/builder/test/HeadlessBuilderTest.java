/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.headless.builder.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.headless.builder.application.HeadlessBuilderApplication;
import com.liferay.headless.builder.application.HeadlessBuilderApplicationFactory;
import com.liferay.headless.builder.application.HeadlessBuilderApplicationManager;
import com.liferay.headless.builder.test.info.item.provider.TestEntryInfoItemFieldValuesProvider;
import com.liferay.headless.builder.test.info.item.provider.TestEntryInfoItemFormProvider;
import com.liferay.headless.builder.test.info.item.provider.TestEntryInfoItemObjectProvider;
import com.liferay.headless.builder.test.model.TestEntry;
import com.liferay.headless.builder.test.object.util.ObjectDefinitionTestUtil;
import com.liferay.headless.builder.test.object.util.ObjectEntryTestUtil;
import com.liferay.headless.builder.test.object.util.ObjectRelationshipTestUtil;
import com.liferay.info.item.provider.InfoItemFieldValuesProvider;
import com.liferay.info.item.provider.InfoItemFormProvider;
import com.liferay.info.item.provider.InfoItemObjectProvider;
import com.liferay.object.constants.ObjectRelationshipConstants;
import com.liferay.object.field.util.ObjectFieldUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.vulcan.yaml.YAMLUtil;
import com.liferay.portal.vulcan.yaml.openapi.OpenAPIYAML;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;

import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.charset.StandardCharsets;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Application;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import org.skyscreamer.jsonassert.JSONAssert;

/**
 * @author Carlos Correa
 */
@RunWith(Arquillian.class)
public class HeadlessBuilderTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_apiApplicationObjectDefinition =
			ObjectDefinitionTestUtil.publishObjectDefinition(
				"MSOD_API_APPLICATION",
				new ArrayList<ObjectField>() {
					{
						add(
							ObjectFieldUtil.createObjectField(
								"Text", "String", true, true, null,
								RandomTestUtil.randomString(),
								_API_APPLICATION_TITLE, false));
						add(
							ObjectFieldUtil.createObjectField(
								"Text", "String", true, true, null,
								RandomTestUtil.randomString(),
								_API_APPLICATION_BASE_URL, false));
					}
				});

		_apiApplicationObjectEntry = ObjectEntryTestUtil.addObjectEntry(
			_apiApplicationObjectDefinition,
			HashMapBuilder.<String, Serializable>put(
				_API_APPLICATION_BASE_URL, _API_APPLICATION_BASE_URL_VALUE
			).put(
				_API_APPLICATION_TITLE, "apiApplication"
			).build());

		_apiEndpointObjectDefinition =
			ObjectDefinitionTestUtil.publishObjectDefinition(
				"MSOD_API_ENDPOINT",
				new ArrayList<ObjectField>() {
					{
						add(
							ObjectFieldUtil.createObjectField(
								"Text", "String", true, true, null,
								RandomTestUtil.randomString(),
								_API_ENDPOINT_HTTP_METHOD, false));
						add(
							ObjectFieldUtil.createObjectField(
								"Text", "String", true, true, null,
								RandomTestUtil.randomString(),
								_API_ENDPOINT_PATH, false));
						add(
							ObjectFieldUtil.createObjectField(
								"Text", "String", true, true, null,
								RandomTestUtil.randomString(),
								_API_ENDPOINT_SCOPE, false));
					}
				});

		_apiEndpointObjectEntry = ObjectEntryTestUtil.addObjectEntry(
			_apiEndpointObjectDefinition,
			HashMapBuilder.<String, Serializable>put(
				_API_ENDPOINT_HTTP_METHOD, "GET"
			).put(
				_API_ENDPOINT_PATH, "/new-path"
			).put(
				_API_ENDPOINT_SCOPE, "Instance"
			).build());

		_applicationEndpointsObjectRelationship =
			ObjectRelationshipTestUtil.addObjectRelationship(
				_apiApplicationObjectDefinition, "applicationEndpoints",
				_apiEndpointObjectDefinition, TestPropsValues.getUserId(),
				ObjectRelationshipConstants.TYPE_ONE_TO_MANY);

		ObjectRelationshipTestUtil.relateObjectEntries(
			_apiApplicationObjectEntry.getObjectEntryId(),
			_apiEndpointObjectEntry.getObjectEntryId(),
			_applicationEndpointsObjectRelationship,
			TestPropsValues.getUserId());

		Bundle bundle = FrameworkUtil.getBundle(HeadlessBuilderTest.class);

		BundleContext bundleContext = bundle.getBundleContext();

		_infoItemFieldValuesProviderServiceRegistration =
			bundleContext.registerService(
				InfoItemFieldValuesProvider.class,
				new TestEntryInfoItemFieldValuesProvider(), null);
		_infoItemFormProviderServiceRegistration =
			bundleContext.registerService(
				InfoItemFormProvider.class, new TestEntryInfoItemFormProvider(),
				null);
		_infoItemObjectProviderServiceRegistration =
			bundleContext.registerService(
				InfoItemObjectProvider.class,
				new TestEntryInfoItemObjectProvider(), null);
	}

	@After
	public void tearDown() throws Exception {
		_infoItemFieldValuesProviderServiceRegistration.unregister();
		_infoItemFormProviderServiceRegistration.unregister();
		_infoItemObjectProviderServiceRegistration.unregister();

		_objectRelationshipLocalService.deleteObjectRelationship(
			_applicationEndpointsObjectRelationship);
		_objectDefinitionLocalService.deleteObjectDefinition(
			_apiApplicationObjectDefinition);
		_objectDefinitionLocalService.deleteObjectDefinition(
			_apiEndpointObjectDefinition);
	}

	@FeatureFlags("LPS-171047")
	@Test
	public void testHeadlessBuilderApplication() throws Exception {
		_withHeadlessBuilderApplication(
			TestPropsValues.getCompanyId(),
			() -> {
				JSONObject jsonObject = _invoke(
					"headless-builder/v1.0/test-entries/" +
						_testEntry.getTestEntryId(),
					Http.Method.GET);

				JSONAssert.assertEquals(
					JSONUtil.put(
						"date", _formatDate(_testEntry.getDateField())
					).put(
						"number", (int)_testEntry.getLongField()
					).toString(),
					jsonObject.toString(), true);
			});
	}

	@FeatureFlags("LPS-171047")
	@Test
	public void testHeadlessBuilderApplicationOnADifferentCompany()
		throws Exception {

		_withHeadlessBuilderApplication(
			0,
			() -> {
				JSONObject jsonObject = _invoke(
					"headless-builder/v1.0/test-entries/" +
						_testEntry.getTestEntryId(),
					Http.Method.GET);

				JSONAssert.assertEquals(
					JSONUtil.put(
						"status", "NOT_FOUND"
					).put(
						"title", "The operation could not be found."
					).toString(),
					jsonObject.toString(), true);
			});
	}

	@Test
	public void testHeadlessBuilderApplicationWithoutFeatureFlag()
		throws Exception {

		HttpURLConnection httpURLConnection = _createHttpURLConnection(
			"headless-builder/v1.0/test-entries/" + _testEntry.getTestEntryId(),
			Http.Method.GET);

		httpURLConnection.connect();

		Assert.assertEquals(404, httpURLConnection.getResponseCode());
	}

	@FeatureFlags("LPS-171047")
	@Test
	public void testMissingHeadlessBuilderApplication() throws Exception {
		JSONObject jsonObject = _invoke(
			"headless-builder/v1.0/test-entries/1234", Http.Method.GET);

		JSONAssert.assertEquals(
			JSONUtil.put(
				"status", "NOT_FOUND"
			).put(
				"title", "The operation could not be found."
			).toString(),
			jsonObject.toString(), true);
	}

	@Test
	public void testPublishApiApplication() throws Exception {
		CountDownLatch addedCountLatch = new CountDownLatch(1);

		Bundle bundle = FrameworkUtil.getBundle(getClass());

		BundleContext bundleContext = bundle.getBundleContext();

		ServiceTracker<?, ?> serviceTracker =
			new ServiceTracker<Application, Application>(
				bundleContext, Application.class, null) {

				@Override
				public Application addingService(
					ServiceReference<Application> serviceReference) {

					String property = (String)serviceReference.getProperty(
						"osgi.jaxrs.application.base");

					if (property.contains(_API_APPLICATION_BASE_URL_VALUE)) {
						addedCountLatch.countDown();

						return super.addingService(serviceReference);
					}

					return null;
				}

			};

		try {
			serviceTracker.open();

			_headlessBuilderApplicationManager.publishApplication(
				_apiApplicationObjectEntry.getExternalReferenceCode());

			addedCountLatch.await(1, TimeUnit.MINUTES);

			HttpURLConnection httpURLConnection = _createHttpURLConnection(
				_API_APPLICATION_BASE_URL_VALUE, Http.Method.GET);

			httpURLConnection.connect();

			Assert.assertEquals(200, httpURLConnection.getResponseCode());
		}
		finally {
			serviceTracker.close();
			_headlessBuilderApplicationManager.unpublishApplication();
		}
	}

	private HttpURLConnection _createHttpURLConnection(
			String endpoint, Http.Method method)
		throws Exception {

		URL url = new URL("http://localhost:8080/o/" + endpoint);

		HttpURLConnection httpURLConnection =
			(HttpURLConnection)url.openConnection();

		httpURLConnection.setRequestProperty(HttpHeaders.ACCEPT, "*/*");

		httpURLConnection.setRequestProperty(
			HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON);

		String encodedUserNameAndPassword = Base64.encode(
			"test@liferay.com:test".getBytes(StandardCharsets.UTF_8));

		httpURLConnection.setRequestProperty(
			"Authorization", "Basic " + encodedUserNameAndPassword);

		httpURLConnection.setRequestMethod(method.toString());

		return httpURLConnection;
	}

	private String _formatDate(Date date) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");

		return simpleDateFormat.format(date);
	}

	private JSONObject _invoke(String endpoint, Http.Method method)
		throws Exception {

		HttpURLConnection httpURLConnection = _createHttpURLConnection(
			endpoint, method);

		httpURLConnection.connect();

		try {
			return JSONFactoryUtil.createJSONObject(
				StringUtil.read(httpURLConnection.getInputStream()));
		}
		catch (FileNotFoundException fileNotFoundException) {
			return JSONFactoryUtil.createJSONObject(
				StringUtil.read(httpURLConnection.getErrorStream()));
		}
	}

	private OpenAPIYAML _readOpenAPIYAML(String yamlFile) throws Exception {
		try (InputStream inputStream = getClass().getResourceAsStream(
				yamlFile)) {

			return YAMLUtil.loadOpenAPIYAML(StringUtil.read(inputStream));
		}
	}

	private void _withHeadlessBuilderApplication(
			long companyId, UnsafeRunnable<Exception> unsafeRunnable)
		throws Exception {

		HeadlessBuilderApplication headlessBuilderApplication =
			_headlessBuilderApplicationFactory.getHeadlessBuilderApplication(
				companyId, _readOpenAPIYAML("/rest-openapi.yaml"));

		HeadlessBuilderApplication.Handle handle =
			headlessBuilderApplication.deploy();

		try {
			unsafeRunnable.run();
		}
		finally {
			handle.undeploy();
		}
	}

	private static final String _API_APPLICATION_BASE_URL = "baseURL";

	private static final String _API_APPLICATION_BASE_URL_VALUE = "base-url";

	private static final String _API_APPLICATION_TITLE = "title";

	private static final String _API_ENDPOINT_HTTP_METHOD = "hTTPMethod";

	private static final String _API_ENDPOINT_PATH = "path";

	private static final String _API_ENDPOINT_SCOPE = "scope";

	private ObjectDefinition _apiApplicationObjectDefinition;
	private ObjectEntry _apiApplicationObjectEntry;
	private ObjectDefinition _apiEndpointObjectDefinition;
	private ObjectEntry _apiEndpointObjectEntry;
	private ObjectRelationship _applicationEndpointsObjectRelationship;

	@Inject
	private HeadlessBuilderApplicationFactory
		_headlessBuilderApplicationFactory;

	@Inject
	private HeadlessBuilderApplicationManager
		_headlessBuilderApplicationManager;

	private ServiceRegistration<?>
		_infoItemFieldValuesProviderServiceRegistration;
	private ServiceRegistration<?> _infoItemFormProviderServiceRegistration;
	private ServiceRegistration<?> _infoItemObjectProviderServiceRegistration;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

	private final TestEntry _testEntry = TestEntry.INSTANCE;

}