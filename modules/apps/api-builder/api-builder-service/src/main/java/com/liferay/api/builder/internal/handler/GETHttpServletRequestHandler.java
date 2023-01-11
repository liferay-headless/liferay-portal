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

package com.liferay.api.builder.internal.handler;

import com.liferay.api.builder.internal.json.writer.JSONWriter;
import com.liferay.api.builder.internal.util.URIUtil;
import com.liferay.portal.kernel.model.PersistedModel;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.service.PersistedModelLocalService;
import com.liferay.portal.kernel.service.PersistedModelLocalServiceRegistryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.yaml.openapi.Get;
import com.liferay.portal.vulcan.yaml.openapi.MappingsDefinition;
import com.liferay.portal.vulcan.yaml.openapi.PathItem;

import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Matija Petanjek
 */
@Component(service = GETHttpServletRequestHandler.class)
public class GETHttpServletRequestHandler {

	public String handle(
			Filter filter, HttpServletRequest httpServletRequest,
			Pagination pagination,
			Map.Entry<Pattern, Map.Entry<String, PathItem>> pathItemEntry,
			String search, Sort[] sorts)
		throws Exception {

		Map.Entry<String, PathItem> pathItem = pathItemEntry.getValue();

		MappingsDefinition mappingsDefinition = _getMappingDefinition(pathItem);

		Map<String, String> pathParams = URIUtil.getPathParams(
			httpServletRequest.getRequestURI(), pathItemEntry.getKey(),
			pathItem.getKey());

		// TODO use elasticsearch first to handle filter/search/sort/pagination
		//  params

		PersistedModelLocalService persistedModelLocalService =
			PersistedModelLocalServiceRegistryUtil.
				getPersistedModelLocalService(mappingsDefinition.getEntity());

		// TODO how to know which path parameter to use to query entity?

		PersistedModel persistedModel =
			persistedModelLocalService.getPersistedModel(
				GetterUtil.getLong(pathParams.get("id")));

		// TODO extraction of field values from persisted model should be
		//  handled by Info Framework

		return _jsonWriter.toJSON(
			persistedModel, mappingsDefinition.getMappings());
	}

	private MappingsDefinition _getMappingDefinition(
		Map.Entry<String, PathItem> pathItemEntry) {

		PathItem pathItem = pathItemEntry.getValue();

		Get get = pathItem.getGet();

		return get.getMappingsDefinition();
	}

	@Reference
	private JSONWriter _jsonWriter;

}