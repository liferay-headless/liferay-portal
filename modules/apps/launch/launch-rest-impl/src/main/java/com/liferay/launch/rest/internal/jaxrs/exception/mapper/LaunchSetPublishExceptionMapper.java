/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.rest.internal.jaxrs.exception.mapper;

import com.liferay.launch.set.publisher.LaunchSetPublishException;
import com.liferay.portal.vulcan.jaxrs.exception.mapper.BaseExceptionMapper;
import com.liferay.portal.vulcan.jaxrs.exception.mapper.Problem;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.osgi.service.component.annotations.Component;

/**
 * @author Alberto Moreno
 */
@Component(
	property = {
		"osgi.jaxrs.application.select=(osgi.jaxrs.name=Liferay.Launches.REST)",
		"osgi.jaxrs.extension=true",
		"osgi.jaxrs.name=Liferay.Launches.REST.LaunchSetPublishExceptionMapper"
	},
	service = ExceptionMapper.class
)
public class LaunchSetPublishExceptionMapper
	extends BaseExceptionMapper<LaunchSetPublishException> {

	@Override
	protected Problem getProblem(
		LaunchSetPublishException launchSetPublishException) {

		return new Problem(
			Response.Status.CONFLICT, launchSetPublishException.getMessage());
	}

}