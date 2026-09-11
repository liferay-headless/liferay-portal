/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.constants;

import com.liferay.portal.kernel.util.HashMapBuilder;

import java.util.Map;

/**
 * @author Petteri Karttunen
 */
public class MCPToolModifiers {

	public static final String MODIFIER_APPROVED = "approved";

	public static final String MODIFIER_BATCH = "batch";

	public static final String MODIFIER_COPY = "copy";

	public static final String MODIFIER_EXPIRE = "expire";

	public static final String MODIFIER_HISTORY = "history";

	public static final String MODIFIER_KEYED = "keyed";

	public static final String MODIFIER_MOVE = "move";

	public static final String MODIFIER_NESTED = "nested";

	public static final String MODIFIER_OPENAPI = "openapi";

	public static final String MODIFIER_PERMISSIONS = "permissions";

	public static final String MODIFIER_PREVIEW = "preview";

	public static final String MODIFIER_RATING = "rating";

	public static final String MODIFIER_RESTORE = "restore";

	public static final String MODIFIER_SUBSCRIPTION = "subscription";

	public static final String MODIFIER_TRANSLATION = "translation";

	public static final String MODIFIER_TRAVERSAL = "traversal";

	public static final String MODIFIER_VALIDATE = "validate";

	public static final String[] RARELY_WANTED_MODIFIERS = {
		MODIFIER_HISTORY, MODIFIER_KEYED, MODIFIER_NESTED, MODIFIER_OPENAPI,
		MODIFIER_PERMISSIONS, MODIFIER_PREVIEW, MODIFIER_RATING,
		MODIFIER_SUBSCRIPTION
	};

	public static final String[] RESHAPING_MODIFIERS = {
		MODIFIER_APPROVED, MODIFIER_COPY, MODIFIER_EXPIRE, MODIFIER_MOVE,
		MODIFIER_RESTORE, MODIFIER_TRANSLATION, MODIFIER_VALIDATE
	};

	public static final Map<String, String> pathSegmentModifiers =
		HashMapBuilder.put(
			"approved", MODIFIER_APPROVED
		).put(
			"batch", MODIFIER_BATCH
		).put(
			"by-key", MODIFIER_KEYED
		).put(
			"by-uuid", MODIFIER_KEYED
		).put(
			"copy", MODIFIER_COPY
		).put(
			"expire", MODIFIER_EXPIRE
		).put(
			"export-batch", MODIFIER_BATCH
		).put(
			"export-preview", MODIFIER_PREVIEW
		).put(
			"friendly-url-history", MODIFIER_HISTORY
		).put(
			"import-preview", MODIFIER_PREVIEW
		).put(
			"move", MODIFIER_MOVE
		).put(
			"my-rating", MODIFIER_RATING
		).put(
			"openapi", MODIFIER_OPENAPI
		).put(
			"permissions", MODIFIER_PERMISSIONS
		).put(
			"preview", MODIFIER_PREVIEW
		).put(
			"rated-by-me", MODIFIER_RATING
		).put(
			"restore", MODIFIER_RESTORE
		).put(
			"subscribe", MODIFIER_SUBSCRIPTION
		).put(
			"translation", MODIFIER_TRANSLATION
		).put(
			"translations", MODIFIER_TRANSLATION
		).put(
			"unsubscribe", MODIFIER_SUBSCRIPTION
		).put(
			"validate", MODIFIER_VALIDATE
		).build();

}