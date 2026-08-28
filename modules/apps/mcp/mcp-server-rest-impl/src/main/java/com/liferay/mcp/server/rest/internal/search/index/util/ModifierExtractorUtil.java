/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index.util;

import com.liferay.mcp.server.rest.internal.search.constants.MCPToolModifiers;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Petteri Karttunen
 */
public class ModifierExtractorUtil {

	public static String getModifier(String entityName, String path) {
		String modifier = _getModifier(path);

		if (Validator.isNotNull(modifier)) {
			return modifier;
		}

		if (_isTraversal(entityName, path)) {
			return MCPToolModifiers.MODIFIER_TRAVERSAL;
		}

		return StringPool.BLANK;
	}

	private static String _getModifier(String path) {
		String[] segments = StringUtil.split(path, CharPool.SLASH);

		for (int i = segments.length - 1; i >= 0; i--) {
			String segment = StringUtil.toLowerCase(segments[i]);

			if (Validator.isNull(segment) || segment.startsWith("{")) {
				continue;
			}

			int index = segment.indexOf(CharPool.PERIOD);

			if (index > 0) {
				segment = segment.substring(0, index);
			}

			String modifier = MCPToolModifiers.pathSegmentModifiers.get(
				StringUtil.removeLast(segment, "-replace"));

			if (modifier != null) {
				return modifier;
			}
		}

		int count = 0;

		for (String segment : segments) {
			if (StringUtil.startsWith(
					StringUtil.toLowerCase(segment), "by-external")) {

				count++;
			}
		}

		if (count > 1) {
			return MCPToolModifiers.MODIFIER_NESTED;
		}

		return StringPool.BLANK;
	}

	private static boolean _isEntitySegment(String entityName, String segment) {
		String entity = WordUtil.toComparable(entityName);
		String comparableSegment = WordUtil.toComparable(segment);

		if (comparableSegment.equals(entity) ||
			comparableSegment.equals(entity + "s") ||
			comparableSegment.equals(entity + "es")) {

			return true;
		}

		if (entity.endsWith("y")) {
			return comparableSegment.equals(
				entity.substring(0, entity.length() - 1) + "ies");
		}

		return false;
	}

	private static boolean _isTraversal(String entityName, String path) {
		if (Validator.isNull(entityName)) {
			return false;
		}

		List<String> segments = new ArrayList<>();
		boolean parameter = false;

		for (String segment : StringUtil.split(path, CharPool.SLASH)) {
			if (Validator.isNull(segment)) {
				continue;
			}

			if (segment.charAt(0) == CharPool.OPEN_CURLY_BRACE) {
				parameter = true;

				continue;
			}

			if (parameter) {
				segments.add(segment);
			}
		}

		if (segments.isEmpty()) {
			return false;
		}

		String segment = segments.get(segments.size() - 1);

		if (_isEntitySegment(entityName, segment)) {
			return false;
		}

		if (WordUtil.isPlural(segment)) {
			return true;
		}

		if (segments.size() < 2) {
			return false;
		}

		segment = segments.get(segments.size() - 2);

		if (WordUtil.isPlural(segment) &&
			!_isEntitySegment(entityName, segment)) {

			return true;
		}

		return false;
	}

}