/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.internal.set.permission;

import com.liferay.launch.entry.manager.LaunchEntryManager;
import com.liferay.launch.entry.manager.LaunchEntryManagerRegistry;
import com.liferay.launch.set.permission.LaunchSetPreviewPermission;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermissionRegistryUtil;
import com.liferay.portal.kernel.util.MapUtil;

import java.io.Serializable;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jose Luis Navarro
 */
@Component(service = LaunchSetPreviewPermission.class)
public class LaunchSetPreviewPermissionImpl
	implements LaunchSetPreviewPermission {

	@Override
	public boolean contains(
			PermissionChecker permissionChecker, long launchSetId)
		throws PortalException {

		ObjectEntry launchSetObjectEntry =
			_objectEntryLocalService.fetchObjectEntry(launchSetId);

		if (launchSetObjectEntry == null) {
			return false;
		}

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.getObjectDefinition(
				launchSetObjectEntry.getObjectDefinitionId());

		if (!_contains(
				permissionChecker, objectDefinition.getClassName(),
				launchSetId)) {

			return false;
		}

		ObjectRelationship objectRelationship =
			_objectRelationshipLocalService.getObjectRelationship(
				launchSetObjectEntry.getObjectDefinitionId(),
				"launchSetToLaunchEntries");

		for (ObjectEntry launchEntryObjectEntry :
				_objectEntryLocalService.getOneToManyObjectEntries(
					launchSetObjectEntry.getGroupId(),
					objectRelationship.getObjectRelationshipId(), null, false,
					launchSetId, true, null, QueryUtil.ALL_POS,
					QueryUtil.ALL_POS, null)) {

			Map<String, Serializable> values =
				launchEntryObjectEntry.getValues();

			String className = MapUtil.getString(values, "className");

			LaunchEntryManager launchEntryManager =
				_launchEntryManagerRegistry.getLaunchEntryManager(className);

			if (launchEntryManager == null) {
				return false;
			}

			long classPK = MapUtil.getLong(values, "classPK");

			if (launchEntryManager.fetchItem(classPK) == null) {
				continue;
			}

			if (!_contains(permissionChecker, className, classPK)) {
				return false;
			}
		}

		return true;
	}

	private boolean _contains(
			PermissionChecker permissionChecker, String className, long classPK)
		throws PortalException {

		ModelResourcePermission<?> modelResourcePermission =
			ModelResourcePermissionRegistryUtil.getModelResourcePermission(
				className);

		if (modelResourcePermission == null) {
			return false;
		}

		return modelResourcePermission.contains(
			permissionChecker, classPK, ActionKeys.VIEW);
	}

	@Reference
	private LaunchEntryManagerRegistry _launchEntryManagerRegistry;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

}