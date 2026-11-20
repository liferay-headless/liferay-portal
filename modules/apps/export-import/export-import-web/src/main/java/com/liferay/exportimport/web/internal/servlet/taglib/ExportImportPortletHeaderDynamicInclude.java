/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.web.internal.servlet.taglib;

import com.liferay.exportimport.constants.ExportImportPortletKeys;
import com.liferay.portal.kernel.servlet.taglib.BaseDynamicInclude;
import com.liferay.portal.kernel.servlet.taglib.DynamicInclude;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import org.osgi.service.component.annotations.Component;

/**
 * @author Jaime León Rosado
 */
@Component(
	property = "dynamic.include.key=portlet_header_" + ExportImportPortletKeys.EXPORT_IMPORT,
	service = DynamicInclude.class
)
public class ExportImportPortletHeaderDynamicInclude
	extends BaseDynamicInclude {

	@Override
	public void include(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, String key)
		throws IOException {

		PrintWriter printWriter = httpServletResponse.getWriter();

		printWriter.print("<div class=\"export-import-header-actions\"></div>");
	}

	@Override
	public void register(DynamicIncludeRegistry dynamicIncludeRegistry) {
	}

}