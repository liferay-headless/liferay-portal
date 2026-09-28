/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.content.restorer;

import com.liferay.layout.content.model.LayoutContentVersion;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.ServiceContext;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author Alejandro Tardín
 */
@ProviderType
public interface LayoutContentVersionRestorer {

	public Layout restoreLayoutContentVersion(
			Layout draftLayout, LayoutContentVersion layoutContentVersion,
			ServiceContext serviceContext)
		throws Exception;

}