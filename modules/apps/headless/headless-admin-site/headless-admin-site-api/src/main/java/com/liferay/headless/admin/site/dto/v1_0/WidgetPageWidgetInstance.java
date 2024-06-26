/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.dto.v1_0;



import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import com.liferay.petra.function.UnsafeSupplier;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLName;
import com.liferay.portal.vulcan.jackson.databind.deser.JSONStringStdDeserializer;
import com.liferay.portal.vulcan.util.ObjectMapperUtil;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

import java.math.BigDecimal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Generated;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Rubén Pulido
 * @generated
 */




@Generated("")
@GraphQLName(
		description = "A widget instance in a widget page.", value = "WidgetPageWidgetInstance"
)
@JsonFilter("Liferay.Vulcan")
@XmlRootElement(name = "WidgetPageWidgetInstance")
public class WidgetPageWidgetInstance  implements Serializable {

	public static WidgetPageWidgetInstance toDTO(String json) {
		return ObjectMapperUtil.readValue(WidgetPageWidgetInstance.class, json);
	}

	public static WidgetPageWidgetInstance unsafeToDTO(String json) {
		return ObjectMapperUtil.unsafeReadValue(WidgetPageWidgetInstance.class, json);
	}









		@Schema(

				description = "The external reference code of the widget instance."

		)




		public String getExternalReferenceCode() {
			if (_externalReferenceCodeSupplier != null) {
				externalReferenceCode = _externalReferenceCodeSupplier.get();

				_externalReferenceCodeSupplier = null;
			}

			return externalReferenceCode;
		}


		public void setExternalReferenceCode(String externalReferenceCode) {
			this.externalReferenceCode = externalReferenceCode;

			_externalReferenceCodeSupplier = null;
		}

		@JsonIgnore
		public void setExternalReferenceCode(UnsafeSupplier<String, Exception> externalReferenceCodeUnsafeSupplier) {
				_externalReferenceCodeSupplier = () -> {
					try {
						return externalReferenceCodeUnsafeSupplier.get();
					}
					catch (RuntimeException runtimeException) {
						throw runtimeException;
					}
					catch (Exception exception) {
						throw new RuntimeException(exception);
					}
				};
		}

		@GraphQLField(
				description = "The external reference code of the widget instance."
		)
		@JsonProperty(
				access = JsonProperty.Access.READ_WRITE

		)
		protected String externalReferenceCode;

		@JsonIgnore
		private Supplier<String> _externalReferenceCodeSupplier;







		@Schema(


		)

			@Valid



			@JsonGetter("wordSpacing")

		public WordSpacing getWordSpacing() {
			if (_wordSpacingSupplier != null) {
				wordSpacing = _wordSpacingSupplier.get();

				_wordSpacingSupplier = null;
			}

			return wordSpacing;
		}

			@JsonIgnore
			public String getWordSpacingAsString() {
				WordSpacing wordSpacing = getWordSpacing();

				if (wordSpacing == null) {
					return null;
				}

				return wordSpacing.toString();
			}

		public void setWordSpacing(WordSpacing wordSpacing) {
			this.wordSpacing = wordSpacing;

			_wordSpacingSupplier = null;
		}

		@JsonIgnore
		public void setWordSpacing(UnsafeSupplier<WordSpacing, Exception> wordSpacingUnsafeSupplier) {
				_wordSpacingSupplier = () -> {
					try {
						return wordSpacingUnsafeSupplier.get();
					}
					catch (RuntimeException runtimeException) {
						throw runtimeException;
					}
					catch (Exception exception) {
						throw new RuntimeException(exception);
					}
				};
		}

		@GraphQLField(
		)
		@JsonProperty(
				access = JsonProperty.Access.READ_WRITE

		)
		protected WordSpacing wordSpacing;

		@JsonIgnore
		private Supplier<WordSpacing> _wordSpacingSupplier;

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof WidgetPageWidgetInstance)) {
			return false;
		}

		WidgetPageWidgetInstance widgetPageWidgetInstance = (WidgetPageWidgetInstance)object;

		return Objects.equals(toString(), widgetPageWidgetInstance.toString());
	}


	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		StringBundler sb = new StringBundler();

		sb.append("{");






			String externalReferenceCode = getExternalReferenceCode();

			if (externalReferenceCode != null) {
				if (sb.length() > 1) {
					sb.append(", ");
				}


				sb.append("\"externalReferenceCode\": ");

							sb.append("\"");

								sb.append(_escape(externalReferenceCode));

							sb.append("\"");
			}


			WordSpacing wordSpacing = getWordSpacing();

			if (wordSpacing != null) {
				if (sb.length() > 1) {
					sb.append(", ");
				}


				sb.append("\"wordSpacing\": ");

							sb.append("\"");

								sb.append(wordSpacing);

							sb.append("\"");
			}

		sb.append("}");

		return sb.toString();
	}

	@Schema(accessMode = Schema.AccessMode.READ_ONLY, defaultValue = "com.liferay.headless.admin.site.dto.v1_0.WidgetPageWidgetInstance", name = "x-class-name")
	public String xClassName;

		@GraphQLName("WordSpacing")
		public static enum WordSpacing {

			_1EM("-1em")

				,
					_095EM("-0.95em")

				,
					09EM("0.9em")

				,
					095EM("0.95em")

				,
					12EM("1.2em")

				,
					12EM("12em")

		;

		@JsonCreator
		public static WordSpacing create(String value) {
			if ((value == null) || value.equals("")) {
				return null;
			}

			for (WordSpacing wordSpacing : values()) {
				if (Objects.equals(wordSpacing.getValue(), value)) {
					return wordSpacing;
				}
			}

			throw new IllegalArgumentException("Invalid enum value: " + value);
		}

		@JsonValue
		public String getValue() {
			return _value;
		}

		@Override
		public String toString() {
			return _value;
		}

		private WordSpacing(String value) {
			_value = value;
		}

		private final String _value;

		}

	private static String _escape(Object object) {
		return StringUtil.replace(String.valueOf(object), _JSON_ESCAPE_STRINGS[0], _JSON_ESCAPE_STRINGS[1]);
	}

	private static boolean _isArray(Object value) {
		if (value == null) {
			return false;
		}

		Class<?> clazz = value.getClass();

		return clazz.isArray();
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
			sb.append(_escape(entry.getKey()));
			sb.append("\": ");

			Object value = entry.getValue();

			if (_isArray(value)) {
				sb.append("[");

				Object[] valueArray = (Object[]) value;

				for (int i = 0; i < valueArray.length; i++) {
					if (valueArray[i] instanceof String) {
						sb.append("\"");
						sb.append(valueArray[i]);
						sb.append("\"");
					}
					else {
						sb.append(valueArray[i]);
					}

					if ((i + 1) < valueArray.length) {
						sb.append(", ");
					}
				}

				sb.append("]");
			}
			else if (value instanceof Map) {
				sb.append(_toJSON((Map<String, ?>) value));
			}
			else if (value instanceof String) {
				sb.append("\"");
				sb.append(_escape(value));
				sb.append("\"");
			}
			else {
				sb.append(value);
			}

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static final String[][] _JSON_ESCAPE_STRINGS = {
		{"\\", "\"", "\b", "\f", "\n", "\r", "\t"},
		{"\\\\", "\\\"", "\\b", "\\f", "\\n", "\\r", "\\t"}
	};

	private Map<String, Serializable> _extendedProperties;

}