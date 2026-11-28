/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.web.internal.display.context;

import com.liferay.launch.display.context.AddToLaunchPropsProvider;
import com.liferay.launch.entry.manager.LaunchEntryManagerRegistry;
import com.liferay.object.constants.ObjectActionKeys;
import com.liferay.object.definition.security.permission.resource.ObjectDefinitionPortletResourcePermissionRegistryUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermission;
import com.liferay.portal.kernel.util.HashMapBuilder;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jorge González
 */
@Component(service = AddToLaunchPropsProvider.class)
public class AddToLaunchPropsProviderImpl implements AddToLaunchPropsProvider {

	@Override
	public Map<String, Object> getProps(
		String className, long classPK, String classVersion) {

		return HashMapBuilder.<String, Object>put(
			"className", className
		).put(
			"classPK", classPK
		).put(
			"classVersion", classVersion
		).build();
	}

	@Override
	public boolean isEnabled(
		String className, PermissionChecker permissionChecker) {

		if (!FeatureFlagManagerUtil.isEnabled(
				permissionChecker.getCompanyId(), "LPD-72278") ||
			(_launchEntryManagerRegistry.getLaunchEntryManager(className) ==
				null)) {

			return false;
		}

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_LAUNCH_ENTRY", permissionChecker.getCompanyId());

		if (objectDefinition == null) {
			return false;
		}

		PortletResourcePermission portletResourcePermission =
			ObjectDefinitionPortletResourcePermissionRegistryUtil.getService(
				objectDefinition.getResourceName());

		return portletResourcePermission.contains(
			permissionChecker, 0, ObjectActionKeys.ADD_OBJECT_ENTRY);
	}

	@Reference
	private LaunchEntryManagerRegistry _launchEntryManagerRegistry;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

}