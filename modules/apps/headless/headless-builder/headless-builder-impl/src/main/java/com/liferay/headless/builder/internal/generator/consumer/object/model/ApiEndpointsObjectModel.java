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

package com.liferay.headless.builder.internal.generator.consumer.object.model;

import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactory;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.vulcan.pagination.Page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Luis Miguel Barcos
 */
public class ApiEndpointsObjectModel extends ObjectModel {

	public ApiEndpointsObjectModel(
			String apiApplicationERC, long companyId,
			ObjectDefinitionLocalService objectDefinitionLocalService,
			ObjectEntryLocalService objectEntryLocalService,
			ObjectEntryManager objectEntryManager,
			PermissionCheckerFactory permissionCheckerFactory,
			UserLocalService userLocalService)
		throws Exception {

		super(
			apiApplicationERC, companyId, objectDefinitionLocalService,
			objectEntryLocalService, objectEntryManager,
			permissionCheckerFactory, userLocalService);
	}

	public List<ApiEndpoint> getApiEndpoints() {
		return _apiEndpoints;
	}

	public static class ApiEndpoint {

		public ApiEndpoint(String method, String path, String scope) {
			_method = method;
			_path = path;
			_scope = scope;
		}

		public String getMethod() {
			return _method;
		}

		public String getPath() {
			return _path;
		}

		public String getScope() {
			return _scope;
		}

		private final String _method;
		private final String _path;
		private final String _scope;

	}

	@Override
	protected void initObjectModel() throws Exception {
		Page<ObjectEntry> objectEntriesPage = getObjectEntries(
			String.format(
				"%s/externalReferenceCode eq '%s'",
				_APPLICATION_ENDPOINT_OBJECT_RELATIONSHIP_NAME,
				apiApplicationERC),
			_OBJECT_DEFINITION_ERC);

		Collection<ObjectEntry> items = objectEntriesPage.getItems();

		_apiEndpoints = new ArrayList<>();

		for (ObjectEntry endpointObjectEntry : items) {
			_apiEndpoints.add(
				new ApiEndpoint(
					(String)getObjectEntryPropertyValue(
						endpointObjectEntry, _HTTP_METHOD_PROPERTY_NAME),
					(String)getObjectEntryPropertyValue(
						endpointObjectEntry, _PATH_PROPERTY_NAME),
					(String)getObjectEntryPropertyValue(
						endpointObjectEntry, _SCOPE_PROPERTY_NAME)));
		}
	}

	private static final String _APPLICATION_ENDPOINT_OBJECT_RELATIONSHIP_NAME =
		"applicationEndpoints";

	private static final String _HTTP_METHOD_PROPERTY_NAME = "hTTPMethod";

	private static final String _OBJECT_DEFINITION_ERC = "MSOD_API_ENDPOINT";

	private static final String _PATH_PROPERTY_NAME = "path";

	private static final String _SCOPE_PROPERTY_NAME = "scope";

	private List<ApiEndpoint> _apiEndpoints;

}