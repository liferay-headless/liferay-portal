/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine;

import com.liferay.exportimport.kernel.lar.PortletDataContext;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author Vendel Toreki
 */
@ProviderType
public interface BatchEngineAttachmentHelper {

	public void exportAttachments(
			String portletId, PortletDataContext portletDataContext)
		throws Exception;

	public void importAttachments(
			String portletId, PortletDataContext portletDataContext)
		throws Exception;

}