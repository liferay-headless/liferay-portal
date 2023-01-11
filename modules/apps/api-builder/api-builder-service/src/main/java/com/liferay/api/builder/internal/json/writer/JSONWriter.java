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

package com.liferay.api.builder.internal.json.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.lang.reflect.Field;

import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Matija Petanjek
 */
@Component(service = JSONWriter.class)
public class JSONWriter {

	public String toJSON(Object object, Map<String, String> mappings)
		throws Exception {

		ObjectNode objectNode = _objectMapper.createObjectNode();

		for (Map.Entry<String, String> mapping : mappings.entrySet()) {
			objectNode.put(
				mapping.getKey(),
				_objectMapper.writeValueAsString(
					_getFieldValue(
						object, object.getClass(), mapping.getValue())));
		}

		return objectNode.toString();
	}

	// TODO use Info framework to extract field values

	private Object _getFieldValue(
			Object object, Class<?> clazz, String fieldName)
		throws Exception {

		if ((clazz != null) && _hasField(clazz, fieldName)) {
			Field field = clazz.getDeclaredField(fieldName);

			field.setAccessible(true);

			return field.get(object);
		}

		return _getFieldValue(object, clazz.getSuperclass(), fieldName);
	}

	private boolean _hasField(Class<?> clazz, String fieldName) {
		for (Field field : clazz.getDeclaredFields()) {
			if (fieldName.equals(field.getName())) {
				return true;
			}
		}

		return false;
	}

	private final ObjectMapper _objectMapper = new ObjectMapper();

}