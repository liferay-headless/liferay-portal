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

package com.liferay.headless.builder.internal.generator.jaxrs.application;

import com.liferay.headless.builder.internal.generator.application.ApiApplication;

import javax.ws.rs.core.Application;

/**
 * @author Luis Miguel Barcos
 */
public class HeadlessBuilderApplication extends Application {

	public HeadlessBuilderApplication(ApiApplication apiApplication) {
		_apiApplication = apiApplication;
	}

	public ApiApplication getApiApplication() {
		return _apiApplication;
	}

	private final ApiApplication _apiApplication;

}