/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.dto.customfields.util;

import com.liferay.expando.kernel.model.ExpandoBridge;
import com.liferay.expando.kernel.model.ExpandoColumnConstants;

import java.io.Serializable;

import java.lang.reflect.Array;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Magdalena Jedraszak
 */
public class CustomFieldsUtil {

	public static Object getValue(
		Map.Entry<String, Serializable> entry, ExpandoBridge expandoBridge,
		String key) {

		Object value = entry.getValue();

		if (value == null) {
			return expandoBridge.getAttributeDefault(key);
		}

		if (value.getClass(
			).isArray() && (Array.getLength(value) == 0)) {

			int attributeType = expandoBridge.getAttributeType(key);

			for (int type : ExpandoColumnConstants.TYPES) {
				if (_isArrayType(type) && (type == attributeType)) {
					return new String[] {"false"};
				}
			}
		}

		if (value instanceof Map) {
			Map<?, ?> map = (Map<?, ?>)value;

			if (map.isEmpty()) {
				return new HashMap<>();
			}
		}

		return value;
	}

	private static boolean _isArrayType(int type) {
		if ((type == ExpandoColumnConstants.BOOLEAN_ARRAY) ||
			(type == ExpandoColumnConstants.DATE_ARRAY) ||
			(type == ExpandoColumnConstants.DOUBLE_ARRAY) ||
			(type == ExpandoColumnConstants.FLOAT_ARRAY) ||
			(type == ExpandoColumnConstants.INTEGER_ARRAY) ||
			(type == ExpandoColumnConstants.LONG_ARRAY) ||
			(type == ExpandoColumnConstants.NUMBER_ARRAY) ||
			(type == ExpandoColumnConstants.SHORT_ARRAY) ||
			(type == ExpandoColumnConstants.STRING_ARRAY) ||
			(type == ExpandoColumnConstants.STRING_ARRAY_LOCALIZED)) {

			return true;
		}

		return false;
	}

}