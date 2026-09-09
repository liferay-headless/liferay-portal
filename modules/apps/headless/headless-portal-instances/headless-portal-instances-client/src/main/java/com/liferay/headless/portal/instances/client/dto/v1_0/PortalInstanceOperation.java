/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.portal.instances.client.dto.v1_0;

import com.liferay.headless.portal.instances.client.function.UnsafeSupplier;
import com.liferay.headless.portal.instances.client.serdes.v1_0.PortalInstanceOperationSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Date;
import java.util.Objects;

/**
 * @author Alberto Chaparro
 * @generated
 */
@Generated("")
public class PortalInstanceOperation implements Cloneable, Serializable {

	public static PortalInstanceOperation toDTO(String json) {
		return PortalInstanceOperationSerDes.toDTO(json);
	}

	public Long getBackgroundTaskId() {
		return backgroundTaskId;
	}

	public void setBackgroundTaskId(Long backgroundTaskId) {
		this.backgroundTaskId = backgroundTaskId;
	}

	public void setBackgroundTaskId(
		UnsafeSupplier<Long, Exception> backgroundTaskIdUnsafeSupplier) {

		try {
			backgroundTaskId = backgroundTaskIdUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long backgroundTaskId;

	public Long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}

	public void setCompanyId(
		UnsafeSupplier<Long, Exception> companyIdUnsafeSupplier) {

		try {
			companyId = companyIdUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long companyId;

	public Date getCompletionDate() {
		return completionDate;
	}

	public void setCompletionDate(Date completionDate) {
		this.completionDate = completionDate;
	}

	public void setCompletionDate(
		UnsafeSupplier<Date, Exception> completionDateUnsafeSupplier) {

		try {
			completionDate = completionDateUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Date completionDate;

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public void setCreateDate(
		UnsafeSupplier<Date, Exception> createDateUnsafeSupplier) {

		try {
			createDate = createDateUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Date createDate;

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void setErrorMessage(
		UnsafeSupplier<String, Exception> errorMessageUnsafeSupplier) {

		try {
			errorMessage = errorMessageUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String errorMessage;

	public OperationType getOperationType() {
		return operationType;
	}

	public String getOperationTypeAsString() {
		if (operationType == null) {
			return null;
		}

		return operationType.toString();
	}

	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}

	public void setOperationType(
		UnsafeSupplier<OperationType, Exception> operationTypeUnsafeSupplier) {

		try {
			operationType = operationTypeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected OperationType operationType;

	public String getPortalInstanceId() {
		return portalInstanceId;
	}

	public void setPortalInstanceId(String portalInstanceId) {
		this.portalInstanceId = portalInstanceId;
	}

	public void setPortalInstanceId(
		UnsafeSupplier<String, Exception> portalInstanceIdUnsafeSupplier) {

		try {
			portalInstanceId = portalInstanceIdUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String portalInstanceId;

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public void setSchemaName(
		UnsafeSupplier<String, Exception> schemaNameUnsafeSupplier) {

		try {
			schemaName = schemaNameUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String schemaName;

	public Status getStatus() {
		return status;
	}

	public String getStatusAsString() {
		if (status == null) {
			return null;
		}

		return status.toString();
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setStatus(
		UnsafeSupplier<Status, Exception> statusUnsafeSupplier) {

		try {
			status = statusUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Status status;

	@Override
	public PortalInstanceOperation clone() throws CloneNotSupportedException {
		return (PortalInstanceOperation)super.clone();
	}

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
		return PortalInstanceOperationSerDes.toJSON(this);
	}

	public static enum OperationType {

		ADD("ADD"), COPY("COPY"), DELETE("DELETE"), EXPORT("EXPORT"),
		IMPORT("IMPORT");

		public static OperationType create(String value) {
			for (OperationType operationType : values()) {
				if (Objects.equals(operationType.getValue(), value) ||
					Objects.equals(operationType.name(), value)) {

					return operationType;
				}
			}

			return null;
		}

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

	public static enum Status {

		CANCELLED("CANCELLED"), COMPLETED_WITH_ERRORS("COMPLETED_WITH_ERRORS"),
		FAILED("FAILED"), IN_PROGRESS("IN_PROGRESS"), NEW("NEW"),
		QUEUED("QUEUED"), SUCCESSFUL("SUCCESSFUL");

		public static Status create(String value) {
			for (Status status : values()) {
				if (Objects.equals(status.getValue(), value) ||
					Objects.equals(status.name(), value)) {

					return status;
				}
			}

			return null;
		}

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

}
// LIFERAY-REST-BUILDER-HASH:-850516572