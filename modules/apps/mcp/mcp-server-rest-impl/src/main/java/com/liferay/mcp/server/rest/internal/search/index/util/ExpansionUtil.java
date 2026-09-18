/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index.util;

import com.liferay.mcp.server.rest.internal.search.index.constants.MCPToolConstants;
import com.liferay.mcp.server.rest.internal.util.OpenAPIUtil;
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
 * Generates the natural-language phrases a person might use to ask for a tool.
 *
 * @author Petteri Karttunen
 */
public class ExpansionUtil {

	public static String[] getExpansions(
		String actionMarker, boolean batch, String entityWords, String method,
		String path, String toolName) {

		// Returns the phrases a person might use to ask for the tool.
		// "postSiteBlogPosting" gives "create blog posting in a site".

		entityWords = StringUtil.trim(entityWords);

		if (Validator.isNull(entityWords) || Objects.equals(method, "head") ||
			Objects.equals(method, "options")) {

			return new String[0];
		}

		if (actionMarker != null) {
			return _getActionExpansions(
				actionMarker, entityWords, path, toolName);
		}

		boolean collectionTool = toolName.endsWith("Page");

		String toolNameRemainder = _removeMethodPrefix(toolName);

		String scopePhrase = StringPool.BLANK;

		String scopeEntityName = _getScopeEntityName(
			entityWords, toolNameRemainder);

		if (scopeEntityName != null) {
			scopePhrase = _scopePhrases.get(scopeEntityName);

			toolNameRemainder = toolNameRemainder.substring(
				scopeEntityName.length());
		}

		if (batch) {
			toolNameRemainder = StringUtil.removeLast(
				toolNameRemainder, "Batch");
		}

		NounPhrase nounPhrase = _getNounPhrase(entityWords, toolNameRemainder);

		if (batch || collectionTool) {
			nounPhrase = new NounPhrase(
				WordUtil.toPlural(nounPhrase._noun), nounPhrase._wordsBefore,
				nounPhrase._wordsAfter);
		}

		String parameterPhrase = _getParameterPhrase(
			nounPhrase, path, scopePhrase);

		List<String> expansions = new ArrayList<>();

		for (String verb :
				_getApplicableVerbs(
					entityWords, _getVerbs(collectionTool, method))) {

			expansions.add(
				_toExpansion(
					batch, nounPhrase, parameterPhrase, scopePhrase, verb));
		}

		return expansions.toArray(new String[0]);
	}

	private static String[] _getActionExpansions(
		String actionMarker, String entityWords, String path, String toolName) {

		// Returns the phrases of a tool that names an action.
		// "putSiteBlogPostingSubscribe" gives "subscribe site blog posting by
		// site id".

		String toolNameRemainder = StringUtil.removeLast(
			_removeMethodPrefix(toolName), actionMarker);

		NounPhrase nounPhrase = _getNounPhrase(entityWords, toolNameRemainder);

		String parameterPhrase = _getParameterPhrase(
			nounPhrase, path, StringPool.BLANK);

		List<String> expansions = new ArrayList<>();

		for (String verb :
				MCPToolConstants.actionMarkerVerbs.get(actionMarker)) {

			expansions.add(
				_toExpansion(
					false, nounPhrase, parameterPhrase, StringPool.BLANK,
					verb));
		}

		return expansions.toArray(new String[0]);
	}

	private static String[] _getApplicableVerbs(
		String entityWords, String[] verbs) {

		// Return the verbs that suit the entity.
		// "blog posting" keeps "write" and drops "upload".

		List<String> applicableVerbs = new ArrayList<>();

		for (String verb : verbs) {
			String[] verbEntityWords = _verbEntityWords.get(verb);

			if (verbEntityWords == null) {
				applicableVerbs.add(verb);

				continue;
			}

			for (String verbEntityWord : verbEntityWords) {
				if (entityWords.contains(verbEntityWord)) {
					applicableVerbs.add(verb);

					break;
				}
			}
		}

		return applicableVerbs.toArray(new String[0]);
	}

