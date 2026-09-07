/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index.util;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Extracts what an operation does, and what a search is asking for, using a
 * controlled vocabulary.
 *
 * @author Petteri Karttunen
 */
public class IntentExtractorUtil {

	public static final String INTENT_CREATE = "create";

	public static final String INTENT_LIST = "list";

	public static final String INTENT_READ = "read";

	public static String getIntent(
		String marker, String method, String toolName) {

		if (marker != null) {
			return _actionMarkerIntents.get(marker);
		}

		if (Objects.equals(method, "get")) {
			if (toolName.endsWith("Page")) {
				return INTENT_LIST;
			}

			return INTENT_READ;
		}

		return _methodIntents.getOrDefault(method, StringPool.BLANK);
	}

	public static List<String> getIntents(String search) {
		String[] words = StringUtil.split(
			StringUtil.toLowerCase(search), CharPool.SPACE);

		if (words.length == 0) {
			return null;
		}

		if (words.length > 1) {
			List<String> intents = _phraseIntents.get(
				words[0] + StringPool.SPACE + words[1]);

			if (intents != null) {
				return intents;
			}
		}

		for (String word : words) {
			List<String> intents = _wordIntents.get(word);

			if (intents != null) {
				return intents;
			}
		}

		return null;
	}

	public static List<String> getOtherIntents(List<String> intents) {
		List<String> otherIntents = new ArrayList<>();

		for (String intent : _getAllIntents()) {
			if (!intents.contains(intent)) {
				otherIntents.add(intent);
			}
		}

		return otherIntents;
	}

	private static List<String> _getAllIntents() {
		return Arrays.asList(
			_INTENT_COPY, INTENT_CREATE, _INTENT_DELETE, _INTENT_DEPLOY,
			INTENT_LIST, _INTENT_MOVE, _INTENT_PUBLISH, INTENT_READ,
			_INTENT_REPLACE, _INTENT_RESTORE, _INTENT_SUBSCRIBE,
			_INTENT_TRANSLATE, _INTENT_UNDEPLOY, _INTENT_UNPUBLISH,
			_INTENT_UNSUBSCRIBE, _INTENT_UPDATE, _INTENT_VALIDATE);
	}

	private static final String _INTENT_COPY = "copy";

	private static final String _INTENT_DELETE = "delete";

	private static final String _INTENT_DEPLOY = "deploy";

	private static final String _INTENT_MOVE = "move";

	private static final String _INTENT_PUBLISH = "publish";

	private static final String _INTENT_REPLACE = "replace";

	private static final String _INTENT_RESTORE = "restore";

	private static final String _INTENT_SUBSCRIBE = "subscribe";

	private static final String _INTENT_TRANSLATE = "translate";

	private static final String _INTENT_UNDEPLOY = "undeploy";

	private static final String _INTENT_UNPUBLISH = "unpublish";

	private static final String _INTENT_UNSUBSCRIBE = "unsubscribe";

	private static final String _INTENT_UPDATE = "update";

	private static final String _INTENT_VALIDATE = "validate";

