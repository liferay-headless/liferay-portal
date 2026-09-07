/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index;

import com.liferay.mcp.server.rest.dto.v1_0.Prerequisite;
import com.liferay.mcp.server.rest.dto.v1_0.ToolSearchResult;
import com.liferay.mcp.server.rest.internal.constants.MCPToolClientAdvices;
import com.liferay.mcp.server.rest.internal.search.constants.MCPToolFields;
import com.liferay.mcp.server.rest.internal.search.constants.MCPToolModifiers;
import com.liferay.mcp.server.rest.internal.search.index.util.IntentExtractorUtil;
import com.liferay.mcp.server.rest.internal.search.index.util.MCPTool;
import com.liferay.mcp.server.rest.internal.search.index.util.ResolverUtil;
import com.liferay.mcp.server.rest.internal.search.index.util.SearchPhraseUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.LinkedHashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.BoostingQuery;
import com.liferay.portal.search.query.MatchPhraseQuery;
import com.liferay.portal.search.query.MatchQuery;
import com.liferay.portal.search.query.MultiMatchQuery;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.query.TermQuery;
import com.liferay.portal.search.sort.ScoreSort;
import com.liferay.portal.search.sort.SortOrder;
import com.liferay.portal.search.sort.Sorts;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Petteri Karttunen
 */
public class MCPToolIndexReaderUtil {

	public static List<ToolSearchResult> search(
		long companyId, boolean includePrerequisites, int limit, String search,
		float confidenceMargin) {

		SearchSearchRequest searchSearchRequest = _createSearchSearchRequest(
			companyId, limit, search);

		SearchEngineAdapter searchEngineAdapter =
			_searchEngineAdapterSnapshot.get();

		SearchSearchResponse searchSearchResponse = searchEngineAdapter.execute(
			searchSearchRequest);

		SearchHits searchHits = searchSearchResponse.getSearchHits();

		List<SearchHit> searchHitsList = searchHits.getSearchHits();

		if (_isConfident(confidenceMargin, searchHitsList)) {
			searchHitsList = searchHitsList.subList(0, 1);
		}

		return _toToolSearchResults(
			companyId, includePrerequisites, searchHitsList);
	}

	private static SearchSearchRequest _createSearchSearchRequest(
		long companyId, int limit, String search) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setFetchSource(true);
		searchSearchRequest.setIndexNames(
			MCPToolIndexCreatorUtil.getIndexName(companyId));
		searchSearchRequest.setQuery(_getQuery(search));
		searchSearchRequest.setSize(limit);

		_setSorts(searchSearchRequest);

