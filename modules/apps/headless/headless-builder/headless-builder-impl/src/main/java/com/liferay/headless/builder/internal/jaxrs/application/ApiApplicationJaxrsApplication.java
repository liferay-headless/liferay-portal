/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.builder.internal.jaxrs.application;

import com.liferay.headless.builder.application.APIApplication;

import javax.ws.rs.core.Application;

/**
 * @author Luis Miguel Barcos
 */
public class ApiApplicationJaxrsApplication extends Application {

	public ApiApplicationJaxrsApplication(APIApplication apiApplication) {
		_apiApplication = apiApplication;
	}

	public APIApplication getApiApplication() {
		return _apiApplication;
	}

	private final APIApplication _apiApplication;

}