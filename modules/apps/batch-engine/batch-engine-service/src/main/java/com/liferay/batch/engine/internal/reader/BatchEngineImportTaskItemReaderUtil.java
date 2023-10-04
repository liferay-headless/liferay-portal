/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.reader;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import com.liferay.batch.engine.action.EntityExtensionItem;
import com.liferay.batch.engine.action.EntityExtensionItemParser;
import com.liferay.batch.engine.action.ItemReaderPostAction;
import com.liferay.batch.engine.model.BatchEngineImportTask;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;
import java.io.Serializable;

import java.util.HashMap;
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
			EntityExtensionItemParser<T> entityExtensionItemParser,
			List<ItemReaderPostAction> itemReaderPostActions)
		throws Exception {

		EntityExtensionItem<T> entityExtensionItem =
			entityExtensionItemParser.parse(
				itemClass, fieldNameValueMap, _objectMapper);

		for (ItemReaderPostAction itemReaderPostAction :
				itemReaderPostActions) {

			itemReaderPostAction.run(
				batchEngineImportTask,
				entityExtensionItem.getExtendedProperties(),
				entityExtensionItem.getItem());
		}

		return entityExtensionItem.getItem();
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