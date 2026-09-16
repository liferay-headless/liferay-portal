/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.portal.instances.client.serdes.v1_0;

import com.liferay.headless.portal.instances.client.dto.v1_0.PortalInstanceOperation;
import com.liferay.headless.portal.instances.client.json.BaseJSONParser;

import jakarta.annotation.Generated;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Alberto Chaparro
 * @generated
 */
@Generated("")
public class PortalInstanceOperationSerDes {

	public static PortalInstanceOperation toDTO(String json) {
		PortalInstanceOperationJSONParser portalInstanceOperationJSONParser =
			new PortalInstanceOperationJSONParser();

		return portalInstanceOperationJSONParser.parseToDTO(json);
	}

	public static PortalInstanceOperation[] toDTOs(String json) {
		PortalInstanceOperationJSONParser portalInstanceOperationJSONParser =
			new PortalInstanceOperationJSONParser();

		return portalInstanceOperationJSONParser.parseToDTOs(json);
	}

	public static String toJSON(
		PortalInstanceOperation portalInstanceOperation) {

		if (portalInstanceOperation == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		DateFormat liferayToJSONDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssXX");

		if (portalInstanceOperation.getBackgroundTaskId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"backgroundTaskId\": ");

			sb.append(portalInstanceOperation.getBackgroundTaskId());
		}

		if (portalInstanceOperation.getCompanyId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"companyId\": ");

			sb.append(portalInstanceOperation.getCompanyId());
		}

		if (portalInstanceOperation.getCompletionDate() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"completionDate\": ");

			sb.append("\"");

			sb.append(
				liferayToJSONDateFormat.format(
					portalInstanceOperation.getCompletionDate()));

			sb.append("\"");
		}

		if (portalInstanceOperation.getCreateDate() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"createDate\": ");

			sb.append("\"");

			sb.append(
				liferayToJSONDateFormat.format(
					portalInstanceOperation.getCreateDate()));

			sb.append("\"");
		}

		if (portalInstanceOperation.getErrorMessage() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"errorMessage\": ");

			sb.append("\"");

			sb.append(_escape(portalInstanceOperation.getErrorMessage()));

			sb.append("\"");
		}

		if (portalInstanceOperation.getOperationType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"operationType\": ");

			sb.append("\"");
			sb.append(portalInstanceOperation.getOperationType());
			sb.append("\"");
		}

		if (portalInstanceOperation.getPortalInstanceId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"portalInstanceId\": ");

			sb.append("\"");

			sb.append(_escape(portalInstanceOperation.getPortalInstanceId()));

			sb.append("\"");
		}

		if (portalInstanceOperation.getSchemaName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"schemaName\": ");

			sb.append("\"");

			sb.append(_escape(portalInstanceOperation.getSchemaName()));

			sb.append("\"");
		}

		if (portalInstanceOperation.getStatus() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"status\": ");

			sb.append("\"");
			sb.append(portalInstanceOperation.getStatus());
			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		PortalInstanceOperationJSONParser portalInstanceOperationJSONParser =
			new PortalInstanceOperationJSONParser();

		return portalInstanceOperationJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		PortalInstanceOperation portalInstanceOperation) {

		if (portalInstanceOperation == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		DateFormat liferayToJSONDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssXX");

		if (portalInstanceOperation.getBackgroundTaskId() == null) {
			map.put("backgroundTaskId", null);
		}
		else {
			map.put(
				"backgroundTaskId",
				String.valueOf(portalInstanceOperation.getBackgroundTaskId()));
		}

		if (portalInstanceOperation.getCompanyId() == null) {
			map.put("companyId", null);
		}
		else {
			map.put(
				"companyId",
				String.valueOf(portalInstanceOperation.getCompanyId()));
		}

		if (portalInstanceOperation.getCompletionDate() == null) {
			map.put("completionDate", null);
		}
		else {
			map.put(
				"completionDate",
				liferayToJSONDateFormat.format(
					portalInstanceOperation.getCompletionDate()));
		}

		if (portalInstanceOperation.getCreateDate() == null) {
			map.put("createDate", null);
		}
		else {
			map.put(
				"createDate",
				liferayToJSONDateFormat.format(
					portalInstanceOperation.getCreateDate()));
		}

		if (portalInstanceOperation.getErrorMessage() == null) {
			map.put("errorMessage", null);
		}
		else {
			map.put(
				"errorMessage",
				String.valueOf(portalInstanceOperation.getErrorMessage()));
		}

		if (portalInstanceOperation.getOperationType() == null) {
			map.put("operationType", null);
		}
		else {
			map.put(
				"operationType",
				String.valueOf(portalInstanceOperation.getOperationType()));
		}

		if (portalInstanceOperation.getPortalInstanceId() == null) {
			map.put("portalInstanceId", null);
		}
		else {
			map.put(
				"portalInstanceId",
				String.valueOf(portalInstanceOperation.getPortalInstanceId()));
		}

		if (portalInstanceOperation.getSchemaName() == null) {
			map.put("schemaName", null);
		}
		else {
			map.put(
				"schemaName",
				String.valueOf(portalInstanceOperation.getSchemaName()));
		}

		if (portalInstanceOperation.getStatus() == null) {
			map.put("status", null);
		}
		else {
			map.put(
				"status", String.valueOf(portalInstanceOperation.getStatus()));
		}

		return map;
	}

	public static class PortalInstanceOperationJSONParser
		extends BaseJSONParser<PortalInstanceOperation> {

		@Override
		protected PortalInstanceOperation createDTO() {
			return new PortalInstanceOperation();
		}

		@Override
		protected PortalInstanceOperation[] createDTOArray(int size) {
			return new PortalInstanceOperation[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "backgroundTaskId")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "companyId")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "completionDate")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "createDate")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "errorMessage")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "operationType")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "portalInstanceId")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "schemaName")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "status")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			PortalInstanceOperation portalInstanceOperation,
			String jsonParserFieldName, Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "backgroundTaskId")) {
				if (jsonParserFieldValue != null) {
					portalInstanceOperation.setBackgroundTaskId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "companyId")) {
				if (jsonParserFieldValue != null) {
					portalInstanceOperation.setCompanyId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "completionDate")) {
				if (jsonParserFieldValue != null) {
					portalInstanceOperation.setCompletionDate(
						toDate((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "createDate")) {
				if (jsonParserFieldValue != null) {
					portalInstanceOperation.setCreateDate(
						toDate((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "errorMessage")) {
				if (jsonParserFieldValue != null) {
					portalInstanceOperation.setErrorMessage(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "operationType")) {
				if (jsonParserFieldValue != null) {
					portalInstanceOperation.setOperationType(
						PortalInstanceOperation.OperationType.create(
							(String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "portalInstanceId")) {
				if (jsonParserFieldValue != null) {
					portalInstanceOperation.setPortalInstanceId(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "schemaName")) {
				if (jsonParserFieldValue != null) {
					portalInstanceOperation.setSchemaName(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "status")) {
				if (jsonParserFieldValue != null) {
					portalInstanceOperation.setStatus(
						PortalInstanceOperation.Status.create(
							(String)jsonParserFieldValue));
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
// LIFERAY-REST-BUILDER-HASH:-2137432571