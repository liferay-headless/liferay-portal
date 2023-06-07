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

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.Http;

import java.util.Objects;

/**
 * @author Luis Miguel Barcos
 */
public class OperationImpl implements Operation {

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof OperationImpl)) {
			return false;
		}

		OperationImpl operationImpl = (OperationImpl)object;

		if (Objects.equals(getMethod(), operationImpl.getMethod()) &&
			Objects.equals(getPath(), operationImpl.getPath()) &&
			Objects.equals(getCompanyId(), operationImpl.getCompanyId())) {

			return true;
		}

		return false;
	}

	@Override
	public long getCompanyId() {
		return _builder.getCompanyId();
	}

	@Override
	public String getKey() {
		return StringBundler.concat(
			getCompanyId(), StringPool.POUND, getMethod().name(),
			StringPool.POUND, getPath());
	}

	@Override
	public Http.Method getMethod() {
		return _builder.getMethod();
	}

	@Override
	public String getPath() {
		return _builder.getPath();
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	protected OperationImpl(Builder builder) {
		_builder = builder;
	}

	private final Builder _builder;

}