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

package com.liferay.headless.builder.internal.generator.operation;

import com.liferay.portal.kernel.util.Http;

/**
 * @author Carlos Correa
 * @author Luis Miguel Barcos
 */
public interface Operation {

	public long getCompanyId();

	public String getKey();

	public Http.Method getMethod();

	public String getPath();

	public static class Builder {

		public Operation build() {
			return new OperationImpl(this);
		}

		public Builder setCompanyId(long companyId) {
			_companyId = companyId;

			return this;
		}

		public Builder setMethod(Http.Method method) {
			_method = method;

			return this;
		}

		public Builder setPath(String path) {
			_path = path;

			return this;
		}

		protected long getCompanyId() {
			return _companyId;
		}

		protected Http.Method getMethod() {
			return _method;
		}

		protected String getPath() {
			return _path;
		}

		private long _companyId;
		private Http.Method _method;
		private String _path;

	}

}