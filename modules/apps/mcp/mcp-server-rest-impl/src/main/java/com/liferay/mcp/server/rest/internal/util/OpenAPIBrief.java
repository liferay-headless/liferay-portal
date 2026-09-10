/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.util;

/**
 * @author Petteri Karttunen
 */
public class OpenAPIBrief {

	public OpenAPIBrief(
		String basePath, String description, String openAPIPath) {

		_basePath = basePath;
		_description = description;
		_openAPIPath = openAPIPath;
	}

	public String getBasePath() {
		return _basePath;
	}

	public String getDescription() {
		return _description;
	}

	public String getOpenAPIPath() {
		return _openAPIPath;
	}

	private final String _basePath;
	private final String _description;
	private final String _openAPIPath;

}