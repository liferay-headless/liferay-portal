/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.rest.client.dto.v1_0;

import com.liferay.launch.rest.client.function.UnsafeSupplier;
import com.liferay.launch.rest.client.serdes.v1_0.LaunchPublishStatusSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Alberto Moreno
 * @generated
 */
@Generated("")
public class LaunchPublishStatus implements Cloneable, Serializable {

	public static LaunchPublishStatus toDTO(String json) {
		return LaunchPublishStatusSerDes.toDTO(json);
	}

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

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setLabel(
		UnsafeSupplier<String, Exception> labelUnsafeSupplier) {

		try {
			label = labelUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String label;

	public String getLabel_i18n() {
		return label_i18n;
	}

	public void setLabel_i18n(String label_i18n) {
		this.label_i18n = label_i18n;
	}

	public void setLabel_i18n(
		UnsafeSupplier<String, Exception> label_i18nUnsafeSupplier) {

		try {
			label_i18n = label_i18nUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String label_i18n;

	@Override
	public LaunchPublishStatus clone() throws CloneNotSupportedException {
		return (LaunchPublishStatus)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof LaunchPublishStatus)) {
			return false;
		}

		LaunchPublishStatus launchPublishStatus = (LaunchPublishStatus)object;

		return Objects.equals(toString(), launchPublishStatus.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return LaunchPublishStatusSerDes.toJSON(this);
	}

}
// LIFERAY-REST-BUILDER-HASH:-1612439253