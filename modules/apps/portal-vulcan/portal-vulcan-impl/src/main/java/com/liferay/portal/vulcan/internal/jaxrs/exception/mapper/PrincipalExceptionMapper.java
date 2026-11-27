/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.jaxrs.exception.mapper;

import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.servlet.HttpMethods;
import com.liferay.portal.vulcan.jaxrs.exception.mapper.BaseExceptionMapper;
import com.liferay.portal.vulcan.jaxrs.exception.mapper.Problem;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Providers;

import java.util.Arrays;

/**
 * Converts any {@code PrincipalException} to a {@code 404} error in case it is
 * a GET request or the failed check was {@code VIEW}, so that an entity the
 * caller may not view is indistinguishable from a missing one. Converts any
 * other {@code PrincipalException} to a {@code 403} error.
 *
 * @author Brian Wing Shun Chan
 * @review
 */
public class PrincipalExceptionMapper
	extends BaseExceptionMapper<PrincipalException> {

	@Override
	public Response toResponse(PrincipalException principalException) {
		String method = _httpServletRequest.getMethod();

		if (method.equals(HttpMethods.GET) ||
			!_hasViewPermission(principalException)) {

			ExceptionMapper<NotFoundException> exceptionMapper =
				_providers.getExceptionMapper(NotFoundException.class);

			return exceptionMapper.toResponse(
				new NotFoundException(principalException));
		}

		return super.toResponse(principalException);
	}

	@Override
	protected Problem getProblem(PrincipalException principalException) {
		if (_log.isWarnEnabled()) {
			_log.warn(principalException);
		}

		return new Problem(
			Response.Status.FORBIDDEN,
			LanguageUtil.get(_httpServletRequest.getLocale(), "forbidden"));
	}

	private boolean _hasViewPermission(PrincipalException principalException) {
		if (!(principalException instanceof
				PrincipalException.MustHavePermission)) {

			return true;
		}

		PrincipalException.MustHavePermission mustHavePermission =
			(PrincipalException.MustHavePermission)principalException;

		if (Arrays.equals(
				mustHavePermission.actionId, new String[] {ActionKeys.VIEW})) {

			return false;
		}

		return true;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		PrincipalExceptionMapper.class);

	@Context
	private HttpServletRequest _httpServletRequest;

	@Context
	private Providers _providers;

}