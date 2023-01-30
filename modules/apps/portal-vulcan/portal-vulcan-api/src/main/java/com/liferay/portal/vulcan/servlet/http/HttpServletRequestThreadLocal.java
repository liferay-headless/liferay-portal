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

package com.liferay.portal.vulcan.servlet.http;

import com.liferay.petra.lang.CentralizedThreadLocal;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Matija Petanjek
 */
public class HttpServletRequestThreadLocal {

	public static HttpServletRequest getHttpServletRequest() {
		return _httpServletRequestThreadLocal.get();
	}

	public static void setHttpServletRequest(
		HttpServletRequest httpServletRequest) {

		_httpServletRequestThreadLocal.set(httpServletRequest);
	}

	private static final ThreadLocal<HttpServletRequest>
		_httpServletRequestThreadLocal = new CentralizedThreadLocal<>(
			HttpServletRequestThreadLocal.class +
				"._httpServletRequestThreadLocal");

}