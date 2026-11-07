/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.set.permission;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author Jose Luis Navarro
 */
@ProviderType
public interface LaunchSetPreviewPermission {

	public boolean contains(
			PermissionChecker permissionChecker, long launchSetId)
		throws PortalException;

}