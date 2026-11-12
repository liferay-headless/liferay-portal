/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.rest.client.serdes.v1_0;

import com.liferay.launch.rest.client.dto.v1_0.LaunchPublishStatus;
import com.liferay.launch.rest.client.json.BaseJSONParser;

import jakarta.annotation.Generated;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Alberto Moreno
 * @generated
 */
@Generated("")
public class LaunchPublishStatusSerDes {

	public static LaunchPublishStatus toDTO(String json) {
		LaunchPublishStatusJSONParser launchPublishStatusJSONParser =
			new LaunchPublishStatusJSONParser();

		return launchPublishStatusJSONParser.parseToDTO(json);
	}

	public static LaunchPublishStatus[] toDTOs(String json) {
		LaunchPublishStatusJSONParser launchPublishStatusJSONParser =
			new LaunchPublishStatusJSONParser();

		return launchPublishStatusJSONParser.parseToDTOs(json);
	}

	public static String toJSON(LaunchPublishStatus launchPublishStatus) {
		if (launchPublishStatus == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (launchPublishStatus.getErrorMessage() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"errorMessage\": ");

			sb.append("\"");

			sb.append(_escape(launchPublishStatus.getErrorMessage()));

			sb.append("\"");
		}

		if (launchPublishStatus.getLabel() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"label\": ");

			sb.append("\"");

			sb.append(_escape(launchPublishStatus.getLabel()));

			sb.append("\"");
		}

		if (launchPublishStatus.getLabel_i18n() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"label_i18n\": ");

			sb.append("\"");

			sb.append(_escape(launchPublishStatus.getLabel_i18n()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		LaunchPublishStatusJSONParser launchPublishStatusJSONParser =
			new LaunchPublishStatusJSONParser();

		return launchPublishStatusJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		LaunchPublishStatus launchPublishStatus) {

		if (launchPublishStatus == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (launchPublishStatus.getErrorMessage() == null) {
			map.put("errorMessage", null);
		}
		else {
			map.put(
				"errorMessage",
				String.valueOf(launchPublishStatus.getErrorMessage()));
		}

		if (launchPublishStatus.getLabel() == null) {
			map.put("label", null);
		}
		else {
			map.put("label", String.valueOf(launchPublishStatus.getLabel()));
		}

		if (launchPublishStatus.getLabel_i18n() == null) {
			map.put("label_i18n", null);
		}
		else {
			map.put(
				"label_i18n",
				String.valueOf(launchPublishStatus.getLabel_i18n()));
		}

		return map;
	}

	public static class LaunchPublishStatusJSONParser
		extends BaseJSONParser<LaunchPublishStatus> {

		@Override
		protected LaunchPublishStatus createDTO() {
			return new LaunchPublishStatus();
		}

		@Override
		protected LaunchPublishStatus[] createDTOArray(int size) {
			return new LaunchPublishStatus[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "errorMessage")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "label")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "label_i18n")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			LaunchPublishStatus launchPublishStatus, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "errorMessage")) {
				if (jsonParserFieldValue != null) {
					launchPublishStatus.setErrorMessage(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "label")) {
				if (jsonParserFieldValue != null) {
					launchPublishStatus.setLabel((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "label_i18n")) {
				if (jsonParserFieldValue != null) {
					launchPublishStatus.setLabel_i18n(
						(String)jsonParserFieldValue);
				}
			}
		}

	}

	private static String _escape(Object object) {
		String string = String.valueOf(object);

		for (String[] strings : BaseJSONParser.JSON_ESCAPE_STRINGS) {
			string = string.replace(strings[0], strings[1]);
		}

		return string;
	}

	private static String _toJSON(Map<String, ?> map) {
		StringBuilder sb = new StringBuilder("{");

		@SuppressWarnings("unchecked")
		Set set = map.entrySet();

		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<String, ?>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, ?> entry = iterator.next();

			sb.append("\"");
			sb.append(entry.getKey());
			sb.append("\": ");

			Object value = entry.getValue();

			sb.append(_toJSON(value));

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static String _toJSON(Object value) {
		if (value == null) {
			return "null";
		}

		if (value instanceof Collection) {
			Collection<?> collection = (Collection<?>)value;

			return _toJSON(collection.toArray());
		}

		if (value instanceof Map) {
			return _toJSON((Map)value);
		}

		Class<?> clazz = value.getClass();

		if (clazz.isArray()) {
			StringBuilder sb = new StringBuilder("[");

			Object[] values = (Object[])value;

			for (int i = 0; i < values.length; i++) {
				sb.append(_toJSON(values[i]));

				if ((i + 1) < values.length) {
					sb.append(", ");
				}
			}

			sb.append("]");

			return sb.toString();
		}

		if (value instanceof String) {
			return "\"" + _escape(value) + "\"";
		}

		return String.valueOf(value);
	}

}
// LIFERAY-REST-BUILDER-HASH:-1912404019