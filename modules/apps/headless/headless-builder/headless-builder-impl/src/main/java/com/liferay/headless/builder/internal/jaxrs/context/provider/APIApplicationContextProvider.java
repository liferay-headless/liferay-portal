/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.builder.internal.jaxrs.context.provider;

import com.liferay.headless.builder.application.APIApplication;
import com.liferay.headless.builder.internal.application.registry.ApiApplicationRegistry;
import com.liferay.headless.builder.internal.util.PathUtil;
import com.liferay.portal.kernel.util.PortalUtil;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ext.Provider;

import org.apache.cxf.jaxrs.ext.ContextProvider;
import org.apache.cxf.message.Message;

/**
 * @author Luis Miguel Barcos
 */
@Provider
public class APIApplicationContextProvider
	implements ContextProvider<APIApplication> {

	public APIApplicationContextProvider(
		ApiApplicationRegistry apiApplicationRegistry) {

		_apiApplicationRegistry = apiApplicationRegistry;
	}

	@Override
	public APIApplication createContext(Message message) {
		HttpServletRequest httpServletRequest = _getHttpServletRequest(message);

		APIApplication apiApplication =
			_apiApplicationRegistry.fetchApiApplication(
				_getBaseURL(httpServletRequest),
				PortalUtil.getCompanyId(httpServletRequest));

		if (apiApplication != null) {
			return apiApplication;
		}

		throw new NotFoundException();
	}

	private String _getBaseURL(HttpServletRequest httpServletRequest) {
		return PathUtil.removeBasePath(httpServletRequest.getContextPath());
	}

	private HttpServletRequest _getHttpServletRequest(Message message) {
		return (HttpServletRequest)message.getContextualProperty(
			"HTTP.REQUEST");
	}

	private final ApiApplicationRegistry _apiApplicationRegistry;

}