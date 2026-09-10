/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index.util;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Petteri Karttunen
 */
public class SearchPhraseUtil {

	public static String getHeadNoun(String search) {
		if (isAssociating(search)) {
			return null;
		}

		String boundary = null;
		String headNoun = null;
		String trailingNoun = null;

		for (String word :
				StringUtil.split(
					StringUtil.toLowerCase(search), CharPool.SPACE)) {

			if (_headNounBoundaries.contains(word)) {
				if (boundary == null) {
					boundary = word;
				}

				continue;
			}

			if (_genericNouns.contains(word)) {
				continue;
			}

			if (boundary == null) {
				headNoun = word;
			}
			else if (Objects.equals(boundary, "of")) {
				trailingNoun = word;
			}
		}

		if (Objects.equals(boundary, "of") && (trailingNoun != null)) {
			return trailingNoun;
		}

		return headNoun;
	}

	public static boolean isAssociating(String search) {
		String[] words = StringUtil.split(
			StringUtil.toLowerCase(search), CharPool.SPACE);

		Set<String> directionWords = new HashSet<>();

		for (String word : words) {
			if (_associationWords.contains(word)) {
				return true;
			}

			String directionWord = _ambiguousAssociationWords.get(word);

			if (directionWord != null) {
				directionWords.add(directionWord);
			}
		}

		if (directionWords.isEmpty()) {
			return false;
		}

		for (String word : words) {
			if (directionWords.contains(word)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isBatch(String search) {
		for (String word :
				StringUtil.split(
					StringUtil.toLowerCase(search), CharPool.SPACE)) {

			if (_bulkWords.contains(word) ||
				(Validator.isNumber(word) &&
				 (GetterUtil.getInteger(word) >= _BULK_THRESHOLD))) {

				return true;
			}
		}

		return false;
	}

	public static String replaceEntityWords(
		List<String> intents, String search) {

		Matcher matcher = _entityWordPattern.matcher(search);

		if (!matcher.find()) {
			return search;
		}

		String entityReplacement =
			_ENTITY_DEFINITION + StringPool.SPACE + _ENTITY_ENTRY;

		if (intents != null) {
			boolean definitionIntent = intents.contains(
				IntentExtractorUtil.INTENT_CREATE);

			boolean entryIntent = false;

			if (intents.contains(IntentExtractorUtil.INTENT_LIST) ||
				intents.contains(IntentExtractorUtil.INTENT_READ)) {

				entryIntent = true;
			}

			if (definitionIntent && !entryIntent) {
				entityReplacement = _ENTITY_DEFINITION;
			}
			else if (entryIntent && !definitionIntent) {
				entityReplacement = _ENTITY_ENTRY;
			}
		}

		return matcher.replaceAll(Matcher.quoteReplacement(entityReplacement));
	}

	private static final int _BULK_THRESHOLD = 10;

	private static final String _ENTITY_DEFINITION = "object definition";

	private static final String _ENTITY_ENTRY = "object entry";

	private static final Map<String, String> _ambiguousAssociationWords =
		HashMapBuilder.put(
			"add", "to"
		).put(
			"added", "to"
		).put(
			"adds", "to"
		).put(
			"put", "to"
		).put(
			"remove", "from"
		).put(
			"removed", "from"
		).put(
			"removes", "from"
		).build();
	private static final Set<String> _associationWords = SetUtil.fromArray(
		"assign", "assigned", "assigning", "associate", "associated",
		"association", "attach", "attached", "detach", "disassociate", "link",
		"linked", "relate", "related", "unassign", "unlink");
	private static final Set<String> _bulkWords = SetUtil.fromArray(
		"batch", "batches", "bulk", "dozen", "hundred", "many", "ten",
		"thousand", "twelve", "twenty");
	private static final Pattern _entityWordPattern = Pattern.compile(
		"\\b(entity|entities)\\b", Pattern.CASE_INSENSITIVE);
	private static final Set<String> _genericNouns = SetUtil.fromArray(
		"data", "detail", "details", "entries", "entry", "info", "information",
		"item", "items", "object", "objects", "record", "records", "row",
		"rows", "value", "values");
	private static final Set<String> _headNounBoundaries = SetUtil.fromArray(
		"and", "as", "at", "belonging", "by", "for", "from", "in", "inside",
		"into", "of", "on", "onto", "that", "to", "under", "using", "via",
		"whose", "with", "within");

}