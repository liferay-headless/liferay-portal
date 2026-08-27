/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index.util;

import com.liferay.mcp.server.rest.internal.search.constants.MCPToolActionMarkers;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LinkedHashMapBuilder;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates phrases a tool is found by.
 *
 * @author Petteri Karttunen
 */
public class MCPToolExpansionUtil {

	public static String[] getExpansion(
		boolean batch, String marker, String method, String path, String tags,
		String toolName) {

		String entity = StringUtil.toLowerCase(tags.trim());

		if (Validator.isNull(entity) || Objects.equals(method, "head") ||
			Objects.equals(method, "options")) {

			return new String[0];
		}

		boolean collection = toolName.endsWith("Page");

		if (marker != null) {
			return _getActionExpansion(entity, marker, path, toolName);
		}

		String[] verbs = _VERBS_DELETE;

		if (Objects.equals(method, "get")) {
			verbs = collection ? _VERBS_COLLECTION : _VERBS_SINGLE;
		}
		else if (Objects.equals(method, "patch")) {
			verbs = _VERBS_UPDATE;
		}
		else if (Objects.equals(method, "post")) {
			verbs = _VERBS_CREATE;
		}
		else if (Objects.equals(method, "put")) {
			verbs = _VERBS_REPLACE;
		}

		String remainder = toolName.replaceFirst(
			"^(delete|get|head|options|patch|post|put)", StringPool.BLANK);

		String scopePhrase = StringPool.BLANK;

		for (Map.Entry<String, String> entry : _entityScopePhrases.entrySet()) {
			String prefix = entry.getKey();

			if (!remainder.startsWith(prefix)) {
				continue;
			}

			String rest = remainder.substring(prefix.length());

			if (rest.isEmpty() || !Character.isUpperCase(rest.charAt(0))) {
				continue;
			}

			remainder = rest;
			scopePhrase = entry.getValue();

			break;
		}

		if (batch) {
			remainder = StringUtil.removeLast(remainder, "Batch");
		}

		NounPhrase nounPhrase = _getNounPhrase(entity, remainder);

		String head = nounPhrase._head;
		String prefix = nounPhrase._prefix;
		String suffix = nounPhrase._suffix;

		if (batch || collection) {
			head = MCPToolWordUtil.toPlural(head);
		}

		String parameterSuffix = _getParameterSuffix(
			path, StringBundler.concat(prefix, " ", head, " ", suffix),
			scopePhrase);

		verbs = _getApplicableVerbs(entity, verbs);

		List<String> expansions = new ArrayList<>(verbs.length);

		for (String verb : verbs) {
			StringBundler sb = new StringBundler(12);

			if (batch) {
				sb.append("batch ");
			}

			sb.append(verb);
			sb.append(StringPool.SPACE);

			if (Validator.isNotNull(prefix)) {
				sb.append(prefix);
				sb.append(StringPool.SPACE);
			}

			sb.append(head);

			if (Validator.isNotNull(scopePhrase)) {
				sb.append(StringPool.SPACE);
				sb.append(scopePhrase);
			}

			if (Validator.isNotNull(suffix)) {
				sb.append(StringPool.SPACE);
				sb.append(suffix);
			}

			if (Validator.isNotNull(parameterSuffix)) {
				sb.append(StringPool.SPACE);
				sb.append(parameterSuffix);
			}

			expansions.add(sb.toString());
		}

		return expansions.toArray(new String[0]);
	}

	private static String[] _getActionExpansion(
		String entity, String marker, String path, String toolName) {

		String remainder = StringUtil.removeLast(
			toolName.replaceFirst(
				"^(delete|get|head|options|patch|post|put)", StringPool.BLANK),
			marker);

		NounPhrase nounPhrase = _getNounPhrase(entity, remainder);

		String head = nounPhrase._head;
		String prefix = nounPhrase._prefix;
		String suffix = nounPhrase._suffix;

		String parameterSuffix = _getParameterSuffix(
			path, StringBundler.concat(prefix, " ", head, " ", suffix),
			StringPool.BLANK);

		List<String> expansions = new ArrayList<>();

		for (String verb : MCPToolActionMarkers.verbs.get(marker)) {
			StringBundler sb = new StringBundler(9);

			sb.append(verb);
			sb.append(StringPool.SPACE);

			if (Validator.isNotNull(prefix)) {
				sb.append(prefix);
				sb.append(StringPool.SPACE);
			}

			sb.append(head);

			if (Validator.isNotNull(suffix)) {
				sb.append(StringPool.SPACE);
				sb.append(suffix);
			}

			if (Validator.isNotNull(parameterSuffix)) {
				sb.append(StringPool.SPACE);
				sb.append(parameterSuffix);
			}

			expansions.add(sb.toString());
		}

		return expansions.toArray(new String[0]);
	}

