/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.batch.engine.action;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.liferay.batch.engine.action.EntityExtensionItem;
import com.liferay.batch.engine.action.EntityExtensionItemParser;
import com.liferay.portal.kernel.util.StringUtil;
import org.osgi.service.component.annotations.Component;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Carlos Correa
 */
@Component(service = EntityExtensionItemParser.class)
public class EntityExtensionItemParserImpl<T> implements
	EntityExtensionItemParser<T> {

	@Override
	public EntityExtensionItem<T> parse(Class<T> clazz, Map<String, Object> fieldNameValueMap, ObjectMapper objectMapper) throws Exception {
		ObjectReader objectReader = objectMapper.readerFor(clazz);

		objectReader = objectReader.without(
			DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		JsonNode jsonNode = objectReader.readTree(
			objectMapper.writeValueAsString(fieldNameValueMap));

		EntityExtensionItem<T> entityExtensionItem = new EntityExtensionItem();

		entityExtensionItem.setItem(objectReader.readValue(jsonNode));

		entityExtensionItem.setExtendedProperties(_getExtendedProperties(
			clazz, jsonNode, objectMapper));

		return entityExtensionItem;
	}

	private static Map<String, Serializable> _getExtendedProperties(
		Class<?> clazz, JsonNode jsonNode, ObjectMapper objectMapper)
		throws Exception {

		Map<String, Serializable> extendedProperties = new HashMap<>();

		List<String> fieldNames = new ArrayList<>();

		for (Field field : clazz.getDeclaredFields()) {
			if (StringUtil.equals("_extendedProperties", field.getName())) {
				continue;
			}

			fieldNames.add(field.getName());
		}

		Iterator<String> iterator = jsonNode.fieldNames();

		while (iterator.hasNext()) {
			String fieldName = iterator.next();

			if (!fieldNames.contains(fieldName)) {
				extendedProperties.put(
					fieldName,
					_getJsonNodeValue(jsonNode.get(fieldName), objectMapper));
			}
		}

		return extendedProperties;
	}

	private static Serializable _getJsonNodeValue(
		JsonNode jsonNode, ObjectMapper objectMapper)
		throws Exception {

		if (jsonNode.isArray()) {
			return (Serializable)objectMapper.readValue(
				jsonNode.traverse(), Object[].class);
		}
		else if (jsonNode.isBoolean()) {
			return jsonNode.asBoolean();
		}
		else if (jsonNode.isDouble()) {
			return jsonNode.asDouble();
		}
		else if (jsonNode.isInt()) {
			return jsonNode.asInt();
		}
		else if (jsonNode.isLong()) {
			return jsonNode.asLong();
		}
		else if (jsonNode.isTextual()) {
			return jsonNode.asText();
		}
		else if (jsonNode.isObject()) {
			return (Serializable)objectMapper.readValue(
				jsonNode.traverse(), Object.class);
		}

		return null;
	}
}
