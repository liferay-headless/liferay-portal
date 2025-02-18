/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.jackson.databind.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Mauricio Valdivia
 */
public class GenericMapDeserializer<T>
	extends StdDeserializer<Map<String, T>> implements ContextualDeserializer {

	public GenericMapDeserializer() {
		super(Map.class);
	}

	// Default constructor needed for Jackson

	public GenericMapDeserializer(Class<T> clazz) {
		super(Map.class);

		_clazz = clazz;
	}

	@Override
	public JsonDeserializer<?> createContextual(
			DeserializationContext ctxt, BeanProperty property)
		throws JsonMappingException {

		// Extract the actual type of the Map's value from the property

		if (property != null) {
			JavaType mapType = property.getType();

			JavaType valueType = mapType.containedType(1);

			if (valueType != null) {
				return new GenericMapDeserializer<>(valueType.getRawClass());
			}
		}

		return this;
	}

	@Override
	public Map<String, T> deserialize(JsonParser p, DeserializationContext ctxt)
		throws IOException {

		ObjectMapper objectMapper = (ObjectMapper)p.getCodec();

		JsonNode jsonNode = objectMapper.readTree(p);

		Map<String, T> result = new HashMap<>();

		Iterator<Map.Entry<String, JsonNode>> fieldsIterator =
			jsonNode.fields();

		while (fieldsIterator.hasNext()) {
			Map.Entry<String, JsonNode> entry = fieldsIterator.next();

			T value = objectMapper.treeToValue(entry.getValue(), _clazz);

			result.put(entry.getKey(), value);
		}

		return result;
	}

	private Class<T> _clazz;

}