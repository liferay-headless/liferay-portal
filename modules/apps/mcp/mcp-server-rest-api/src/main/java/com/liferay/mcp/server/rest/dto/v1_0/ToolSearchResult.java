/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.dto.v1_0;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.liferay.petra.function.UnsafeSupplier;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLName;
import com.liferay.portal.vulcan.util.ObjectMapperUtil;

import jakarta.annotation.Generated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Alejandro Tardín
 * @generated
 */
@Generated("")
@GraphQLName(
	description = "A tool matching a search query, ranked by relevance. It describes an operation reachable through this server; it is not itself one of this server's tools. Run it by calling `postToolSetToolSetNameToolInvoke` with its `toolSetName` and `toolName`. Calling `toolName` as though it were a tool of its own fails, because the only tools this server exposes are the handful listed in its tool list. When `requiredInputSchema` is present it lists the arguments the tool cannot run without, so a tool taking only required arguments can be invoked straight from a search result without calling `getToolSetToolSetNameTool` first. Call `getToolSetToolSetNameTool` when you need the optional arguments too.",
	value = "ToolSearchResult"
)
@io.swagger.v3.oas.annotations.media.Schema(
	description = "A tool matching a search query, ranked by relevance. It describes an operation reachable through this server; it is not itself one of this server's tools. Run it by calling `postToolSetToolSetNameToolInvoke` with its `toolSetName` and `toolName`. Calling `toolName` as though it were a tool of its own fails, because the only tools this server exposes are the handful listed in its tool list. When `requiredInputSchema` is present it lists the arguments the tool cannot run without, so a tool taking only required arguments can be invoked straight from a search result without calling `getToolSetToolSetNameTool` first. Call `getToolSetToolSetNameTool` when you need the optional arguments too.",
	requiredProperties = {"toolName", "toolSetName"}
)
@JsonFilter("Liferay.Vulcan")
@XmlRootElement(name = "ToolSearchResult")
public class ToolSearchResult implements Serializable {

	public static ToolSearchResult toDTO(String json) {
		return ObjectMapperUtil.readValue(ToolSearchResult.class, json);
	}

