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

package com.liferay.portal.vulcan.graphql.validation;

import java.lang.reflect.Method;

/**
 * @author Carlos Correa
 */
public class GraphQLRequestContext {

	public String getApplicationName() {
		return _applicationName;
	}

	public long getCompanyId() {
		return _companyId;
	}

	public String getHttpMethod() {
		return _httpMethod;
	}

	public Method getMethod() {
		return _method;
	}

	public Class<?> getResourceClass() {
		return _resourceClass;
	}

	public void setApplicationName(String applicationName) {
		_applicationName = applicationName;
	}

	public void setCompanyId(long companyId) {
		_companyId = companyId;
	}

	public void setHttpMethod(String httpMethod) {
		_httpMethod = httpMethod;
	}

	public void setMethod(Method method) {
		_method = method;
	}

	public void setResourceClass(Class<?> resourceClass) {
		_resourceClass = resourceClass;
	}

	private String _applicationName;
	private long _companyId;
	private String _httpMethod;
	private Method _method;
	private Class<?> _resourceClass;

}