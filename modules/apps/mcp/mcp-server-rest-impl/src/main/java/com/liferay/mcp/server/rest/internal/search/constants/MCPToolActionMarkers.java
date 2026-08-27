/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.constants;

import com.liferay.portal.kernel.util.LinkedHashMapBuilder;

import java.util.Map;

/**
 * @author Petteri Karttunen
 */
public class MCPToolActionMarkers {

	public static final Map<String, String[]> verbs = LinkedHashMapBuilder.put(
		"TranslationLanguage", new String[] {"translate", "localize"}
	).put(
		"CopyReplace", new String[] {"copy", "duplicate", "clone"}
	).put(
		"MoveReplace", new String[] {"move", "relocate", "transfer"}
	).put(
		"Unsubscribe", new String[] {"unsubscribe", "unfollow", "unwatch"}
	).put(
		"Translation", new String[] {"translate", "localize"}
	).put(
		"Subscribe", new String[] {"subscribe", "follow", "watch"}
	).put(
		"Undeploy", new String[] {"undeploy", "deactivate"}
	).put(
		"Validate", new String[] {"validate", "check", "verify"}
	).put(
		"Restore", new String[] {"restore", "recover", "undelete"}
	).put(
		"Publish", new String[] {"publish", "activate", "make live"}
	).put(
		"Expire", new String[] {"expire", "unpublish", "retire"}
	).put(
		"Deploy", new String[] {"deploy", "activate"}
	).put(
		"Copy", new String[] {"copy", "duplicate", "clone"}
	).put(
		"Move", new String[] {"move", "relocate", "transfer"}
	).build();

}