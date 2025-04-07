/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.client.serdes.v1_0;

import com.liferay.headless.admin.site.client.dto.v1_0.WidgetLookAndFeelConfig;
import com.liferay.headless.admin.site.client.json.BaseJSONParser;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Generated;

/**
 * @author Rubén Pulido
 * @generated
 */
@Generated("")
public class WidgetLookAndFeelConfigSerDes {

	public static WidgetLookAndFeelConfig toDTO(String json) {
		WidgetLookAndFeelConfigJSONParser widgetLookAndFeelConfigJSONParser =
			new WidgetLookAndFeelConfigJSONParser();

		return widgetLookAndFeelConfigJSONParser.parseToDTO(json);
	}

	public static WidgetLookAndFeelConfig[] toDTOs(String json) {
		WidgetLookAndFeelConfigJSONParser widgetLookAndFeelConfigJSONParser =
			new WidgetLookAndFeelConfigJSONParser();

		return widgetLookAndFeelConfigJSONParser.parseToDTOs(json);
	}

	public static String toJSON(
		WidgetLookAndFeelConfig widgetLookAndFeelConfig) {

		if (widgetLookAndFeelConfig == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (widgetLookAndFeelConfig.getAdvancedStylingConfig() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"advancedStylingConfig\": ");

			if (widgetLookAndFeelConfig.getAdvancedStylingConfig() instanceof
					String) {

				sb.append("\"");
				sb.append(
					(String)widgetLookAndFeelConfig.getAdvancedStylingConfig());
				sb.append("\"");
			}
			else if (
						widgetLookAndFeelConfig.
							getAdvancedStylingConfig() instanceof Map) {

				sb.append(
					_toJSON(
						(Map<String, ?>)
							widgetLookAndFeelConfig.
								getAdvancedStylingConfig()));
			}
			else {
				sb.append(widgetLookAndFeelConfig.getAdvancedStylingConfig());
			}
		}

		if (widgetLookAndFeelConfig.getBackgroundStylesConfig() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"backgroundStylesConfig\": ");

			if (widgetLookAndFeelConfig.getBackgroundStylesConfig() instanceof
					String) {

				sb.append("\"");
				sb.append(
					(String)
						widgetLookAndFeelConfig.getBackgroundStylesConfig());
				sb.append("\"");
			}
			else if (
						widgetLookAndFeelConfig.
							getBackgroundStylesConfig() instanceof Map) {

				sb.append(
					_toJSON(
						(Map<String, ?>)
							widgetLookAndFeelConfig.
								getBackgroundStylesConfig()));
			}
			else {
				sb.append(widgetLookAndFeelConfig.getBackgroundStylesConfig());
			}
		}

		if (widgetLookAndFeelConfig.getBorderStylesConfig() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"borderStylesConfig\": ");

