/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.api.builder.internal.jaxrs.application;

import com.liferay.api.builder.constants.APIBuilderConstants;
import com.liferay.api.builder.internal.handler.GETHttpServletRequestHandler;
import com.liferay.api.builder.registry.APIBuilderOpenAPIRegistry;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Matija Petanjek
 */
@Component(
	property = {
		"osgi.jaxrs.application.base=" + APIBuilderConstants.BASE_PATH,
		"osgi.jaxrs.extension.select=(osgi.jaxrs.name=Liferay.Vulcan)",
		"osgi.jaxrs.name=Liferay.API.Builder"
	},
	service = Application.class
)
public class APIBuilderApplication extends Application {

	@GET
	@Path("{any: .*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(
			@QueryParam("search") String search, @Context Filter filter,
			@Context Pagination pagination, @Context Sort[] sorts)
		throws Exception {

		return Response.ok(
			_getHttpServletRequestHandler.handle(
				filter, _httpServletRequest, pagination,
				_apiBuilderOpenAPIRegistry.getPathItemEntry(
					_httpServletRequest.getRequestURI()),
				search, sorts)
		).build();
	}

	@Override
	public Set<Object> getSingletons() {
		Set<Object> singletons = new HashSet<>();

		singletons.add(this);

		return singletons;
	}

	@Path("{any: .*}")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response post() {
		return null;
	}

	@Reference
	private APIBuilderOpenAPIRegistry _apiBuilderOpenAPIRegistry;

	@Context
	private Company _company;

	@Reference
	private GETHttpServletRequestHandler _getHttpServletRequestHandler;

	@Context
	private HttpServletRequest _httpServletRequest;

}