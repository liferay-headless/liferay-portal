/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.jaxrs.container.response.filter;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.Response;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author Luis Ortiz
 */
public class StatusContainerResponseFilterTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testFilter() throws Exception {
		Mockito.when(
			_containerResponseContext.getStatusInfo()
		).thenReturn(
			Response.Status.OK
		);

		_statusContainerResponseFilter.filter(
			_containerRequestContext, _containerResponseContext);

		Mockito.verify(
			_containerResponseContext
		).setStatus(
			_STATUS.getStatusCode()
		);
	}

	@Test
	public void testFilterWithClientErrorStatus() throws Exception {
		Mockito.when(
			_containerResponseContext.getStatusInfo()
		).thenReturn(
			Response.Status.CONFLICT
		);

		_statusContainerResponseFilter.filter(
			_containerRequestContext, _containerResponseContext);

		Mockito.verify(
			_containerResponseContext, Mockito.never()
		).setStatus(
			Mockito.anyInt()
		);
	}

	@Test
	public void testFilterWithServerErrorStatus() throws Exception {
		Mockito.when(
			_containerResponseContext.getStatusInfo()
		).thenReturn(
			Response.Status.INTERNAL_SERVER_ERROR
		);

		_statusContainerResponseFilter.filter(
			_containerRequestContext, _containerResponseContext);

		Mockito.verify(
			_containerResponseContext, Mockito.never()
		).setStatus(
			Mockito.anyInt()
		);
	}

	private static final Response.Status _STATUS = Response.Status.ACCEPTED;

	@Mock
	private ContainerRequestContext _containerRequestContext;

	@Mock
	private ContainerResponseContext _containerResponseContext;

	private final StatusContainerResponseFilter _statusContainerResponseFilter =
		new StatusContainerResponseFilter(_STATUS);

}