			if (widgetLookAndFeelConfig.getBorderStylesConfig() instanceof
					String) {

				sb.append("\"");
				sb.append(
					(String)widgetLookAndFeelConfig.getBorderStylesConfig());
				sb.append("\"");
			}
			else if (widgetLookAndFeelConfig.getBorderStylesConfig() instanceof
						Map) {

				sb.append(
					_toJSON(
						(Map<String, ?>)
							widgetLookAndFeelConfig.getBorderStylesConfig()));
			}
			else {
				sb.append(widgetLookAndFeelConfig.getBorderStylesConfig());
			}
		}

		if (widgetLookAndFeelConfig.getGeneralConfig() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"generalConfig\": ");

			if (widgetLookAndFeelConfig.getGeneralConfig() instanceof String) {
				sb.append("\"");
				sb.append((String)widgetLookAndFeelConfig.getGeneralConfig());
				sb.append("\"");
			}
			else if (widgetLookAndFeelConfig.getGeneralConfig() instanceof
						Map) {

				sb.append(
					_toJSON(
						(Map<String, ?>)
							widgetLookAndFeelConfig.getGeneralConfig()));
			}
			else {
				sb.append(widgetLookAndFeelConfig.getGeneralConfig());
			}
		}

		if (widgetLookAndFeelConfig.getMarginAndPaddingConfig() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"marginAndPaddingConfig\": ");

			if (widgetLookAndFeelConfig.getMarginAndPaddingConfig() instanceof
					String) {

				sb.append("\"");
				sb.append(
					(String)
						widgetLookAndFeelConfig.getMarginAndPaddingConfig());
				sb.append("\"");
			}
			else if (
						widgetLookAndFeelConfig.
							getMarginAndPaddingConfig() instanceof Map) {

				sb.append(
					_toJSON(
						(Map<String, ?>)
							widgetLookAndFeelConfig.
								getMarginAndPaddingConfig()));
			}
			else {
				sb.append(widgetLookAndFeelConfig.getMarginAndPaddingConfig());
			}
		}

		if (widgetLookAndFeelConfig.getTextStylesConfig() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"textStylesConfig\": ");

			if (widgetLookAndFeelConfig.getTextStylesConfig() instanceof
					String) {

				sb.append("\"");
				sb.append(
					(String)widgetLookAndFeelConfig.getTextStylesConfig());
				sb.append("\"");
			}
			else if (widgetLookAndFeelConfig.getTextStylesConfig() instanceof
						Map) {

				sb.append(
					_toJSON(
						(Map<String, ?>)
							widgetLookAndFeelConfig.getTextStylesConfig()));
			}
			else {
				sb.append(widgetLookAndFeelConfig.getTextStylesConfig());
			}
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		WidgetLookAndFeelConfigJSONParser widgetLookAndFeelConfigJSONParser =
			new WidgetLookAndFeelConfigJSONParser();

		return widgetLookAndFeelConfigJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		WidgetLookAndFeelConfig widgetLookAndFeelConfig) {

		if (widgetLookAndFeelConfig == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (widgetLookAndFeelConfig.getAdvancedStylingConfig() == null) {
			map.put("advancedStylingConfig", null);
		}
		else {
			map.put(
				"advancedStylingConfig",
				String.valueOf(
					widgetLookAndFeelConfig.getAdvancedStylingConfig()));
		}

		if (widgetLookAndFeelConfig.getBackgroundStylesConfig() == null) {
			map.put("backgroundStylesConfig", null);
		}
		else {
			map.put(
				"backgroundStylesConfig",
				String.valueOf(
					widgetLookAndFeelConfig.getBackgroundStylesConfig()));
		}

		if (widgetLookAndFeelConfig.getBorderStylesConfig() == null) {
			map.put("borderStylesConfig", null);
		}
		else {
			map.put(
				"borderStylesConfig",
				String.valueOf(
					widgetLookAndFeelConfig.getBorderStylesConfig()));
		}

		if (widgetLookAndFeelConfig.getGeneralConfig() == null) {
			map.put("generalConfig", null);
		}
		else {
			map.put(
				"generalConfig",
				String.valueOf(widgetLookAndFeelConfig.getGeneralConfig()));
		}

		if (widgetLookAndFeelConfig.getMarginAndPaddingConfig() == null) {
			map.put("marginAndPaddingConfig", null);
		}
		else {
			map.put(
				"marginAndPaddingConfig",
				String.valueOf(
					widgetLookAndFeelConfig.getMarginAndPaddingConfig()));
		}

		if (widgetLookAndFeelConfig.getTextStylesConfig() == null) {
			map.put("textStylesConfig", null);
		}
		else {
			map.put(
				"textStylesConfig",
				String.valueOf(widgetLookAndFeelConfig.getTextStylesConfig()));
		}

		return map;
	}

	public static class WidgetLookAndFeelConfigJSONParser
		extends BaseJSONParser<WidgetLookAndFeelConfig> {

		@Override
		protected WidgetLookAndFeelConfig createDTO() {
			return new WidgetLookAndFeelConfig();
		}

		@Override
		protected WidgetLookAndFeelConfig[] createDTOArray(int size) {
			return new WidgetLookAndFeelConfig[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "advancedStylingConfig")) {
				return true;
			}
			else if (Objects.equals(
						jsonParserFieldName, "backgroundStylesConfig")) {

				return true;
			}
			else if (Objects.equals(
						jsonParserFieldName, "borderStylesConfig")) {

				return true;
			}
			else if (Objects.equals(jsonParserFieldName, "generalConfig")) {
				return true;
			}
			else if (Objects.equals(
						jsonParserFieldName, "marginAndPaddingConfig")) {

				return true;
			}
			else if (Objects.equals(jsonParserFieldName, "textStylesConfig")) {
				return true;
			}

			return false;
		}

		@Override
		protected void setField(
			WidgetLookAndFeelConfig widgetLookAndFeelConfig,
			String jsonParserFieldName, Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "advancedStylingConfig")) {
				if (jsonParserFieldValue != null) {
					if (jsonParserFieldValue instanceof String) {
						String jsonStr = (String)jsonParserFieldValue;

						if ((jsonStr.startsWith("{") &&
							 jsonStr.endsWith("}")) ||
							(jsonStr.startsWith("[") &&
							 jsonStr.endsWith("]"))) {

							try {
								Object parsedValue = parseToMap(jsonStr);
								widgetLookAndFeelConfig.
									setAdvancedStylingConfig(parsedValue);
							}
							catch (Exception e) {
								widgetLookAndFeelConfig.
									setAdvancedStylingConfig(
										jsonParserFieldValue);
							}
						}
						else {
							widgetLookAndFeelConfig.setAdvancedStylingConfig(
								jsonParserFieldValue);
						}
					}
					else {
						widgetLookAndFeelConfig.setAdvancedStylingConfig(
							jsonParserFieldValue);
					}
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "backgroundStylesConfig")) {

				if (jsonParserFieldValue != null) {
					if (jsonParserFieldValue instanceof String) {
						String jsonStr = (String)jsonParserFieldValue;

						if ((jsonStr.startsWith("{") &&
							 jsonStr.endsWith("}")) ||
							(jsonStr.startsWith("[") &&
							 jsonStr.endsWith("]"))) {

							try {
								Object parsedValue = parseToMap(jsonStr);
								widgetLookAndFeelConfig.
									setBackgroundStylesConfig(parsedValue);
							}
							catch (Exception e) {
								widgetLookAndFeelConfig.
									setBackgroundStylesConfig(
										jsonParserFieldValue);
							}
						}
						else {
							widgetLookAndFeelConfig.setBackgroundStylesConfig(
								jsonParserFieldValue);
						}
					}
					else {
						widgetLookAndFeelConfig.setBackgroundStylesConfig(
							jsonParserFieldValue);
					}
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "borderStylesConfig")) {

				if (jsonParserFieldValue != null) {
					if (jsonParserFieldValue instanceof String) {
						String jsonStr = (String)jsonParserFieldValue;

						if ((jsonStr.startsWith("{") &&
							 jsonStr.endsWith("}")) ||
							(jsonStr.startsWith("[") &&
							 jsonStr.endsWith("]"))) {

							try {
								Object parsedValue = parseToMap(jsonStr);
								widgetLookAndFeelConfig.setBorderStylesConfig(
									parsedValue);
							}
							catch (Exception e) {
								widgetLookAndFeelConfig.setBorderStylesConfig(
									jsonParserFieldValue);
							}
						}
						else {
							widgetLookAndFeelConfig.setBorderStylesConfig(
								jsonParserFieldValue);
						}
					}
					else {
						widgetLookAndFeelConfig.setBorderStylesConfig(
							jsonParserFieldValue);
					}
				}
			}
			else if (Objects.equals(jsonParserFieldName, "generalConfig")) {
				if (jsonParserFieldValue != null) {
					if (jsonParserFieldValue instanceof String) {
						String jsonStr = (String)jsonParserFieldValue;

						if ((jsonStr.startsWith("{") &&
							 jsonStr.endsWith("}")) ||
							(jsonStr.startsWith("[") &&
							 jsonStr.endsWith("]"))) {

							try {
								Object parsedValue = parseToMap(jsonStr);
								widgetLookAndFeelConfig.setGeneralConfig(
									parsedValue);
							}
							catch (Exception e) {
								widgetLookAndFeelConfig.setGeneralConfig(
									jsonParserFieldValue);
							}
						}
						else {
							widgetLookAndFeelConfig.setGeneralConfig(
								jsonParserFieldValue);
						}
					}
					else {
						widgetLookAndFeelConfig.setGeneralConfig(
							jsonParserFieldValue);
					}
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "marginAndPaddingConfig")) {

				if (jsonParserFieldValue != null) {
					if (jsonParserFieldValue instanceof String) {
						String jsonStr = (String)jsonParserFieldValue;

						if ((jsonStr.startsWith("{") &&
							 jsonStr.endsWith("}")) ||
							(jsonStr.startsWith("[") &&
							 jsonStr.endsWith("]"))) {

							try {
								Object parsedValue = parseToMap(jsonStr);
								widgetLookAndFeelConfig.
									setMarginAndPaddingConfig(parsedValue);
							}
							catch (Exception e) {
								widgetLookAndFeelConfig.
									setMarginAndPaddingConfig(
										jsonParserFieldValue);
							}
						}
						else {
							widgetLookAndFeelConfig.setMarginAndPaddingConfig(
								jsonParserFieldValue);
						}
					}
					else {
						widgetLookAndFeelConfig.setMarginAndPaddingConfig(
							jsonParserFieldValue);
					}
				}
			}
			else if (Objects.equals(jsonParserFieldName, "textStylesConfig")) {
				if (jsonParserFieldValue != null) {
					if (jsonParserFieldValue instanceof String) {
						String jsonStr = (String)jsonParserFieldValue;

						if ((jsonStr.startsWith("{") &&
							 jsonStr.endsWith("}")) ||
							(jsonStr.startsWith("[") &&
							 jsonStr.endsWith("]"))) {

							try {
								Object parsedValue = parseToMap(jsonStr);
								widgetLookAndFeelConfig.setTextStylesConfig(
									parsedValue);
							}
							catch (Exception e) {
								widgetLookAndFeelConfig.setTextStylesConfig(
									jsonParserFieldValue);
							}
						}
						else {
							widgetLookAndFeelConfig.setTextStylesConfig(
								jsonParserFieldValue);
						}
					}
					else {
						widgetLookAndFeelConfig.setTextStylesConfig(
							jsonParserFieldValue);
					}
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