/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.exception;

/**
 * @author Brian Wing Shun Chan
 */
public class NoSuchModelException extends PortalException {

	public NoSuchModelException() {
	}

	public NoSuchModelException(String msg) {
		super(msg);
	}

	public NoSuchModelException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public NoSuchModelException(Throwable throwable) {
		super(throwable);
	}

	public String getExternalReferenceCode() {
		return _externalReferenceCode;
	}

	public void setExternalReferenceCode(String externalReferenceCode) {
		_externalReferenceCode = externalReferenceCode;
	}

	private String _externalReferenceCode;

}