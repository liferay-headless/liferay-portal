/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index.util;

import com.liferay.petra.string.CharPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.Objects;

/**
 * Resolves operations to list the things a path segment names, like {siteId}.
 *
 * @author Petteri Karttunen
 */
public class ResolverUtil {

	public static String getPathParameter(String segment) {
		if ((segment.length() > 2) &&
			(segment.charAt(0) == CharPool.OPEN_CURLY_BRACE) &&
			(segment.charAt(segment.length() - 1) ==
				CharPool.CLOSE_CURLY_BRACE)) {

			return segment.substring(1, segment.length() - 1);
		}

		return null;
	}

	public static String getResolverSegment(MCPTool mcpTool) {
		if (Validator.isNotNull(mcpTool.getModifier()) ||
			!Objects.equals(mcpTool.getMethod(), "get") ||
			!StringUtil.endsWith(mcpTool.getToolName(), "Page")) {

			return null;
		}

		String[] segments = StringUtil.split(mcpTool.getPath(), CharPool.SLASH);

		if (segments.length == 0) {
			return null;
		}

		String segment = segments[segments.length - 1];

		if (getPathParameter(segment) != null) {
			return null;
		}

		return segment;
	}

}