	private static NounPhrase _getNounPhrase(
		String entityWords, String toolNameRemainder) {

		// Splits the tool name into a noun and the words around it.
		// "BlogPostingByExternalReferenceCode" gives the noun "blog posting"
		// followed by "by external reference code".

		String remainderWords = WordUtil.toWords(
			StringUtil.removeLast(toolNameRemainder, "Page"));

		Set<String> singularEntityWords = new HashSet<>();

		for (String word : StringUtil.split(entityWords, CharPool.SPACE)) {
			singularEntityWords.add(WordUtil.toSingular(word));
		}

		StringBundler wordsAfterEntitySB = new StringBundler();
		StringBundler wordsBeforeEntitySB = new StringBundler();

		boolean entityWordSeen = false;

		for (String word : StringUtil.split(remainderWords, CharPool.SPACE)) {
			if (singularEntityWords.contains(WordUtil.toSingular(word))) {
				entityWordSeen = true;

				continue;
			}

			if (!entityWordSeen) {
				wordsBeforeEntitySB.append(word);
				wordsBeforeEntitySB.append(StringPool.SPACE);
			}
			else if (!_firstPersonPronouns.contains(word)) {
				wordsAfterEntitySB.append(word);
				wordsAfterEntitySB.append(StringPool.SPACE);
			}
		}

		String wordsBeforeEntity = StringUtil.trim(
			wordsBeforeEntitySB.toString());

		if (StringUtil.startsWith(wordsBeforeEntity, "by ")) {
			return new NounPhrase(
				entityWords, StringPool.BLANK, wordsBeforeEntity);
		}

		String wordsAfterEntity = StringUtil.trim(
			wordsAfterEntitySB.toString());

		if (Validator.isNull(wordsAfterEntity)) {
			return new NounPhrase(
				entityWords, wordsBeforeEntity, StringPool.BLANK);
		}

		if (StringUtil.startsWith(wordsAfterEntity, "by ")) {
			return new NounPhrase(
				entityWords, wordsBeforeEntity, wordsAfterEntity);
		}

		return new NounPhrase(
			wordsAfterEntity,
			StringUtil.trim(wordsBeforeEntity + StringPool.SPACE + entityWords),
			StringPool.BLANK);
	}

	private static String _getParameterPhrase(
		NounPhrase nounPhrase, String path, String scopePhrase) {

		// Names the last path parameter, unless the phrase already names it.
		// "/user-accounts/{userAccountId}" gives "by user account id".

		String pathParameter = OpenAPIUtil.getLastPathParameter(path);

		if (pathParameter == null) {
			return StringPool.BLANK;
		}

		String pathParameterWords = WordUtil.toWords(pathParameter);

		String nounPhraseString = nounPhrase.toString();

		if (nounPhraseString.contains(pathParameterWords)) {
			return StringPool.BLANK;
		}

		for (String word :
				StringUtil.split(pathParameterWords, CharPool.SPACE)) {

			if (scopePhrase.contains(word)) {
				return StringPool.BLANK;
			}
		}

		return "by " + pathParameterWords;
	}

	private static String _getScopeEntityName(
		String entityWords, String toolNameRemainder) {

		// Returns the scope the tool name starts with.
		// "ObjectDefinitionObjectFieldsPage" gives "ObjectDefinition".

		String normalizedEntityWords = WordUtil.normalize(entityWords);

		for (String scopeEntityName : _scopePhrases.keySet()) {
			if (!toolNameRemainder.startsWith(scopeEntityName) ||
				Objects.equals(
					WordUtil.normalize(scopeEntityName),
					normalizedEntityWords)) {

				continue;
			}

			String rest = toolNameRemainder.substring(scopeEntityName.length());

			if (rest.isEmpty() || !Character.isUpperCase(rest.charAt(0))) {
				continue;
			}

			return scopeEntityName;
		}

		return null;
	}

