/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.internal.entry.type.util;

import com.liferay.layout.content.model.LayoutContentVersion;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.Layout;

/**
 * Names the layout a page version is materialized into so that it is preview
 * only. The name carries the launch infix, which keeps the portal from
 * resolving it as the page's own draft, and the version it holds, which makes
 * it the cache key a second preview of the same version finds.
 *
 * @author Alejandro Tardín
 */
public class LaunchPreviewLayoutUtil {

	public static final String EXTERNAL_REFERENCE_CODE_INFIX =
		"-launch-preview-";

	public static String getExternalReferenceCode(
		Layout layout, LayoutContentVersion layoutContentVersion) {

		return StringBundler.concat(
			layout.getExternalReferenceCode(), EXTERNAL_REFERENCE_CODE_INFIX,
			layoutContentVersion.getLayoutContentVersionId());
	}

	public static boolean isPreviewLayout(Layout layout) {
		String externalReferenceCode = layout.getExternalReferenceCode();

		return externalReferenceCode.contains(EXTERNAL_REFERENCE_CODE_INFIX);
	}

}