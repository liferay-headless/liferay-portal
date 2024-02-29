/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.writer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.Objects;

/**
 * @author Carlos Correa
 */
public class FieldValueExtractor {

	public FieldValueExtractor(Field field, Method method) {
		_field = field;
		_method = method;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if ((object == null) || (getClass() != object.getClass())) {
			return false;
		}

		FieldValueExtractor that = (FieldValueExtractor)object;

		if (Objects.equals(_field, that._field) &&
			Objects.equals(_method, that._method)) {

			return true;
		}

		return false;
	}

	public Object extract(Object item) throws Exception {
		if (_method != null) {
			return _method.invoke(item);
		}

		return _field.get(item);
	}

	public Field getField() {
		return _field;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_field, _method);
	}

	private final Field _field;
	private final Method _method;

}