	private static final Map<String, String> _actionMarkerIntents =
		HashMapBuilder.put(
			"Copy", _INTENT_COPY
		).put(
			"CopyReplace", _INTENT_COPY
		).put(
			"Deploy", _INTENT_DEPLOY
		).put(
			"Expire", _INTENT_UNPUBLISH
		).put(
			"Move", _INTENT_MOVE
		).put(
			"MoveReplace", _INTENT_MOVE
		).put(
			"Publish", _INTENT_PUBLISH
		).put(
			"Restore", _INTENT_RESTORE
		).put(
			"Subscribe", _INTENT_SUBSCRIBE
		).put(
			"Translation", _INTENT_TRANSLATE
		).put(
			"TranslationLanguage", _INTENT_TRANSLATE
		).put(
			"Undeploy", _INTENT_UNDEPLOY
		).put(
			"Unsubscribe", _INTENT_UNSUBSCRIBE
		).put(
			"Validate", _INTENT_VALIDATE
		).build();
	private static final Map<String, String> _methodIntents =
		HashMapBuilder.put(
			"delete", _INTENT_DELETE
		).put(
			"patch", _INTENT_UPDATE
		).put(
			"post", INTENT_CREATE
		).put(
			"put", _INTENT_REPLACE
		).build();
	private static final Map<String, List<String>> _phraseIntents =
		HashMapBuilder.<String, List<String>>put(
			"get rid", Arrays.asList(_INTENT_DELETE)
		).put(
			"look up", Arrays.asList(INTENT_LIST, INTENT_READ)
		).put(
			"set up", Arrays.asList(INTENT_CREATE)
		).put(
			"sign up", Arrays.asList(INTENT_CREATE)
		).build();
	private static final Map<String, List<String>> _wordIntents =
		HashMapBuilder.<String, List<String>>put(
			"add", Arrays.asList(INTENT_CREATE)
		).put(
			"assign", Arrays.asList(INTENT_CREATE)
		).put(
			"associate", Arrays.asList(INTENT_CREATE)
		).put(
			"attach", Arrays.asList(INTENT_CREATE)
		).put(
			"browse", Arrays.asList(INTENT_LIST, INTENT_READ)
		).put(
			"change", Arrays.asList(_INTENT_UPDATE, _INTENT_REPLACE)
		).put(
			"check", Arrays.asList(INTENT_LIST, INTENT_READ)
		).put(
			"clone", Arrays.asList(_INTENT_COPY)
		).put(
			"copy", Arrays.asList(_INTENT_COPY)
		).put(
			"create", Arrays.asList(INTENT_CREATE)
		).put(
			"delete", Arrays.asList(_INTENT_DELETE)
		).put(
			"deploy", Arrays.asList(_INTENT_DEPLOY)
		).put(
			"destroy", Arrays.asList(_INTENT_DELETE)
		).put(
			"detach", Arrays.asList(_INTENT_DELETE)
		).put(
			"disassociate", Arrays.asList(_INTENT_DELETE)
		).put(
			"duplicate", Arrays.asList(_INTENT_COPY)
		).put(
			"edit", Arrays.asList(_INTENT_UPDATE, _INTENT_REPLACE)
		).put(
			"expire", Arrays.asList(_INTENT_UNPUBLISH)
		).put(
			"fetch", Arrays.asList(INTENT_LIST, INTENT_READ)
		).put(
			"find", Arrays.asList(INTENT_LIST, INTENT_READ)
		).put(
			"follow", Arrays.asList(_INTENT_SUBSCRIBE)
		).put(
			"get", Arrays.asList(INTENT_LIST, INTENT_READ)
		).put(
			"link", Arrays.asList(INTENT_CREATE)
		).put(
			"list", Arrays.asList(INTENT_LIST, INTENT_READ)
		).put(
			"localize", Arrays.asList(_INTENT_TRANSLATE)
		).put(
			"make", Arrays.asList(INTENT_CREATE)
		).put(
			"modify", Arrays.asList(_INTENT_UPDATE, _INTENT_REPLACE)
		).put(
			"move", Arrays.asList(_INTENT_MOVE)
		).put(
			"new", Arrays.asList(INTENT_CREATE)
		).put(
			"open", Arrays.asList(INTENT_LIST, INTENT_READ)
		).put(
			"overwrite", Arrays.asList(_INTENT_REPLACE)
		).put(
			"patch", Arrays.asList(_INTENT_UPDATE)
		).put(
			"place", Arrays.asList(INTENT_CREATE)
		).put(
			"post", Arrays.asList(INTENT_CREATE)
		).put(
			"publish", Arrays.asList(_INTENT_PUBLISH)
		).put(
			"put", Arrays.asList(_INTENT_REPLACE)
		).put(
			"read", Arrays.asList(INTENT_LIST, INTENT_READ)
		).put(
			"recover", Arrays.asList(_INTENT_RESTORE)
		).put(
			"relate", Arrays.asList(INTENT_CREATE)
		).put(
			"relocate", Arrays.asList(_INTENT_MOVE)
		).put(
			"remove", Arrays.asList(_INTENT_DELETE)
		).put(
			"rename", Arrays.asList(_INTENT_UPDATE, _INTENT_REPLACE)
		).put(
			"replace", Arrays.asList(_INTENT_REPLACE)
		).put(
			"restore", Arrays.asList(_INTENT_RESTORE)
		).put(
			"retrieve", Arrays.asList(INTENT_LIST, INTENT_READ)
		).put(
			"see", Arrays.asList(INTENT_LIST, INTENT_READ)
		).put(
			"set", Arrays.asList(_INTENT_UPDATE, _INTENT_REPLACE)
		).put(
			"show", Arrays.asList(INTENT_LIST, INTENT_READ)
		).put(
			"start", Arrays.asList(INTENT_CREATE)
		).put(
			"submit", Arrays.asList(INTENT_CREATE)
		).put(
			"subscribe", Arrays.asList(_INTENT_SUBSCRIBE)
		).put(
			"translate", Arrays.asList(_INTENT_TRANSLATE)
		).put(
			"unassign", Arrays.asList(_INTENT_DELETE)
		).put(
			"undeploy", Arrays.asList(_INTENT_UNDEPLOY)
		).put(
			"unfollow", Arrays.asList(_INTENT_UNSUBSCRIBE)
		).put(
			"unlink", Arrays.asList(_INTENT_DELETE)
		).put(
			"unpublish", Arrays.asList(_INTENT_UNPUBLISH)
		).put(
			"unsubscribe", Arrays.asList(_INTENT_UNSUBSCRIBE)
		).put(
			"update", Arrays.asList(_INTENT_UPDATE, _INTENT_REPLACE)
		).put(
			"upload", Arrays.asList(INTENT_CREATE)
		).put(
			"upsert", Arrays.asList(_INTENT_REPLACE)
		).put(
			"validate", Arrays.asList(_INTENT_VALIDATE)
		).put(
			"verify", Arrays.asList(_INTENT_VALIDATE)
		).put(
			"view", Arrays.asList(INTENT_LIST, INTENT_READ)
		).put(
			"write", Arrays.asList(INTENT_CREATE)
		).build();

}