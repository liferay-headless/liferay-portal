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

import com.liferay.asset.kernel.NoSuchClassTypeException;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactory;
import com.liferay.portal.kernel.service.UserLocalService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Luis Miguel Barcos
 */
@Component(service = ObjectModelsFactory.class)
public class ObjectModelsFactory {

	@SuppressWarnings("unchecked")
	public <T extends ObjectModel> T getObjectModel(
			String apiApplicationERC, Class<T> clazz)
		throws Exception {

		if (clazz.isAssignableFrom(ApiApplicationObjectModel.class)) {
			return (T)new ApiApplicationObjectModel(
				apiApplicationERC, CompanyThreadLocal.getCompanyId(),
				_objectDefinitionLocalService, _objectEntryLocalService,
				_objectEntryManager, _permissionCheckerFactory,
				_userLocalService);
		}
		else if (clazz.isAssignableFrom(ApiEndpointsObjectModel.class)) {
			return (T)new ApiEndpointsObjectModel(
				apiApplicationERC, CompanyThreadLocal.getCompanyId(),
				_objectDefinitionLocalService, _objectEntryLocalService,
				_objectEntryManager, _permissionCheckerFactory,
				_userLocalService);
		}

		throw new NoSuchClassTypeException();
	}

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference(target = "(object.entry.manager.storage.type=default)")
	private ObjectEntryManager _objectEntryManager;

	@Reference
	private PermissionCheckerFactory _permissionCheckerFactory;

	@Reference
	private UserLocalService _userLocalService;

}