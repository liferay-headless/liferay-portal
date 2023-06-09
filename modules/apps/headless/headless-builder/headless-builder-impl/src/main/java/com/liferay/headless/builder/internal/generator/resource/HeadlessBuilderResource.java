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

package com.liferay.headless.builder.internal.generator.resource;

import com.liferay.headless.builder.internal.generator.application.ApiApplication;
import com.liferay.headless.builder.internal.generator.application.Operation;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Carlos Correa
 * @author Luis Miguel Barcos
 */
public class HeadlessBuilderResource extends BaseHeadlessBuilderResource {

	public HeadlessBuilderResource(
		Portal portal,
		ServiceTracker<Application, ApiApplication> serviceTracker) {

		_portal = portal;
		_serviceTracker = serviceTracker;
	}

	@Override
	public Response get() throws Exception {
		Operation operation = _getOperation(
			_getCurrentApiApplication(contextHttpServletRequest),
			contextHttpServletRequest);

		// TODO At this point we find a operation that correspond with the
		//  proper request. Now it is necessary to extract the
		//  information from the Operation to handle the request and return
		//  the proper Entity.

		// TODO Delete this println. It is only to pass source formatter

		System.out.println(operation);

		return Response.ok(
		).build();
	}

	private ApiApplication _getCurrentApiApplication(
		HttpServletRequest httpServletRequest) {

		String currentApiApplicationBaseURL = _sanitizeURL(
			httpServletRequest.getContextPath());
		long currentCompanyId = _portal.getCompanyId(httpServletRequest);

		for (Object service : _serviceTracker.getServices()) {
			ApiApplication apiApplication = (ApiApplication)service;

			if (_isCurrentApiApplication(
					apiApplication, currentApiApplicationBaseURL,
					currentCompanyId)) {

				return apiApplication;
			}
		}

		throw new NotFoundException();
	}

	private Operation _getOperation(
		ApiApplication apiApplication, HttpServletRequest httpServletRequest) {

		String currentEndpoint = _sanitizeURL(
			httpServletRequest.getRequestURI());

		String currentEndpointPath = StringUtil.removeSubstring(
			currentEndpoint, apiApplication.getBaseURL());

		for (Operation operation : apiApplication.getOperations()) {
			if (Objects.equals(operation.getPath(), currentEndpointPath)) {
				return operation;
			}
		}

		throw new NotFoundException();
	}

	private boolean _isCurrentApiApplication(
		ApiApplication apiApplication, String baseURL, long companyId) {

		if (Objects.equals(apiApplication.getBaseURL(), baseURL) &&
			Objects.equals(apiApplication.getCompanyId(), companyId)) {

			return true;
		}

		return false;
	}

	private String _sanitizeURL(String url) {
		return StringUtil.removeSubstring(url, "/o/");
	}

	private final Portal _portal;
	private final ServiceTracker<Application, ApiApplication> _serviceTracker;

}