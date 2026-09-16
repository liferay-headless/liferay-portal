/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.portal.instances.dto.v1_0;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import com.liferay.petra.function.UnsafeSupplier;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLName;
import com.liferay.portal.vulcan.util.ObjectMapperUtil;

import jakarta.annotation.Generated;

import jakarta.validation.Valid;

import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Alberto Chaparro
 * @generated
 */
@Generated("")
@GraphQLName(
	description = "An asynchronous portal instance operation.",
	value = "PortalInstanceOperation"
)
@io.swagger.v3.oas.annotations.media.Schema(
	description = "An asynchronous portal instance operation."
)
@JsonFilter("Liferay.Vulcan")
@XmlRootElement(name = "PortalInstanceOperation")
public class PortalInstanceOperation implements Serializable {

	public static PortalInstanceOperation toDTO(String json) {
		return ObjectMapperUtil.readValue(PortalInstanceOperation.class, json);
	}

	public static PortalInstanceOperation unsafeToDTO(String json) {
		return ObjectMapperUtil.unsafeReadValue(
			PortalInstanceOperation.class, json);
	}

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "internal unique key."
	)
	public Long getBackgroundTaskId() {
		if (_backgroundTaskIdSupplier != null) {
			backgroundTaskId = _backgroundTaskIdSupplier.get();

			_backgroundTaskIdSupplier = null;
		}

		return backgroundTaskId;
	}

	public void setBackgroundTaskId(Long backgroundTaskId) {
		this.backgroundTaskId = backgroundTaskId;

		_backgroundTaskIdSupplier = null;
	}

	@JsonIgnore
	public void setBackgroundTaskId(
		UnsafeSupplier<Long, Exception> backgroundTaskIdUnsafeSupplier) {

		_backgroundTaskIdSupplier = () -> {
			try {
				return backgroundTaskIdUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField(description = "internal unique key.")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected Long backgroundTaskId;

	@JsonIgnore
	private Supplier<Long> _backgroundTaskIdSupplier;

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "internal unique key of the affected portal instance."
	)
	public Long getCompanyId() {
		if (_companyIdSupplier != null) {
			companyId = _companyIdSupplier.get();

			_companyIdSupplier = null;
		}

		return companyId;
	}

	public void setCompanyId(Long companyId) {
		this.companyId = companyId;

		_companyIdSupplier = null;
	}

	@JsonIgnore
	public void setCompanyId(
		UnsafeSupplier<Long, Exception> companyIdUnsafeSupplier) {

		_companyIdSupplier = () -> {
			try {
				return companyIdUnsafeSupplier.get();
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
		description = "internal unique key of the affected portal instance."
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected Long companyId;

	@JsonIgnore
	private Supplier<Long> _companyIdSupplier;

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "date the operation reached a terminal status."
	)
	public Date getCompletionDate() {
		if (_completionDateSupplier != null) {
			completionDate = _completionDateSupplier.get();

			_completionDateSupplier = null;
		}

		return completionDate;
	}

	public void setCompletionDate(Date completionDate) {
		this.completionDate = completionDate;

		_completionDateSupplier = null;
	}

	@JsonIgnore
	public void setCompletionDate(
		UnsafeSupplier<Date, Exception> completionDateUnsafeSupplier) {

		_completionDateSupplier = () -> {
			try {
				return completionDateUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField(description = "date the operation reached a terminal status.")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected Date completionDate;

	@JsonIgnore
	private Supplier<Date> _completionDateSupplier;

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "date the operation was enqueued."
	)
	public Date getCreateDate() {
		if (_createDateSupplier != null) {
			createDate = _createDateSupplier.get();

			_createDateSupplier = null;
		}

		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;

		_createDateSupplier = null;
	}

	@JsonIgnore
	public void setCreateDate(
		UnsafeSupplier<Date, Exception> createDateUnsafeSupplier) {

		_createDateSupplier = () -> {
			try {
				return createDateUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField(description = "date the operation was enqueued.")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected Date createDate;

	@JsonIgnore
	private Supplier<Date> _createDateSupplier;

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "message of the error that made the operation fail."
	)
	public String getErrorMessage() {
		if (_errorMessageSupplier != null) {
			errorMessage = _errorMessageSupplier.get();

			_errorMessageSupplier = null;
		}

		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;

		_errorMessageSupplier = null;
	}

	@JsonIgnore
	public void setErrorMessage(
		UnsafeSupplier<String, Exception> errorMessageUnsafeSupplier) {

		_errorMessageSupplier = () -> {
			try {
				return errorMessageUnsafeSupplier.get();
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
		description = "message of the error that made the operation fail."
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String errorMessage;

	@JsonIgnore
	private Supplier<String> _errorMessageSupplier;

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "operation performed on the portal instance."
	)
	@JsonGetter("operationType")
	@Valid
	public OperationType getOperationType() {
		if (_operationTypeSupplier != null) {
			operationType = _operationTypeSupplier.get();

			_operationTypeSupplier = null;
		}

		return operationType;
	}

	@JsonIgnore
	public String getOperationTypeAsString() {
		OperationType operationType = getOperationType();

		if (operationType == null) {
			return null;
		}

		return operationType.toString();
	}

	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;

		_operationTypeSupplier = null;
	}

	@JsonIgnore
	public void setOperationType(
		UnsafeSupplier<OperationType, Exception> operationTypeUnsafeSupplier) {

		_operationTypeSupplier = () -> {
			try {
				return operationTypeUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField(description = "operation performed on the portal instance.")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected OperationType operationType;

	@JsonIgnore
	private Supplier<OperationType> _operationTypeSupplier;

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "public unique key (corresponds to company's webId field)"
	)
	public String getPortalInstanceId() {
		if (_portalInstanceIdSupplier != null) {
			portalInstanceId = _portalInstanceIdSupplier.get();

			_portalInstanceIdSupplier = null;
		}

		return portalInstanceId;
	}

	public void setPortalInstanceId(String portalInstanceId) {
		this.portalInstanceId = portalInstanceId;

		_portalInstanceIdSupplier = null;
	}

	@JsonIgnore
	public void setPortalInstanceId(
		UnsafeSupplier<String, Exception> portalInstanceIdUnsafeSupplier) {

		_portalInstanceIdSupplier = () -> {
			try {
				return portalInstanceIdUnsafeSupplier.get();
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
		description = "public unique key (corresponds to company's webId field)"
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String portalInstanceId;

	@JsonIgnore
	private Supplier<String> _portalInstanceIdSupplier;

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "database schema extracted by an export operation."
	)
	public String getSchemaName() {
		if (_schemaNameSupplier != null) {
			schemaName = _schemaNameSupplier.get();

			_schemaNameSupplier = null;
		}

		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;

		_schemaNameSupplier = null;
	}

	@JsonIgnore
	public void setSchemaName(
		UnsafeSupplier<String, Exception> schemaNameUnsafeSupplier) {

		_schemaNameSupplier = () -> {
			try {
				return schemaNameUnsafeSupplier.get();
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
		description = "database schema extracted by an export operation."
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String schemaName;

	@JsonIgnore
	private Supplier<String> _schemaNameSupplier;

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "execution status of the operation."
	)
	@JsonGetter("status")
	@Valid
	public Status getStatus() {
		if (_statusSupplier != null) {
			status = _statusSupplier.get();

			_statusSupplier = null;
		}

		return status;
	}

	@JsonIgnore
	public String getStatusAsString() {
		Status status = getStatus();

		if (status == null) {
			return null;
		}

		return status.toString();
	}

	public void setStatus(Status status) {
		this.status = status;

		_statusSupplier = null;
	}

	@JsonIgnore
	public void setStatus(
		UnsafeSupplier<Status, Exception> statusUnsafeSupplier) {

		_statusSupplier = () -> {
			try {
				return statusUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField(description = "execution status of the operation.")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected Status status;

	@JsonIgnore
	private Supplier<Status> _statusSupplier;

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof PortalInstanceOperation)) {
			return false;
		}

		PortalInstanceOperation portalInstanceOperation =
			(PortalInstanceOperation)object;

		return Objects.equals(toString(), portalInstanceOperation.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		StringBundler sb = new StringBundler();

		sb.append("{");

		DateFormat liferayToJSONDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");

		Long backgroundTaskId = getBackgroundTaskId();

		if (backgroundTaskId != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"backgroundTaskId\": ");

			sb.append(backgroundTaskId);
		}

		Long companyId = getCompanyId();

		if (companyId != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"companyId\": ");

			sb.append(companyId);
		}

		Date completionDate = getCompletionDate();

		if (completionDate != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"completionDate\": ");

			sb.append("\"");

			sb.append(liferayToJSONDateFormat.format(completionDate));

			sb.append("\"");
		}

		Date createDate = getCreateDate();

		if (createDate != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"createDate\": ");

			sb.append("\"");

			sb.append(liferayToJSONDateFormat.format(createDate));

			sb.append("\"");
		}

		String errorMessage = getErrorMessage();

		if (errorMessage != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"errorMessage\": ");

			sb.append("\"");

			sb.append(_escape(errorMessage));

			sb.append("\"");
		}

		OperationType operationType = getOperationType();

		if (operationType != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"operationType\": ");

			sb.append("\"");
			sb.append(operationType);
			sb.append("\"");
		}

		String portalInstanceId = getPortalInstanceId();

		if (portalInstanceId != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"portalInstanceId\": ");

			sb.append("\"");

			sb.append(_escape(portalInstanceId));

			sb.append("\"");
		}

		String schemaName = getSchemaName();

		if (schemaName != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"schemaName\": ");

			sb.append("\"");

			sb.append(_escape(schemaName));

			sb.append("\"");
		}

		Status status = getStatus();

		if (status != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"status\": ");

			sb.append("\"");
			sb.append(status);
			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	@io.swagger.v3.oas.annotations.media.Schema(
		accessMode = io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY,
		defaultValue = "com.liferay.headless.portal.instances.dto.v1_0.PortalInstanceOperation",
		name = "x-class-name"
	)
	public String xClassName;

	@GraphQLName("OperationType")
	public static enum OperationType {

		ADD("ADD"), COPY("COPY"), DELETE("DELETE"), EXPORT("EXPORT"),
		IMPORT("IMPORT");

		@JsonCreator
		public static OperationType create(String value) {
			if ((value == null) || value.equals("")) {
				return null;
			}

			for (OperationType operationType : values()) {
				if (Objects.equals(operationType.getValue(), value)) {
					return operationType;
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

		private OperationType(String value) {
			_value = value;
		}

		private final String _value;

	}

	@GraphQLName("Status")
	public static enum Status {

		CANCELLED("CANCELLED"), COMPLETED_WITH_ERRORS("COMPLETED_WITH_ERRORS"),
		FAILED("FAILED"), IN_PROGRESS("IN_PROGRESS"), NEW("NEW"),
		QUEUED("QUEUED"), SUCCESSFUL("SUCCESSFUL");

		@JsonCreator
		public static Status create(String value) {
			if ((value == null) || value.equals("")) {
				return null;
			}

			for (Status status : values()) {
				if (Objects.equals(status.getValue(), value)) {
					return status;
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

		private Status(String value) {
			_value = value;
		}

		private final String _value;

	}

	private static String _escape(Object object) {
		return StringUtil.replace(
			String.valueOf(object), _JSON_ESCAPE_STRINGS[0],
			_JSON_ESCAPE_STRINGS[1]);
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

				Object[] valueArray = (Object[])value;

				for (int i = 0; i < valueArray.length; i++) {
					if (valueArray[i] instanceof Map) {
						sb.append(_toJSON((Map<String, ?>)valueArray[i]));
					}
					else if (valueArray[i] instanceof String) {
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
				sb.append(_toJSON((Map<String, ?>)value));
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

	private static String _toJSON(Object value) {
		if (value instanceof Collection) {
			return String.valueOf(
				JSONFactoryUtil.createJSONArray((Collection<?>)value));
		}
		else if (value instanceof Map) {
			return String.valueOf(
				JSONFactoryUtil.createJSONObject((Map<?, ?>)value));
		}
		else if (value instanceof Object[]) {
			return String.valueOf(
				JSONFactoryUtil.createJSONArray(
					Arrays.asList((Object[])value)));
		}
		else if (value instanceof String) {
			return StringBundler.concat("\"", _escape(value), "\"");
		}

		return String.valueOf(value);
	}

	private static final String[][] _JSON_ESCAPE_STRINGS = {
		{"\\", "\"", "\b", "\f", "\n", "\r", "\t"},
		{"\\\\", "\\\"", "\\b", "\\f", "\\n", "\\r", "\\t"}
	};

	private Map<String, Serializable> _extendedProperties;

}
// LIFERAY-REST-BUILDER-HASH:2079950408