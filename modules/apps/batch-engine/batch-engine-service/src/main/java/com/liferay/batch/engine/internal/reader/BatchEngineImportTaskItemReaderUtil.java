/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.reader;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import com.liferay.batch.engine.action.ItemReaderPostAction;
import com.liferay.batch.engine.model.BatchEngineImportTask;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;
import java.io.Serializable;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ivica Cardic
 */
public class BatchEngineImportTaskItemReaderUtil {

	public static <T> T convertValue(
			BatchEngineImportTask batchEngineImportTask, Class<T> itemClass,
			Map<String, Object> fieldNameValueMap,
			List<ItemReaderPostAction> itemReaderPostActions)
		throws Exception {

		ObjectReader objectReader = _objectMapper.readerFor(itemClass);

		objectReader = objectReader.without(
			DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		JsonNode jsonNode = objectReader.readTree(
			_objectMapper.writeValueAsString(fieldNameValueMap));

		T item = objectReader.readValue(jsonNode);

		Map<String, Serializable> extendedProperties = _getExtendedProperties(
			itemClass, jsonNode, _objectMapper);

		for (ItemReaderPostAction itemReaderPostAction :
				itemReaderPostActions) {

			itemReaderPostAction.run(
				batchEngineImportTask, extendedProperties, item);
		}

		return item;
	}

	public static Map<String, Object> mapFieldNames(
		Map<String, ? extends Serializable> fieldNameMappingMap,
		Map<String, Object> fieldNameValueMap) {

		if ((fieldNameMappingMap == null) || fieldNameMappingMap.isEmpty()) {
			return fieldNameValueMap;
		}

		Map<String, Object> targetFieldNameValueMap = new HashMap<>();

		for (Map.Entry<String, Object> entry : fieldNameValueMap.entrySet()) {
			String targetFieldName = (String)fieldNameMappingMap.get(
				entry.getKey());

			if (Validator.isNotNull(targetFieldName)) {
				Object object = targetFieldNameValueMap.get(targetFieldName);

				if ((object != null) && (object instanceof Map)) {
					Map<?, ?> map = (Map)object;

					map.putAll((Map)entry.getValue());
				}
				else {
					targetFieldNameValueMap.put(
						targetFieldName, entry.getValue());
				}
			}
		}

		return targetFieldNameValueMap;
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

	private static final Log _log = LogFactoryUtil.getLog(
		BatchEngineImportTaskItemReaderUtil.class);

	private static final ObjectMapper _objectMapper = new ObjectMapper() {
		{
			registerModule(
				new SimpleModule() {
					{
						addDeserializer(Map.class, new MapStdDeserializer());
					}
				});
		}
	};

	private static class MapStdDeserializer
		extends StdDeserializer<Map<String, Object>> {

		public MapStdDeserializer() {
			this(Map.class);
		}

		@Override
		public Map<String, Object> deserialize(
				JsonParser jsonParser,
				DeserializationContext deserializationContext)
			throws IOException {

			try {
				return deserializationContext.readValue(
					jsonParser, LinkedHashMap.class);
			}
			catch (Exception exception) {
				if (_log.isDebugEnabled()) {
					_log.debug(exception);
				}

				Map<String, Object> map = new LinkedHashMap<>();

				String string = jsonParser.getValueAsString();

				for (String line : string.split(StringPool.RETURN_NEW_LINE)) {
					String[] lineParts = line.split(StringPool.COLON);

					map.put(lineParts[0], lineParts[1]);
				}

				return map;
			}
		}

		protected MapStdDeserializer(Class<?> clazz) {
			super(clazz);
		}

	}

}