	public static ToolSearchResult unsafeToDTO(String json) {
		return ObjectMapperUtil.unsafeReadValue(ToolSearchResult.class, json);
	}

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "What the tool does. More than half the catalogue documents nothing, and for those this falls back to the operation's own name rendered as words, so it is always populated but not always informative. For some tools it ends with a hint on how to use them, which batch tools always carry."
	)
	public String getDescription() {
		if (_descriptionSupplier != null) {
			description = _descriptionSupplier.get();

			_descriptionSupplier = null;
		}

		return description;
	}

	public void setDescription(String description) {
		this.description = description;

		_descriptionSupplier = null;
	}

	@JsonIgnore
	public void setDescription(
		UnsafeSupplier<String, Exception> descriptionUnsafeSupplier) {

		_descriptionSupplier = () -> {
			try {
				return descriptionUnsafeSupplier.get();
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
		description = "What the tool does. More than half the catalogue documents nothing, and for those this falls back to the operation's own name rendered as words, so it is always populated but not always informative. For some tools it ends with a hint on how to use them, which batch tools always carry."
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String description;

	@JsonIgnore
	private Supplier<String> _descriptionSupplier;

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "What the tool's path needs identified before it can be invoked. Returned on the highest ranked results when `includeRequiredInputSchema` is true, alongside their `requiredInputSchema`. Read it before invoking: each entry either names the operation that resolves the parameter, so no further search is needed, or says what the parameter accepts. Read it before invoking: each entry either names the operation that resolves the parameter, so no further search is needed, or says what the parameter accepts."
	)
	@Valid
	public Prerequisite[] getPrerequisites() {
		if (_prerequisitesSupplier != null) {
			prerequisites = _prerequisitesSupplier.get();

			_prerequisitesSupplier = null;
		}

		return prerequisites;
	}

	public void setPrerequisites(Prerequisite[] prerequisites) {
		this.prerequisites = prerequisites;

		_prerequisitesSupplier = null;
	}

	@JsonIgnore
	public void setPrerequisites(
		UnsafeSupplier<Prerequisite[], Exception> prerequisitesUnsafeSupplier) {

		_prerequisitesSupplier = () -> {
			try {
				return prerequisitesUnsafeSupplier.get();
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
		description = "What the tool's path needs identified before it can be invoked. Returned on the highest ranked results when `includeRequiredInputSchema` is true, alongside their `requiredInputSchema`. Read it before invoking: each entry either names the operation that resolves the parameter, so no further search is needed, or says what the parameter accepts. Read it before invoking: each entry either names the operation that resolves the parameter, so no further search is needed, or says what the parameter accepts."
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected Prerequisite[] prerequisites;

	@JsonIgnore
	private Supplier<Prerequisite[]> _prerequisitesSupplier;

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "JSON Schema covering only the tool's required arguments, returned on the highest ranked results when `includeRequiredInputSchema` is true, alongside their `prerequisites`. A result that comes back without it needs `getToolSetToolSetNameTool` before it can be invoked. Listing tools also keep `fields`, so the response can be trimmed without fetching the full schema. Enough to invoke the tool in the common case; call `getToolSetToolSetNameTool` for the full schema including optional arguments."
	)
	@Valid
	public Map<String, ?> getRequiredInputSchema() {
		if (_requiredInputSchemaSupplier != null) {
			requiredInputSchema = _requiredInputSchemaSupplier.get();

			_requiredInputSchemaSupplier = null;
		}

		return requiredInputSchema;
	}

	public void setRequiredInputSchema(Map<String, ?> requiredInputSchema) {
		this.requiredInputSchema = requiredInputSchema;

		_requiredInputSchemaSupplier = null;
	}

	@JsonIgnore
	public void setRequiredInputSchema(
		UnsafeSupplier<Map<String, ?>, Exception>
			requiredInputSchemaUnsafeSupplier) {

		_requiredInputSchemaSupplier = () -> {
			try {
				return requiredInputSchemaUnsafeSupplier.get();
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
		description = "JSON Schema covering only the tool's required arguments, returned on the highest ranked results when `includeRequiredInputSchema` is true, alongside their `prerequisites`. A result that comes back without it needs `getToolSetToolSetNameTool` before it can be invoked. Listing tools also keep `fields`, so the response can be trimmed without fetching the full schema. Enough to invoke the tool in the common case; call `getToolSetToolSetNameTool` for the full schema including optional arguments."
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected Map<String, ?> requiredInputSchema;

	@JsonIgnore
	private Supplier<Map<String, ?>> _requiredInputSchemaSupplier;

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "Stable identifier of the operation within its tool set, such as `postSiteStructuredContent`. Not a callable tool: pass it verbatim as the `toolName` argument of `getToolSetToolSetNameTool` or `postToolSetToolSetNameToolInvoke`, alongside `toolSetName`."
	)
	public String getToolName() {
		if (_toolNameSupplier != null) {
			toolName = _toolNameSupplier.get();

			_toolNameSupplier = null;
		}

		return toolName;
	}

	public void setToolName(String toolName) {
		this.toolName = toolName;

		_toolNameSupplier = null;
	}

	@JsonIgnore
	public void setToolName(
		UnsafeSupplier<String, Exception> toolNameUnsafeSupplier) {

		_toolNameSupplier = () -> {
			try {
				return toolNameUnsafeSupplier.get();
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
		description = "Stable identifier of the operation within its tool set, such as `postSiteStructuredContent`. Not a callable tool: pass it verbatim as the `toolName` argument of `getToolSetToolSetNameTool` or `postToolSetToolSetNameToolInvoke`, alongside `toolSetName`."
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotEmpty
	protected String toolName;

	@JsonIgnore
	private Supplier<String> _toolNameSupplier;

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "Stable identifier of the tool set that exposes this tool. Pass this verbatim as `toolSetName` to `getToolSetToolSetNameTool` and `postToolSetToolSetNameToolInvoke`."
	)
	public String getToolSetName() {
		if (_toolSetNameSupplier != null) {
			toolSetName = _toolSetNameSupplier.get();

			_toolSetNameSupplier = null;
		}

		return toolSetName;
	}

	public void setToolSetName(String toolSetName) {
		this.toolSetName = toolSetName;

		_toolSetNameSupplier = null;
	}

	@JsonIgnore
	public void setToolSetName(
		UnsafeSupplier<String, Exception> toolSetNameUnsafeSupplier) {

		_toolSetNameSupplier = () -> {
			try {
				return toolSetNameUnsafeSupplier.get();
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
		description = "Stable identifier of the tool set that exposes this tool. Pass this verbatim as `toolSetName` to `getToolSetToolSetNameTool` and `postToolSetToolSetNameToolInvoke`."
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotEmpty
	protected String toolSetName;

	@JsonIgnore
	private Supplier<String> _toolSetNameSupplier;

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof ToolSearchResult)) {
			return false;
		}

		ToolSearchResult toolSearchResult = (ToolSearchResult)object;

		return Objects.equals(toString(), toolSearchResult.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		StringBundler sb = new StringBundler();

		sb.append("{");

		String description = getDescription();

		if (description != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"description\": ");

			sb.append("\"");

			sb.append(_escape(description));

			sb.append("\"");
		}

		Prerequisite[] prerequisites = getPrerequisites();

		if (prerequisites != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"prerequisites\": ");

			sb.append("[");

			for (int i = 0; i < prerequisites.length; i++) {
				sb.append(String.valueOf(prerequisites[i]));

				if ((i + 1) < prerequisites.length) {
					sb.append(", ");
				}
			}

			sb.append("]");
		}

		Map<String, ?> requiredInputSchema = getRequiredInputSchema();

		if (requiredInputSchema != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"requiredInputSchema\": ");

			sb.append(_toJSON(requiredInputSchema));
		}

		String toolName = getToolName();

		if (toolName != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"toolName\": ");

			sb.append("\"");

			sb.append(_escape(toolName));

			sb.append("\"");
		}

		String toolSetName = getToolSetName();

		if (toolSetName != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"toolSetName\": ");

			sb.append("\"");

			sb.append(_escape(toolSetName));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	@io.swagger.v3.oas.annotations.media.Schema(
		accessMode = io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY,
		defaultValue = "com.liferay.mcp.server.rest.dto.v1_0.ToolSearchResult",
		name = "x-class-name"
	)
	public String xClassName;

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
// LIFERAY-REST-BUILDER-HASH:938403818