	private static String[] _getApplicableVerbs(String entity, String[] verbs) {
		List<String> applicableVerbs = new ArrayList<>(verbs.length);

		for (String verb : verbs) {
			String[] targetEntities = _verbTargetEntities.get(verb);

			if (targetEntities == null) {
				applicableVerbs.add(verb);

				continue;
			}

			for (String targetEntity : targetEntities) {
				if (entity.contains(targetEntity)) {
					applicableVerbs.add(verb);

					break;
				}
			}
		}

		return applicableVerbs.toArray(new String[0]);
	}

	private static NounPhrase _getNounPhrase(String entity, String remainder) {
		String humanizedRemainder = StringUtil.toLowerCase(
			MCPToolWordUtil.humanize(StringUtil.removeLast(remainder, "Page")));

		Set<String> entityWords = new HashSet<>();

		for (String word : StringUtil.split(entity, CharPool.SPACE)) {
			entityWords.add(MCPToolWordUtil.toSingular(word));
		}

		StringBundler afterSB = new StringBundler();
		StringBundler beforeSB = new StringBundler();
		boolean seen = false;

		for (String word :
				StringUtil.split(humanizedRemainder, CharPool.SPACE)) {

			if (entityWords.contains(MCPToolWordUtil.toSingular(word))) {
				seen = true;

				continue;
			}

			if (!seen) {
				beforeSB.append(word);
				beforeSB.append(StringPool.SPACE);
			}
			else if (!_possessives.contains(word)) {
				afterSB.append(word);
				afterSB.append(StringPool.SPACE);
			}
		}

		String before = StringUtil.trim(beforeSB.toString());

		if (StringUtil.startsWith(before, "by ")) {
			return new NounPhrase(entity, StringPool.BLANK, before);
		}

		String after = StringUtil.trim(afterSB.toString());

		if (Validator.isNull(after)) {
			return new NounPhrase(entity, before, StringPool.BLANK);
		}

		if (StringUtil.startsWith(after, "by ")) {
			return new NounPhrase(entity, before, after);
		}

		return new NounPhrase(
			after, StringUtil.trim(before + StringPool.SPACE + entity),
			StringPool.BLANK);
	}

	private static String _getParameterSuffix(
		String path, String qualifier, String scope) {

		Matcher matcher = _pathParameterPattern.matcher(path);

		String parameter = null;

		while (matcher.find()) {
			parameter = matcher.group(1);
		}

		if (parameter == null) {
			return StringPool.BLANK;
		}

		String words = StringUtil.toLowerCase(
			MCPToolWordUtil.humanize(parameter));

		if (qualifier.contains(words)) {
			return StringPool.BLANK;
		}

		for (String word : StringUtil.split(words, CharPool.SPACE)) {
			if (scope.contains(word)) {
				return StringPool.BLANK;
			}
		}

		return "by " + words;
	}

	private static final String[] _VERBS_COLLECTION = {
		"list", "show", "browse", "find", "get all", "see"
	};

	private static final String[] _VERBS_CREATE = {
		"create", "add", "make", "start", "upload", "write"
	};

	private static final String[] _VERBS_DELETE = {
		"delete", "remove", "get rid of"
	};

	private static final String[] _VERBS_REPLACE = {
		"replace", "set", "overwrite"
	};

	private static final String[] _VERBS_SINGLE = {
		"get", "view", "open", "read", "fetch", "look up"
	};

	private static final String[] _VERBS_UPDATE = {
		"update", "edit", "change", "rename", "modify"
	};

	private static final Map<String, String> _entityScopePhrases =
		LinkedHashMapBuilder.put(
			"AssetLibrary", "in an asset library"
		).put(
			"DocumentFolder", "in a folder"
		).put(
			"KnowledgeBaseFolder", "in a folder"
		).put(
			"MessageBoardSection", "in a section"
		).put(
			"MessageBoardThread", "in a thread"
		).put(
			"ObjectDefinition", "on a custom object"
		).put(
			"StructuredContentFolder", "in a folder"
		).put(
			"TaxonomyVocabulary", "in a vocabulary"
		).put(
			"WikiNode", "in a wiki"
		).put(
			"Organization", "in an organization"
		).put(
			"Account", "on an account"
		).put(
			"Site", "in a site"
		).build();
	private static final Pattern _pathParameterPattern = Pattern.compile(
		"\\{([^}]+)\\}");
	private static final Set<String> _possessives = SetUtil.fromArray(
		"me", "my");
	private static final Map<String, String[]> _verbTargetEntities =
		HashMapBuilder.put(
			"start",
			new String[] {
				"conversation", "discussion", "instance", "process", "task",
				"thread"
			}
		).put(
			"submit", new String[] {"form", "request", "task", "workflow"}
		).put(
			"upload",
			new String[] {
				"attachment", "document", "file", "image", "logo", "media",
				"picture", "thumbnail", "video"
			}
		).put(
			"write",
			new String[] {
				"article", "comment", "message", "note", "post", "text"
			}
		).build();

	private static class NounPhrase {

		private NounPhrase(String head, String prefix, String suffix) {
			_head = head;
			_prefix = prefix;
			_suffix = suffix;
		}

		private final String _head;
		private final String _prefix;
		private final String _suffix;

	}

}