/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index.util;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.TextFormatter;
import com.liferay.portal.kernel.util.Validator;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Petteri Karttunen
 */
public class WordUtil {

	public static boolean isPlural(String word) {
		String normalizedWord = normalize(word);

		if (normalizedWord.endsWith("ss") ||
			_singularWordsEndingInS.contains(_getLastWord(word))) {

			return false;
		}

		return normalizedWord.endsWith("s");
	}

	public static String normalize(String value) {
		Matcher matcher = _specialCharacterPattern.matcher(value);

		return StringUtil.toLowerCase(matcher.replaceAll(StringPool.BLANK));
	}

	public static String toPlural(String word) {
		if (Validator.isBlank(word)) {
			return StringPool.BLANK;
		}

		if (_singularWordsEndingInS.contains(normalize(word))) {
			return word;
		}

		if (word.endsWith("ss")) {
			return word + "es";
		}

		if (word.endsWith("s")) {
			return word;
		}

		if (word.endsWith("ch") || word.endsWith("sh") || word.endsWith("x") ||
			word.endsWith("z")) {

			return word + "es";
		}

		int length = word.length();

		if ((length > 1) && word.endsWith("y") &&
			("aeiou".indexOf(word.charAt(length - 2)) == -1)) {

			return word.substring(0, length - 1) + "ies";
		}

		return word + "s";
	}

	public static String toSingular(String word) {
		if (Validator.isBlank(word)) {
			return StringPool.BLANK;
		}

		if (_singularWordsEndingInS.contains(normalize(word))) {
			return word;
		}

		if (word.endsWith("ies")) {
			return word.substring(0, word.length() - 3) + "y";
		}

		if (word.endsWith("ches") || word.endsWith("shes") ||
			word.endsWith("sses") || word.endsWith("xes") ||
			word.endsWith("zes")) {

			return word.substring(0, word.length() - 2);
		}

		if (word.endsWith("s")) {
			return word.substring(0, word.length() - 1);
		}

		return word;
	}

	public static String toWords(String value) {
		if (Validator.isBlank(value)) {
			return StringPool.BLANK;
		}

		return TextFormatter.format(value, TextFormatter.H);
	}

	private static String _getLastWord(String word) {
		String[] words = StringUtil.split(word, CharPool.DASH);

		if (words.length == 0) {
			return StringPool.BLANK;
		}

		return normalize(words[words.length - 1]);
	}

	private static final Set<String> _singularWordsEndingInS =
		SetUtil.fromArray(
			"analysis", "analytics", "cms", "metrics", "news", "series",
			"status");
	private static final Pattern _specialCharacterPattern = Pattern.compile(
		"[^A-Za-z0-9]");

}