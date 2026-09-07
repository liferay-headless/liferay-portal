/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.template;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.portal.vulcan.http.VulcanRequestForwarder;
import com.liferay.portal.vulcan.internal.template.RESTClientTemplateContextContributor.RESTClient;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Gabor Komaromi
 */
public class RESTClientTemplateContextContributorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_vulcanRequestForwarder = Mockito.mock(VulcanRequestForwarder.class);

		VulcanRequestForwarder.Response response = _response(
			HttpServletResponse.SC_OK);

		Mockito.when(
			_vulcanRequestForwarder.forward(Mockito.any(), Mockito.any())
		).thenReturn(
			response
		);
	}

	@Test
	public void testGetDoesNotCacheErrorResponse() throws Exception {
		VulcanRequestForwarder.Response response = _response(
			HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		Mockito.when(
			_vulcanRequestForwarder.forward(Mockito.any(), Mockito.any())
		).thenReturn(
			response
		);

		RESTClient restClient = _createRESTClient(_createHttpServletRequest());

		restClient.get("/path");
		restClient.get("/path");

		Mockito.verify(
			_vulcanRequestForwarder, Mockito.times(2)
		).forward(
			Mockito.any(), Mockito.any()
		);
	}

	@Test
	public void testGetForwardsOncePerPathWithinRender() throws Exception {
		RESTClient restClient = _createRESTClient(_createHttpServletRequest());

		restClient.get("/path");
		restClient.get("/path");

		Mockito.verify(
			_vulcanRequestForwarder, Mockito.times(1)
		).forward(
			Mockito.any(), Mockito.any()
		);
	}

	@Test
	public void testGetForwardsPerDistinctPath() throws Exception {
		RESTClient restClient = _createRESTClient(_createHttpServletRequest());

		restClient.get("/path");
		restClient.get("/other");

		Mockito.verify(
			_vulcanRequestForwarder, Mockito.times(2)
		).forward(
			Mockito.any(), Mockito.any()
		);
	}

	@Test
	public void testGetIsolatesCacheAcrossRequests() throws Exception {
		RESTClient restClient1 = _createRESTClient(_createHttpServletRequest());
		RESTClient restClient2 = _createRESTClient(_createHttpServletRequest());

		restClient1.get("/path");
		restClient2.get("/path");

		Mockito.verify(
			_vulcanRequestForwarder, Mockito.times(2)
		).forward(
			Mockito.any(), Mockito.any()
		);
	}

	@Test
	public void testGetSharesCacheAcrossRESTClientsOnSameRequest()
		throws Exception {

		HttpServletRequest httpServletRequest = _createHttpServletRequest();

		RESTClient restClient1 = _createRESTClient(httpServletRequest);
		RESTClient restClient2 = _createRESTClient(httpServletRequest);

		restClient1.get("/path");
		restClient2.get("/path");

		Mockito.verify(
			_vulcanRequestForwarder, Mockito.times(1)
		).forward(
			Mockito.any(), Mockito.any()
		);
	}

	private HttpServletRequest _createHttpServletRequest() {
		HttpServletRequest httpServletRequest = Mockito.mock(
			HttpServletRequest.class);

		Map<String, Object> attributes = new HashMap<>();

		Mockito.when(
			httpServletRequest.getAttribute(Mockito.anyString())
		).thenAnswer(
			invocation -> attributes.get(invocation.getArgument(0))
		);

		Mockito.doAnswer(
			invocation -> {
				attributes.put(
					invocation.getArgument(0), invocation.getArgument(1));

				return null;
			}
		).when(
			httpServletRequest
		).setAttribute(
			Mockito.anyString(), Mockito.any()
		);

		return httpServletRequest;
	}

	private RESTClient _createRESTClient(
		HttpServletRequest httpServletRequest) {

		RESTClientTemplateContextContributor
			restClientTemplateContextContributor =
				new RESTClientTemplateContextContributor();

		ReflectionTestUtil.setFieldValue(
			restClientTemplateContextContributor, "_vulcanRequestForwarder",
			_vulcanRequestForwarder);

		Map<String, Object> contextObjects = new HashMap<>();

		restClientTemplateContextContributor.prepare(
			contextObjects, httpServletRequest);

		return (RESTClient)contextObjects.get("restClient");
	}

	private VulcanRequestForwarder.Response _response(int statusCode) {
		VulcanRequestForwarder.Response response = Mockito.mock(
			VulcanRequestForwarder.Response.class);

		Mockito.when(
			response.getContent()
		).thenReturn(
			"content"
		);

		Mockito.when(
			response.getStatusCode()
		).thenReturn(
			statusCode
		);

		return response;
	}

	private VulcanRequestForwarder _vulcanRequestForwarder;

}