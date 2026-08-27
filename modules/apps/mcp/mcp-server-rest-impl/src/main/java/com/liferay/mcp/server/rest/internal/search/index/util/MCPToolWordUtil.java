/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index.util;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

/**
 * Turns the words of REST Builder's generated operation into the words a
 * caller would use.
 *
 * @author Petteri Karttunen
 */
public class MCPToolWordUtil {

	public static String humanize(String value) {
		if (Validator.isNull(value)) {
			return StringPool.BLANK;
		}

		value = value.replaceAll("([a-z0-9])([A-Z])", "$1 $2");

		return value.replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2");
	}

	public static String toComparable(String value) {
		return StringUtil.toLowerCase(value.replaceAll("[^A-Za-z0-9]", ""));
	}

	public static String toPlural(String word) {
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
		if (word.endsWith("ies")) {
			return word.substring(0, word.length() - 3) + "y";
		}

		if (word.endsWith("ches") || word.endsWith("shes") ||
			word.endsWith("xes") || word.endsWith("zes")) {

			return word.substring(0, word.length() - 2);
		}

		if (word.endsWith("s")) {
			return word.substring(0, word.length() - 1);
		}

		return word;
	}

}