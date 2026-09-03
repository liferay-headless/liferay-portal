/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.staticsite;

import com.liferay.exportimport.kernel.lar.PortletDataContext;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Serializes the layouts selected by an export process as a tree of static
 * HTML files and their assets, written into the export's own zip archive.
 *
 * <p>
 * Implementations reuse the page selection resolved by the export process. They
 * do not reuse the staged model data handlers, which serialize models rather
 * than rendered output.
 * </p>
 *
 * @author Alejandro Tardín
 */
@ProviderType
public interface StaticSiteExporter {

	public void export(PortletDataContext portletDataContext) throws Exception;

}