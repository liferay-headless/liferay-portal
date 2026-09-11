/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index;

import com.liferay.mcp.server.rest.dto.v1_0.Prerequisite;
import com.liferay.mcp.server.rest.dto.v1_0.ToolSearchResult;
import com.liferay.mcp.server.rest.internal.constants.MCPClientHints;
import com.liferay.mcp.server.rest.internal.search.constants.MCPToolFields;
import com.liferay.mcp.server.rest.internal.search.constants.MCPToolModifiers;
import com.liferay.mcp.server.rest.internal.search.index.util.IntentExtractorUtil;
import com.liferay.mcp.server.rest.internal.search.index.util.ResolverUtil;
import com.liferay.mcp.server.rest.internal.search.index.util.SearchPhraseUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
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
import com.liferay.portal.search.query.QueriesUtil;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.sort.ScoreSort;
import com.liferay.portal.search.sort.SortOrder;
import com.liferay.portal.search.sort.Sorts;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = MCPToolIndexReader.class)
public class MCPToolIndexReader {

	public List<ToolSearchResult> search(
		long companyId, float confidenceMargin, boolean includePrerequisites,
		int limit, String search) {

		SearchSearchRequest searchSearchRequest = _createSearchSearchRequest(
			companyId, limit, search);

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		SearchHits searchHits = searchSearchResponse.getSearchHits();

		List<SearchHit> searchHitsList = searchHits.getSearchHits();

		if (_isConfident(confidenceMargin, searchHitsList)) {
			searchHitsList = searchHitsList.subList(0, 1);
		}

		return _toToolSearchResults(
			companyId, includePrerequisites, searchHitsList);
	}

	private SearchSearchRequest _createSearchSearchRequest(
		long companyId, int limit, String search) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setFetchSource(true);
		searchSearchRequest.setIndexNames(
			_mcpToolIndexCreator.getIndexName(companyId));
		searchSearchRequest.setQuery(_getQuery(search));
		searchSearchRequest.setSize(limit);

		_setSorts(searchSearchRequest);