		return searchSearchRequest;
	}

	private static Query _getIntentQuery(List<String> intents) {
		if (intents == null) {
			return null;
		}

		BooleanQuery booleanQuery = new BooleanQuery();

		for (String intent : IntentExtractorUtil.getOtherIntents(intents)) {
			booleanQuery.addShouldQueryClauses(
				new TermQuery(MCPToolFields.INTENT, intent));
		}

		return booleanQuery;
	}

	private static Query _getModifierQuery(String[] modifiers) {
		BooleanQuery booleanQuery = new BooleanQuery();

		for (String modifier : modifiers) {
			booleanQuery.addShouldQueryClauses(
				new TermQuery(MCPToolFields.MODIFIER, modifier));
		}

		return booleanQuery;
	}

	private static MultiMatchQuery _getMultiMatchQuery(String search) {
		MultiMatchQuery multiMatchQuery = new MultiMatchQuery(
			search,
			LinkedHashMapBuilder.put(
				MCPToolFields.DESCRIPTION, _BOOST_FIELD_DESCRIPTION
			).put(
				MCPToolFields.EXPANSION, _BOOST_FIELD_EXPANSION
			).put(
				MCPToolFields.TOOL_NAME + ".split", _BOOST_FIELD_TOOL_NAME_SPLIT
			).put(
				MCPToolFields.PARAMETERS, _BOOST_FIELD_PARAMETERS
			).put(
				MCPToolFields.PATH, _BOOST_FIELD_PATH
			).put(
				MCPToolFields.SCHEMA_PROPERTIES, _BOOST_FIELD_SCHEMA_PROPERTIES
			).put(
				MCPToolFields.ENTITY_NAME + ".split",
				_BOOST_FIELD_ENTITY_NAME_SPLIT
			).put(
				MCPToolFields.TOOL_NAME, _BOOST_FIELD_TOOL_NAME
			).build());

		multiMatchQuery.setType(MultiMatchQuery.Type.CROSS_FIELDS);

		return multiMatchQuery;
	}

	private static String _getParameterNote(String parameter) {
		String note = MCPToolClientAdvices.parameterHints.get(parameter);

		if (note != null) {
			return note;
		}

		if (StringUtil.endsWith(parameter, "ExternalReferenceCode")) {
			return MCPToolClientAdvices.EXTERNAL_REFERENCE_CODE_NOTE;
		}

		return null;
	}

	private static List<Prerequisite> _getPrerequisites(
		long companyId, boolean includeNotes, String path,
		List<String> requiredReferences) {

		List<Prerequisite> prerequisites = new ArrayList<>();

		String[] segments = StringUtil.split(path, CharPool.SLASH);

		for (int i = 1; i < (segments.length - 1); i++) {
			String parameter = ResolverUtil.getPathParameter(segments[i]);

			if (parameter == null) {
				continue;
			}

			Prerequisite prerequisite = new Prerequisite();

			prerequisite.setParameter(() -> parameter);

			String note = includeNotes ? _getParameterNote(parameter) : null;

			if (note != null) {
				prerequisite.setNote(() -> note);
			}

			MCPTool mcpTool = ResolverUtil.getResolverMCPTool(
				companyId, segments[i - 1]);

			if (mcpTool != null) {
				prerequisite.setToolName(mcpTool::getToolName);
				prerequisite.setToolSetName(mcpTool::getToolSetName);
			}

			prerequisites.add(prerequisite);
		}

		for (String requiredReference :
				ListUtil.filter(requiredReferences, Validator::isNotNull)) {

			String[] parts = StringUtil.split(
				requiredReference, CharPool.POUND);

			if (parts.length != 2) {
				continue;
			}

			MCPTool mcpTool = ResolverUtil.getResolverMCPTool(
				companyId, parts[1]);

			if (mcpTool == null) {
				continue;
			}

			Prerequisite prerequisite = new Prerequisite();

			prerequisite.setToolName(mcpTool::getToolName);
			prerequisite.setParameter(() -> parts[0]);
			prerequisite.setToolSetName(mcpTool::getToolSetName);

			prerequisites.add(prerequisite);
		}

		return prerequisites;
	}

	private static Query _getQuery(String search) {
		List<String> intents = IntentExtractorUtil.getIntents(search);

		BoostingQuery boostingQuery = new BoostingQuery(
			_getScoringQuery(
				SearchPhraseUtil.replaceEntityWords(intents, search)),
			_getModifierQuery(MCPToolModifiers.RARELY_WANTED_MODIFIERS));

		boostingQuery.setNegativeBoost(_NEGATIVE_BOOST_RARELY_WANTED);

		BoostingQuery reshapingBoostingQuery = new BoostingQuery(
			boostingQuery,
			_getModifierQuery(MCPToolModifiers.RESHAPING_MODIFIERS));

		reshapingBoostingQuery.setNegativeBoost(_NEGATIVE_BOOST_RESHAPING);

		BoostingQuery deprecatedBoostingQuery = new BoostingQuery(
			reshapingBoostingQuery,
			new TermQuery(MCPToolFields.DEPRECATED, true));

		deprecatedBoostingQuery.setNegativeBoost(_NEGATIVE_BOOST_DEPRECATED);

		Query query = deprecatedBoostingQuery;

		if (!SearchPhraseUtil.isBatch(search)) {
			BoostingQuery batchBoostingQuery = new BoostingQuery(
				query,
				new TermQuery(
					MCPToolFields.MODIFIER, MCPToolModifiers.MODIFIER_BATCH));

			batchBoostingQuery.setNegativeBoost(_NEGATIVE_BOOST_BATCH);

			query = batchBoostingQuery;
		}

		if (!SearchPhraseUtil.isAssociating(search)) {
			BoostingQuery traversalBoostingQuery = new BoostingQuery(
				query,
				new TermQuery(
					MCPToolFields.MODIFIER,
					MCPToolModifiers.MODIFIER_TRAVERSAL));

			traversalBoostingQuery.setNegativeBoost(_NEGATIVE_BOOST_TRAVERSAL);

			query = traversalBoostingQuery;
		}

		Query intentQuery = _getIntentQuery(intents);

		if (intentQuery == null) {
			return query;
		}

		BoostingQuery intentBoostingQuery = new BoostingQuery(
			query, intentQuery);

		intentBoostingQuery.setNegativeBoost(_NEGATIVE_BOOST_OTHER_INTENTS);

		return intentBoostingQuery;
	}

	private static Query _getScoringQuery(String search) {
		BooleanQuery booleanQuery = new BooleanQuery();

		booleanQuery.addShouldQueryClauses(
			_getMultiMatchQuery(search),
			new MatchPhraseQuery(MCPToolFields.EXPANSION + ".phrase", search));

		String headNoun = SearchPhraseUtil.getHeadNoun(search);

		if (headNoun == null) {
			return booleanQuery;
		}

		MatchQuery matchQuery = new MatchQuery(
			MCPToolFields.ENTITY_NAME + ".split", headNoun);

		matchQuery.setBoost(_BOOST_HEAD_NOUN);

		return booleanQuery.addShouldQueryClauses(matchQuery);
	}

	private static boolean _isConfident(
		float confidenceMargin, List<SearchHit> searchHits) {

		if ((confidenceMargin <= 0) || (searchHits.size() < 2)) {
			return false;
		}

		SearchHit searchHit = searchHits.get(0);

		float score = searchHit.getScore();

		if (score <= 0) {
			return false;
		}

		SearchHit nextSearchHit = searchHits.get(1);

		float margin = (score - nextSearchHit.getScore()) / score;

		if (margin > confidenceMargin) {
			return true;
		}

		return false;
	}

	private static void _setSorts(SearchSearchRequest searchSearchRequest) {
		Sorts sorts = _sortsSnapshot.get();

		ScoreSort scoreSort = sorts.score();

		scoreSort.setSortOrder(SortOrder.DESC);

		searchSearchRequest.addSorts(
			scoreSort,
			sorts.field(MCPToolFields.PATH_SEGMENTS_COUNT, SortOrder.ASC),
			sorts.field(MCPToolFields.UID, SortOrder.ASC));
	}

	private static ToolSearchResult _toToolSearchResult(Document document) {
		ToolSearchResult toolSearchResult = new ToolSearchResult();

		toolSearchResult.setDescription(
			() -> {
				String description = document.getString(
					MCPToolFields.DESCRIPTION);

				if (Validator.isNull(description)) {
					description = document.getString(
						MCPToolFields.TOOL_NAME + ".split");
				}

				String hint = MCPToolClientAdvices.toolHints.get(
					document.getString(MCPToolFields.TOOL_NAME));

				if (Objects.equals(
						document.getString(MCPToolFields.MODIFIER), "batch")) {

					hint = MCPToolClientAdvices.BATCH_HINT;
				}

				if (hint == null) {
					return description;
				}

				return description + StringPool.SPACE + hint;
			});
		toolSearchResult.setName(
			() -> document.getString(MCPToolFields.TOOL_NAME));
		toolSearchResult.setToolSetName(
			() -> document.getString(MCPToolFields.TOOL_SET_NAME));

		return toolSearchResult;
	}

	private static List<ToolSearchResult> _toToolSearchResults(
		long companyId, boolean includePrerequisites,
		List<SearchHit> searchHitsList) {

		List<ToolSearchResult> toolSearchResults = new ArrayList<>();

		for (SearchHit searchHit : searchHitsList) {
			Document document = searchHit.getDocument();

			ToolSearchResult toolSearchResult = _toToolSearchResult(document);

			if (includePrerequisites && (toolSearchResults.size() < 3)) {
				List<Prerequisite> prerequisites = _getPrerequisites(
					companyId, toolSearchResults.isEmpty(),
					document.getString(MCPToolFields.PATH),
					document.getStrings(MCPToolFields.REQUIRED_REFERENCES));

				if (!prerequisites.isEmpty()) {
					toolSearchResult.setPrerequisites(
						() -> prerequisites.toArray(new Prerequisite[0]));
				}
			}

			toolSearchResults.add(toolSearchResult);
		}

		return toolSearchResults;
	}

	private static final float _BOOST_FIELD_DESCRIPTION = 1.0F;

	private static final float _BOOST_FIELD_ENTITY_NAME_SPLIT = 4.0F;

	private static final float _BOOST_FIELD_EXPANSION = 1.5F;

	private static final float _BOOST_FIELD_PARAMETERS = 0.25F;

	private static final float _BOOST_FIELD_PATH = 2.0F;

	private static final float _BOOST_FIELD_SCHEMA_PROPERTIES = 0.75F;

	private static final float _BOOST_FIELD_TOOL_NAME = 5.0F;

	private static final float _BOOST_FIELD_TOOL_NAME_SPLIT = 2.0F;

	private static final float _BOOST_HEAD_NOUN = 2.0F;

	private static final float _NEGATIVE_BOOST_BATCH = 0.3F;

	private static final float _NEGATIVE_BOOST_DEPRECATED = 0.8F;

	private static final float _NEGATIVE_BOOST_OTHER_INTENTS = 0.5F;

	private static final float _NEGATIVE_BOOST_RARELY_WANTED = 0.3F;

	private static final float _NEGATIVE_BOOST_RESHAPING = 0.8F;

	private static final float _NEGATIVE_BOOST_TRAVERSAL = 0.7F;

	private static final Snapshot<SearchEngineAdapter>
		_searchEngineAdapterSnapshot = new Snapshot<>(
			MCPToolIndexReaderUtil.class, SearchEngineAdapter.class);
	private static final Snapshot<Sorts> _sortsSnapshot = new Snapshot<>(
		MCPToolIndexReaderUtil.class, Sorts.class);

}