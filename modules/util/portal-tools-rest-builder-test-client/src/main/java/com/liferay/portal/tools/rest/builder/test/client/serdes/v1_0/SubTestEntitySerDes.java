/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.tools.rest.builder.test.client.serdes.v1_0;

import com.liferay.portal.tools.rest.builder.test.client.dto.v1_0.SubTestEntity;
import com.liferay.portal.tools.rest.builder.test.client.json.BaseJSONParser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Generated;

/**
 * @author Alejandro Tardín
 * @generated
 */
@Generated("")
public class SubTestEntitySerDes {

	public static SubTestEntity toDTO(String json) {
		SubTestEntityJSONParser subTestEntityJSONParser =
			new SubTestEntityJSONParser();

		return subTestEntityJSONParser.parseToDTO(json);
	}

	public static SubTestEntity[] toDTOs(String json) {
		SubTestEntityJSONParser subTestEntityJSONParser =
			new SubTestEntityJSONParser();

		return subTestEntityJSONParser.parseToDTOs(json);
	}

	public static String toJSON(SubTestEntity subTestEntity) {
		if (subTestEntity == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		DateFormat liferayToJSONDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssXX");

		if (subTestEntity.getDateCreated() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"dateCreated\": ");

			sb.append("\"");

			sb.append(
				liferayToJSONDateFormat.format(subTestEntity.getDateCreated()));

			sb.append("\"");
		}

		if (subTestEntity.getDateModified() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"dateModified\": ");

			sb.append("\"");

			sb.append(
				liferayToJSONDateFormat.format(
					subTestEntity.getDateModified()));

			sb.append("\"");
		}

		if (subTestEntity.getDescription() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"description\": ");

			sb.append("\"");

			sb.append(_escape(subTestEntity.getDescription()));

			sb.append("\"");
		}

		if (subTestEntity.getErcSiteTestEntityExternalReferenceCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"ercSiteTestEntityExternalReferenceCode\": ");

			sb.append("\"");

			sb.append(
				_escape(
					subTestEntity.getErcSiteTestEntityExternalReferenceCode()));

			sb.append("\"");
		}

		if (subTestEntity.getExternalReferenceCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"externalReferenceCode\": ");

			sb.append("\"");

			sb.append(_escape(subTestEntity.getExternalReferenceCode()));

			sb.append("\"");
		}

		if (subTestEntity.getPermissions() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"permissions\": ");

			sb.append("[");

			for (int i = 0; i < subTestEntity.getPermissions().length; i++) {
				sb.append(subTestEntity.getPermissions()[i]);

				if ((i + 1) < subTestEntity.getPermissions().length) {
					sb.append(", ");
				}
			}

			sb.append("]");
		}

		if (subTestEntity.getSiteExternalReferenceCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"siteExternalReferenceCode\": ");

			sb.append("\"");

			sb.append(_escape(subTestEntity.getSiteExternalReferenceCode()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		SubTestEntityJSONParser subTestEntityJSONParser =
			new SubTestEntityJSONParser();

		return subTestEntityJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(SubTestEntity subTestEntity) {
		if (subTestEntity == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		DateFormat liferayToJSONDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssXX");

		if (subTestEntity.getDateCreated() == null) {
			map.put("dateCreated", null);
		}
		else {
			map.put(
				"dateCreated",
				liferayToJSONDateFormat.format(subTestEntity.getDateCreated()));
		}

		if (subTestEntity.getDateModified() == null) {
			map.put("dateModified", null);
		}
		else {
			map.put(
				"dateModified",
				liferayToJSONDateFormat.format(
					subTestEntity.getDateModified()));
		}

		if (subTestEntity.getDescription() == null) {
			map.put("description", null);
		}
		else {
			map.put(
				"description", String.valueOf(subTestEntity.getDescription()));
		}

		if (subTestEntity.getErcSiteTestEntityExternalReferenceCode() == null) {
			map.put("ercSiteTestEntityExternalReferenceCode", null);
		}
		else {
			map.put(
				"ercSiteTestEntityExternalReferenceCode",
				String.valueOf(
					subTestEntity.getErcSiteTestEntityExternalReferenceCode()));
		}

		if (subTestEntity.getExternalReferenceCode() == null) {
			map.put("externalReferenceCode", null);
		}
		else {
			map.put(
				"externalReferenceCode",
				String.valueOf(subTestEntity.getExternalReferenceCode()));
		}

		if (subTestEntity.getPermissions() == null) {
			map.put("permissions", null);
		}
		else {
			map.put(
				"permissions", String.valueOf(subTestEntity.getPermissions()));
		}

		if (subTestEntity.getSiteExternalReferenceCode() == null) {
			map.put("siteExternalReferenceCode", null);
		}
		else {
			map.put(
				"siteExternalReferenceCode",
				String.valueOf(subTestEntity.getSiteExternalReferenceCode()));
		}

		return map;
	}

	public static class SubTestEntityJSONParser
		extends BaseJSONParser<SubTestEntity> {

		@Override
		protected SubTestEntity createDTO() {
			return new SubTestEntity();
		}

		@Override
		protected SubTestEntity[] createDTOArray(int size) {
			return new SubTestEntity[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "dateCreated")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "dateModified")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "description")) {
				return false;
			}
			else if (Objects.equals(
						jsonParserFieldName,
						"ercSiteTestEntityExternalReferenceCode")) {

				return false;
			}
			else if (Objects.equals(
						jsonParserFieldName, "externalReferenceCode")) {

				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "permissions")) {
				return false;
			}
			else if (Objects.equals(
						jsonParserFieldName, "siteExternalReferenceCode")) {

				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			SubTestEntity subTestEntity, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "dateCreated")) {
				if (jsonParserFieldValue != null) {
					subTestEntity.setDateCreated(
						toDate((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "dateModified")) {
				if (jsonParserFieldValue != null) {
					subTestEntity.setDateModified(
						toDate((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "description")) {
				if (jsonParserFieldValue != null) {
					subTestEntity.setDescription((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(
						jsonParserFieldName,
						"ercSiteTestEntityExternalReferenceCode")) {

				if (jsonParserFieldValue != null) {
					subTestEntity.setErcSiteTestEntityExternalReferenceCode(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "externalReferenceCode")) {

				if (jsonParserFieldValue != null) {
					subTestEntity.setExternalReferenceCode(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "permissions")) {
				if (jsonParserFieldValue != null) {
					Object[] jsonParserFieldValues =
						(Object[])jsonParserFieldValue;

					com.liferay.portal.tools.rest.builder.test.client.
						permission.Permission[] permissionsArray = new
						com.liferay.portal.tools.rest.builder.test.client.
							permission.Permission[jsonParserFieldValues.length];

					for (int i = 0; i < permissionsArray.length; i++) {
						permissionsArray[i] =
							com.liferay.portal.tools.rest.builder.test.client.
								permission.Permission.toDTO(
									(String)jsonParserFieldValues[i]);
					}

					subTestEntity.setPermissions(permissionsArray);
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "siteExternalReferenceCode")) {

				if (jsonParserFieldValue != null) {
					subTestEntity.setSiteExternalReferenceCode(
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