		return searchSearchRequest;
	}

	private Query _getIntentQuery(List<String> intents) {
		if (intents == null) {
			return null;
		}

		BooleanQuery booleanQuery = QueriesUtil.booleanQuery();

		for (String intent : IntentExtractorUtil.getOtherIntents(intents)) {
			booleanQuery.addShouldQueryClauses(
				QueriesUtil.term(MCPToolFields.INTENT, intent));
		}

		return booleanQuery;
	}

	private Query _getModifierQuery(String[] modifiers) {
		BooleanQuery booleanQuery = QueriesUtil.booleanQuery();

		for (String modifier : modifiers) {
			booleanQuery.addShouldQueryClauses(
				QueriesUtil.term(MCPToolFields.MODIFIER, modifier));
		}

		return booleanQuery;
	}

	private MultiMatchQuery _getMultiMatchQuery(String search) {
		MultiMatchQuery multiMatchQuery = QueriesUtil.multiMatch(
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

	private String _getParameterNote(String parameter) {
		String note = MCPClientHints.parameters.get(parameter);

		if (note != null) {
			return note;
		}

		if (StringUtil.endsWith(parameter, "ExternalReferenceCode")) {
			return MCPClientHints.EXTERNAL_REFERENCE_CODE_NOTE;
		}

		return null;
	}

	private List<Prerequisite> _getPrerequisites(
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

			Document document = _getResolverDocument(
				companyId, segments[i - 1]);

			if (document != null) {
				prerequisite.setToolName(
					() -> document.getString(MCPToolFields.TOOL_NAME));
				prerequisite.setToolSetName(
					() -> document.getString(MCPToolFields.TOOL_SET_NAME));
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

			Document document = _getResolverDocument(companyId, parts[1]);

			if (document == null) {
				continue;
			}

			Prerequisite prerequisite = new Prerequisite();

			prerequisite.setToolName(
				() -> document.getString(MCPToolFields.TOOL_NAME));
			prerequisite.setParameter(() -> parts[0]);
			prerequisite.setToolSetName(
				() -> document.getString(MCPToolFields.TOOL_SET_NAME));

			prerequisites.add(prerequisite);
		}

		return prerequisites;
	}

	private Query _getQuery(String search) {
		List<String> intents = IntentExtractorUtil.getIntents(search);

		BoostingQuery boostingQuery = QueriesUtil.boosting(
			_getScoringQuery(
				SearchPhraseUtil.replaceEntityWords(intents, search)),
			_getModifierQuery(MCPToolModifiers.RARELY_WANTED_MODIFIERS));

		boostingQuery.setNegativeBoost(_NEGATIVE_BOOST_RARELY_WANTED);

		BoostingQuery reshapingBoostingQuery = QueriesUtil.boosting(
			boostingQuery,
			_getModifierQuery(MCPToolModifiers.RESHAPING_MODIFIERS));

		reshapingBoostingQuery.setNegativeBoost(_NEGATIVE_BOOST_RESHAPING);

		BoostingQuery deprecatedBoostingQuery = QueriesUtil.boosting(
			reshapingBoostingQuery,
			QueriesUtil.term(MCPToolFields.DEPRECATED, true));

		deprecatedBoostingQuery.setNegativeBoost(_NEGATIVE_BOOST_DEPRECATED);

		Query query = deprecatedBoostingQuery;

		if (!SearchPhraseUtil.isBatch(search)) {
			BoostingQuery batchBoostingQuery = QueriesUtil.boosting(
				query,
				QueriesUtil.term(
					MCPToolFields.MODIFIER, MCPToolModifiers.MODIFIER_BATCH));

			batchBoostingQuery.setNegativeBoost(_NEGATIVE_BOOST_BATCH);

			query = batchBoostingQuery;
		}

		if (!SearchPhraseUtil.isAssociating(search)) {
			BoostingQuery traversalBoostingQuery = QueriesUtil.boosting(
				query,
				QueriesUtil.term(
					MCPToolFields.MODIFIER,
					MCPToolModifiers.MODIFIER_TRAVERSAL));

			traversalBoostingQuery.setNegativeBoost(_NEGATIVE_BOOST_TRAVERSAL);

			query = traversalBoostingQuery;
		}

		return _withIntentQuery(intents, query);
	}

	private Document _getResolverDocument(long companyId, String segment) {
		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.addSorts(
			_sorts.field(MCPToolFields.DEPRECATED, SortOrder.ASC),
			_sorts.field(MCPToolFields.PATH_PARAMETER_COUNT, SortOrder.ASC),
			_sorts.field(MCPToolFields.PATH_SEGMENTS_COUNT, SortOrder.ASC),
			_sorts.field(MCPToolFields.TOOL_SET_SIZE, SortOrder.DESC),
			_sorts.field(MCPToolFields.PATH_LENGTH, SortOrder.ASC),
			_sorts.field(MCPToolFields.UID, SortOrder.ASC));
		searchSearchRequest.setFetchSource(true);
		searchSearchRequest.setIndexNames(
			_mcpToolIndexCreator.getIndexName(companyId));
		searchSearchRequest.setQuery(
			QueriesUtil.term(MCPToolFields.RESOLVER_SEGMENT, segment));
		searchSearchRequest.setSize(1);

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		SearchHits searchHits = searchSearchResponse.getSearchHits();

		List<SearchHit> searchHitsList = searchHits.getSearchHits();

		if (searchHitsList.isEmpty()) {
			return null;
		}

		SearchHit searchHit = searchHitsList.get(0);

		return searchHit.getDocument();
	}

	private Query _getScoringQuery(String search) {
		BooleanQuery booleanQuery = QueriesUtil.booleanQuery();

		booleanQuery.addShouldQueryClauses(
			_getMultiMatchQuery(search),
			new MatchPhraseQuery(MCPToolFields.EXPANSION + ".phrase", search));

		String headNoun = SearchPhraseUtil.getHeadNoun(search);

		if (headNoun == null) {
			return booleanQuery;
		}

		MatchQuery matchQuery = QueriesUtil.match(
			MCPToolFields.ENTITY_NAME + ".split", headNoun);

		matchQuery.setBoost(_BOOST_HEAD_NOUN);

		return booleanQuery.addShouldQueryClauses(matchQuery);
	}

	private boolean _isConfident(
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

	private void _setSorts(SearchSearchRequest searchSearchRequest) {
		Sorts sorts = _sorts;

		ScoreSort scoreSort = sorts.score();

		scoreSort.setSortOrder(SortOrder.DESC);

		searchSearchRequest.addSorts(
			scoreSort,
			sorts.field(MCPToolFields.PATH_SEGMENTS_COUNT, SortOrder.ASC),
			sorts.field(MCPToolFields.UID, SortOrder.ASC));
	}

	private ToolSearchResult _toToolSearchResult(Document document) {
		ToolSearchResult toolSearchResult = new ToolSearchResult();

		toolSearchResult.setDescription(
			() -> {
				String description = document.getString(
					MCPToolFields.DESCRIPTION);

				if (Validator.isNull(description)) {
					description = document.getString(
						MCPToolFields.TOOL_NAME + ".split");
				}

				String hint = MCPClientHints.operations.get(
					document.getString(MCPToolFields.TOOL_NAME));

				if (Objects.equals(
						document.getString(MCPToolFields.MODIFIER), "batch")) {

					hint = MCPClientHints.BATCH;
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

	private List<ToolSearchResult> _toToolSearchResults(
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

	private Query _withIntentQuery(List<String> intents, Query query) {
		Query intentQuery = _getIntentQuery(intents);

		if (intentQuery == null) {
			return query;
		}

		BoostingQuery intentBoostingQuery = QueriesUtil.boosting(
			query, intentQuery);

		intentBoostingQuery.setNegativeBoost(_NEGATIVE_BOOST_OTHER_INTENTS);

		return intentBoostingQuery;
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

	@Reference
	private MCPToolIndexCreator _mcpToolIndexCreator;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

	@Reference
	private Sorts _sorts;

}