	private static String[] _getVerbs(boolean collectionTool, String method) {

		// Returns the verbs of the HTTP method.
		// "get" gives "list" and "browse" for a page, "view" and "read"
		// otherwise.

		if (Objects.equals(method, "delete")) {
			return _VERBS_DELETE;
		}

		if (Objects.equals(method, "get")) {
			if (collectionTool) {
				return _VERBS_LIST;
			}

			return _VERBS_READ;
		}

		if (Objects.equals(method, "patch")) {
			return _VERBS_UPDATE;
		}

		if (Objects.equals(method, "post")) {
			return _VERBS_CREATE;
		}

		if (Objects.equals(method, "put")) {
			return _VERBS_REPLACE;
		}

		return new String[0];
	}

	private static String _removeMethodPrefix(String toolName) {

		// Removes the leading HTTP method.
		// "postSiteBlogPosting" gives "SiteBlogPosting".

		Matcher matcher = _methodPrefixPattern.matcher(toolName);

		return matcher.replaceFirst(StringPool.BLANK);
	}

	private static String _toExpansion(
		boolean batch, NounPhrase nounPhrase, String parameterPhrase,
		String scopePhrase, String verb) {

		// Joins the parts of one phrase.
		// The verb "delete" and the noun "user accounts" give "batch delete
		// user accounts".

		StringBundler sb = new StringBundler(12);

		if (batch) {
			sb.append("batch ");
		}

		sb.append(verb);
		sb.append(StringPool.SPACE);

		if (Validator.isNotNull(nounPhrase._wordsBefore)) {
			sb.append(nounPhrase._wordsBefore);
			sb.append(StringPool.SPACE);
		}

		sb.append(nounPhrase._noun);

		if (Validator.isNotNull(scopePhrase)) {
			sb.append(StringPool.SPACE);
			sb.append(scopePhrase);
		}

		if (Validator.isNotNull(nounPhrase._wordsAfter)) {
			sb.append(StringPool.SPACE);
			sb.append(nounPhrase._wordsAfter);
		}

		if (Validator.isNotNull(parameterPhrase)) {
			sb.append(StringPool.SPACE);
			sb.append(parameterPhrase);
		}

		return sb.toString();
	}

	private static final String[] _VERBS_CREATE = {
		"create", "add", "make", "start", "upload", "write"
	};

	private static final String[] _VERBS_DELETE = {
		"delete", "remove", "get rid of"
	};

	private static final String[] _VERBS_LIST = {
		"list", "show", "browse", "find", "get all", "see"
	};

	private static final String[] _VERBS_READ = {
		"get", "view", "open", "read", "fetch", "look up"
	};

	private static final String[] _VERBS_REPLACE = {
		"replace", "set", "overwrite"
	};

	private static final String[] _VERBS_UPDATE = {
		"update", "edit", "change", "rename", "modify"
	};

	private static final Set<String> _firstPersonPronouns = SetUtil.fromArray(
		"me", "my");
	private static final Pattern _methodPrefixPattern = Pattern.compile(
		StringBundler.concat(
			"^(", StringUtil.merge(OpenAPIUtil.METHODS, StringPool.PIPE), ")"));
	private static final Map<String, String> _scopePhrases =
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
	private static final Map<String, String[]> _verbEntityWords =
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

		@Override
		public String toString() {
			return StringUtil.trim(
				StringBundler.concat(
					_wordsBefore, StringPool.SPACE, _noun, StringPool.SPACE,
					_wordsAfter));
		}

		private NounPhrase(String noun, String wordsBefore, String wordsAfter) {
			_noun = noun;
			_wordsBefore = wordsBefore;
			_wordsAfter = wordsAfter;
		}

		private final String _noun;
		private final String _wordsAfter;
		private final String _wordsBefore;

	}

}