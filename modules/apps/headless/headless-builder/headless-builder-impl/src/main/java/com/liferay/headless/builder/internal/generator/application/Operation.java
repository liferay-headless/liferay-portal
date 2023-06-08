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

package com.liferay.headless.builder.internal.generator.application;

import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.StringUtil;

/**
 * @author Luis Miguel Barcos
 */
public class Operation {

	public Operation(Builder builder) {
		_builder = builder;
	}

	public Http.Method getMethod() {
		return _builder._method;
	}

	public String getPath() {
		return _builder._path;
	}

	public Scope getScope() {
		return _builder._scope;
	}

	public static class Builder {

		public Operation build() {
			return new Operation(this);
		}

		public Builder setMethod(String method) {
			if (StringUtil.toUpperCase(
					method
				).equals(
					"GET"
				)) {

				_method = Http.Method.GET;
			}
			else {
				throw new UnsupportedOperationException("Scope not supported");
			}

			return this;
		}

		public Builder setPath(String path) {
			_path = path;

			return this;
		}

		public Builder setScope(String scope) {
			String lowerCase = StringUtil.toLowerCase(scope);

			if (lowerCase.equals("instance")) {
				_scope = Scope.INSTANCE;
			}
			else if (lowerCase.equals("site")) {
				_scope = Scope.SITE;
			}
			else {
				throw new UnsupportedOperationException("Scope not supported");
			}

			return this;
		}

		private Http.Method _method;
		private String _path;
		private Scope _scope;

	}

	public enum Scope {

		INSTANCE, SITE

	}

	private final Builder _builder;

}