/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.cache.test;

import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.vulcan.application.HeadlessApplicationProvider;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Petteri Karttunen
 */
public abstract class BaseOpenAPIDocumentCacheTestCase {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(
			BaseOpenAPIDocumentCacheTestCase.class);

		BundleContext bundleContext = bundle.getBundleContext();

		basePath = "/test-vulcan-application-" + RandomTestUtil.randomString();

		String name = "Test.Vulcan." + RandomTestUtil.randomString();

		_serviceRegistrations = Arrays.asList(
			bundleContext.registerService(
				Application.class, new TestApplication(),
				HashMapDictionaryBuilder.<String, Object>put(
					"liferay.auth.verifier", true
				).put(
					"liferay.oauth2", false
				).put(
					"osgi.jaxrs.application.base", basePath
				).put(
					"osgi.jaxrs.extension.select",
					"(osgi.jaxrs.name=Liferay.Vulcan)"
				).put(
					"osgi.jaxrs.name", name
				).build()),
			bundleContext.registerService(
				TestResource_v1_0.class, testResource,
				HashMapDictionaryBuilder.<String, Object>put(
					"api.version", "v1.0"
				).put(
					"openapi.resource", "true"
				).put(
					"openapi.resource.path", basePath
				).put(
					"osgi.jaxrs.application.select",
					"(osgi.jaxrs.name=" + name + ")"
				).put(
					"osgi.jaxrs.resource", "true"
				).build()));

		openAPIDocument = _getOpenAPIDocument();
	}

	@After
	public void tearDown() {
		_serviceRegistrations.forEach(ServiceRegistration::unregister);
	}

	public static class TestApplication extends Application {

		@Override
		public Set<Object> getSingletons() {
			return Collections.singleton(this);
		}

	}

	@Path("/v1.0")
	public static class TestResource_v1_0 {

		public int getCount() {
			return _count.get();
		}

		@GET
		@Path("/openapi.{type:json|yaml}")
		@Produces(MediaType.APPLICATION_JSON)
		public Response getOpenAPI(
			HttpServletRequest httpServletRequest,
			@PathParam("type") String type, UriInfo uriInfo) {

			if (_runnable != null) {
				_runnable.run();
			}

			return Response.ok(
				"{\"openapi\": \"" + _count.incrementAndGet() + "\"}"
			).build();
		}

		public void setRunnable(Runnable runnable) {
			_runnable = runnable;
		}

		private final AtomicInteger _count = new AtomicInteger();
		private Runnable _runnable;

	}

	protected String getContentString(String serverURL) {
		return openAPIDocument.getContentString(
			serverURL, HeadlessApplicationProvider.OpenAPIDocument.Type.JSON);
	}

	protected String basePath;

	@Inject
	protected HeadlessApplicationProvider headlessApplicationProvider;

	protected HeadlessApplicationProvider.OpenAPIDocument openAPIDocument;
	protected final TestResource_v1_0 testResource = new TestResource_v1_0();

	private HeadlessApplicationProvider.OpenAPIDocument _getOpenAPIDocument()
		throws Exception {

		for (HeadlessApplicationProvider.Application application :
				headlessApplicationProvider.getApplications(
					TestPropsValues.getCompanyId())) {

			if (!Objects.equals(application.getBasePath(), basePath)) {
				continue;
			}

			List<HeadlessApplicationProvider.OpenAPIDocument> openAPIDocuments =
				application.getOpenAPIDocuments();

			return openAPIDocuments.get(0);
		}

		return null;
	}

	private List<ServiceRegistration<?>> _serviceRegistrations;

}