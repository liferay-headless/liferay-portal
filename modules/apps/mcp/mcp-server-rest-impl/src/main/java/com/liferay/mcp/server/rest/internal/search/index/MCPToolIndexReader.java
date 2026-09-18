/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index;

import com.liferay.mcp.server.rest.dto.v1_0.Prerequisite;
import com.liferay.mcp.server.rest.dto.v1_0.ToolSearchResult;
import com.liferay.mcp.server.rest.internal.search.index.constants.MCPToolConstants;
import com.liferay.mcp.server.rest.internal.search.index.constants.MCPToolFields;
import com.liferay.mcp.server.rest.internal.search.index.constants.MCPToolOperationVariants;
import com.liferay.mcp.server.rest.internal.search.index.util.IntentUtil;
import com.liferay.mcp.server.rest.internal.search.index.util.WordUtil;
import com.liferay.mcp.server.rest.internal.util.OpenAPIUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LinkedHashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.TermsAggregation;
import com.liferay.portal.search.aggregation.bucket.TermsAggregationResult;
import com.liferay.portal.search.aggregation.metrics.TopHitsAggregation;
import com.liferay.portal.search.aggregation.metrics.TopHitsAggregationResult;
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
import com.liferay.portal.search.query.TermQuery;
import com.liferay.portal.search.query.TermsQuery;
import com.liferay.portal.search.sort.ScoreSort;
import com.liferay.portal.search.sort.SortOrder;
import com.liferay.portal.search.sort.Sorts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = MCPToolIndexReader.class)
public class MCPToolIndexReader {

	public List<ToolSearchResult> search(
		long companyId, boolean includePrerequisites, String search, int size) {

		SearchSearchRequest searchSearchRequest = _createSearchSearchRequest(
			companyId, search, size);

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		SearchHits searchHits = searchSearchResponse.getSearchHits();

		List<SearchHit> searchHitsList = searchHits.getSearchHits();

		return _toToolSearchResults(
			companyId, includePrerequisites, searchHitsList);
	}

	private SearchSearchRequest _createSearchSearchRequest(
		long companyId, String search, int size) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setFetchSource(true);
		searchSearchRequest.setIndexNames(
			_mcpToolIndexCreator.getIndexName(companyId));
		searchSearchRequest.setQuery(_getQuery(search));
		searchSearchRequest.setSize(size);

		_setSorts(searchSearchRequest);

		return searchSearchRequest;
	}

	private BoostingQuery _getBoostingQuery(
		Query positiveQuery, Query negativeQuery, float negativeBoost) {

		BoostingQuery boostingQuery = QueriesUtil.boosting(
			positiveQuery, negativeQuery);

		boostingQuery.setNegativeBoost(negativeBoost);

		return boostingQuery;
	}

	private Set<String> _getCollectionSegments(Document document) {
		Set<String> collectionSegments = new HashSet<>();

		String[] segments = StringUtil.split(
			document.getString(MCPToolFields.PATH), CharPool.SLASH);

		for (int i = 1; i < (segments.length - 1); i++) {
			if (OpenAPIUtil.isPathParameter(segments[i])) {
				collectionSegments.add(segments[i - 1]);
			}
		}

		for (String requiredReference :
				document.getStrings(MCPToolFields.REQUIRED_REFERENCES)) {

			String[] parts = StringUtil.split(
				requiredReference, CharPool.POUND);

			if (parts.length == 2) {
				collectionSegments.add(parts[1]);
			}
		}

		return collectionSegments;
	}

	private Map<String, Document> _getCollectionToolDocuments(
		long companyId, List<SearchHit> searchHitsList) {

		Set<String> collectionSegments = new HashSet<>();

		for (SearchHit searchHit :
				ListUtil.subList(
					searchHitsList, 0, MCPToolConstants.DETAILED_HITS_MAX)) {

			collectionSegments.addAll(
				_getCollectionSegments(searchHit.getDocument()));
		}

		if (collectionSegments.isEmpty()) {
			return Collections.emptyMap();
		}

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(
				_getCollectionToolsSearchSearchRequest(
					companyId, collectionSegments));

		Map<String, AggregationResult> aggregationResultsMap =
			searchSearchResponse.getAggregationResultsMap();

		return _toCollectionToolDocuments(
			(TermsAggregationResult)aggregationResultsMap.get(
				MCPToolFields.COLLECTION_SEGMENT));
	}

	private SearchSearchRequest _getCollectionToolsSearchSearchRequest(
		long companyId, Set<String> collectionSegments) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		TermsAggregation termsAggregation = _aggregations.terms(
			MCPToolFields.COLLECTION_SEGMENT, MCPToolFields.COLLECTION_SEGMENT);

		TopHitsAggregation topHitsAggregation = _aggregations.topHits(
			_TOP_HITS_AGGREGATION_NAME);

		topHitsAggregation.addSortFields(
			_sorts.field(MCPToolFields.DEPRECATED, SortOrder.ASC),
			_sorts.field(MCPToolFields.PATH_PARAMETER_COUNT, SortOrder.ASC),
			_sorts.field(MCPToolFields.PATH_SEGMENTS_COUNT, SortOrder.ASC),
			_sorts.field(MCPToolFields.TOOL_SET_SIZE, SortOrder.DESC),
			_sorts.field(MCPToolFields.PATH_LENGTH, SortOrder.ASC),
			_sorts.field(MCPToolFields.UID, SortOrder.ASC));
		topHitsAggregation.setFetchSource(true);
		topHitsAggregation.setSize(1);

		termsAggregation.addChildAggregation(topHitsAggregation);

		termsAggregation.setSize(collectionSegments.size());

		TermsQuery termsQuery = QueriesUtil.terms(
			MCPToolFields.COLLECTION_SEGMENT);

		for (String collectionSegment : collectionSegments) {
			termsQuery.addValue(collectionSegment);
		}

		searchSearchRequest.addAggregation(termsAggregation);

		searchSearchRequest.setIndexNames(
			_mcpToolIndexCreator.getIndexName(companyId));
		searchSearchRequest.setQuery(termsQuery);
		searchSearchRequest.setSize(0);

		return searchSearchRequest;
	}

	private String _getDescription(Document document) {
		String description = document.getString(MCPToolFields.DESCRIPTION);

		String toolName = document.getString(MCPToolFields.TOOL_NAME);

		if (Validator.isBlank(description)) {
			description = WordUtil.toWords(toolName);
		}

		String hint = _operationHints.get(toolName);

		if (Objects.equals(
				document.getString(MCPToolFields.OPERATION_VARIANT),
				MCPToolOperationVariants.OPERATION_VARIANT_BATCH)) {

			hint = StringBundler.concat(
				"Takes many entities in one call, so use it rather than ",
				"calling the single entity operation repeatedly. It answers ",
				"202 with an import task, not a result: nothing is written ",
				"yet and 202 is not success. Before reporting the work done, ",
				"read executeStatus and failedItems by invoking getImportTask ",
				"in headless-batch-engine-v1.0, passing the id this returned.");
		}

		if (hint == null) {
			return description;
		}

		return description + StringPool.SPACE + hint;
	}

	private String _getEntityNameWord(String search) {
		if (_hasAssociationWords(search)) {
			return null;
		}

		String entityNameWord = null;

		for (String word :
				StringUtil.split(
					StringUtil.toLowerCase(search), CharPool.SPACE)) {

			if (_boundaryWords.contains(word)) {
				if (!Objects.equals(word, "of")) {
					break;
				}

				continue;
			}

			if (_articles.contains(word) || _genericNouns.contains(word)) {
				continue;
			}

			entityNameWord = word;
		}

		return entityNameWord;
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

	private String _getObjectTerm(List<String> intents) {
		if (intents == null) {
			return _OBJECT_TERM_DEFINITION + StringPool.SPACE +
				_OBJECT_TERM_ENTRY;
		}

		if (intents.contains(IntentUtil.INTENT_CREATE)) {
			return _OBJECT_TERM_DEFINITION;
		}

		if (intents.contains(IntentUtil.INTENT_LIST) ||
			intents.contains(IntentUtil.INTENT_READ)) {

			return _OBJECT_TERM_ENTRY;
		}

		return _OBJECT_TERM_DEFINITION + StringPool.SPACE + _OBJECT_TERM_ENTRY;
	}

	private Query _getOperationVariantQuery(String... operationVariants) {
		BooleanQuery booleanQuery = QueriesUtil.booleanQuery();

		for (String operationVariant : operationVariants) {
			booleanQuery.addShouldQueryClauses(
				QueriesUtil.term(
					MCPToolFields.OPERATION_VARIANT, operationVariant));
		}

		return booleanQuery;
	}

	private Query _getOtherIntentsQuery(List<String> intents) {
		BooleanQuery booleanQuery = QueriesUtil.booleanQuery();

		for (String intent : IntentUtil.getOtherIntents(intents)) {
			booleanQuery.addShouldQueryClauses(
				QueriesUtil.term(MCPToolFields.INTENT, intent));
		}

		return booleanQuery;
	}

	private String _getParameterNote(String parameter) {
		String note = _parameterHints.get(parameter);

		if (note != null) {
			return note;
		}

		if (StringUtil.endsWith(parameter, "ExternalReferenceCode")) {
			return "A generated code rather than a name. Read it from the " +
				"externalReferenceCode field of the listing named here.";
		}

		return null;
	}

	private List<Prerequisite> _getPrerequisites(
		Map<String, Document> collectionToolDocuments, boolean includeNotes,
		String path, List<String> requiredReferences) {

		List<Prerequisite> prerequisites = new ArrayList<>();

		String[] segments = StringUtil.split(path, CharPool.SLASH);

		for (int i = 1; i < (segments.length - 1); i++) {
			String parameter = OpenAPIUtil.getPathParameter(segments[i]);

			if (parameter == null) {
				continue;
			}

			Prerequisite prerequisite = new Prerequisite();

			prerequisite.setParameter(() -> parameter);

			if (includeNotes) {
				String note = _getParameterNote(parameter);

				if (note != null) {
					prerequisite.setNote(() -> note);
				}
			}

			Document document = collectionToolDocuments.get(segments[i - 1]);

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

			Document document = collectionToolDocuments.get(parts[1]);

			if (document == null) {
				continue;
			}

			Prerequisite prerequisite = new Prerequisite();

			prerequisite.setParameter(() -> parts[0]);
			prerequisite.setToolName(
				() -> document.getString(MCPToolFields.TOOL_NAME));
			prerequisite.setToolSetName(
				() -> document.getString(MCPToolFields.TOOL_SET_NAME));

			prerequisites.add(prerequisite);
		}

		return prerequisites;
	}

	private Query _getQuery(String search) {
		boolean batch = _hasBatchWords(search);
		List<String> intents = IntentUtil.getIntents(search);

		Query query = _getBoostingQuery(
			_getRelevanceQuery(
				batch, _replaceEntityWordWithObjectTerm(intents, search)),
			_getOperationVariantQuery(_OPERATION_VARIANTS_RARELY_WANTED),
			_NEGATIVE_BOOST_RARELY_WANTED);

		query = _getBoostingQuery(
			query,
			_getOperationVariantQuery(_OPERATION_VARIANTS_SECONDARY_ACTION),
			_NEGATIVE_BOOST_SECONDARY_ACTION);

		query = _getBoostingQuery(
			query, QueriesUtil.term(MCPToolFields.DEPRECATED, true),
			_NEGATIVE_BOOST_DEPRECATED);

		if (!batch) {
			query = _getBoostingQuery(
				query,
				_getOperationVariantQuery(
					MCPToolOperationVariants.OPERATION_VARIANT_BATCH),
				_NEGATIVE_BOOST_BATCH);
		}

		if (_hasAssociationWords(search)) {
			query = _getBoostingQuery(
				query,
				_getOperationVariantQuery(
					MCPToolOperationVariants.OPERATION_VARIANT_TRAVERSAL),
				_NEGATIVE_BOOST_TRAVERSAL);
		}

		if (ListUtil.isNotEmpty(intents)) {
			query = _getBoostingQuery(
				query, _getOtherIntentsQuery(intents),
				_NEGATIVE_BOOST_OTHER_INTENTS);
		}

		return query;
	}

	private Query _getRelevanceQuery(boolean batch, String search) {
		BooleanQuery booleanQuery = QueriesUtil.booleanQuery();

		booleanQuery.addShouldQueryClauses(
			_getMultiMatchQuery(search),
			new MatchPhraseQuery(MCPToolFields.EXPANSION + ".phrase", search));

		if (batch) {
			TermQuery termQuery = QueriesUtil.term(
				MCPToolFields.OPERATION_VARIANT,
				MCPToolOperationVariants.OPERATION_VARIANT_BATCH);

			termQuery.setBoost(_BOOST_BATCH);

			booleanQuery.addShouldQueryClauses(termQuery);
		}

		String entityNameWord = _getEntityNameWord(search);

		if (entityNameWord == null) {
			return booleanQuery;
		}

		MatchQuery matchQuery = QueriesUtil.match(
			MCPToolFields.ENTITY_NAME + ".split", entityNameWord);

		matchQuery.setBoost(_BOOST_ENTITY_NAME_WORD);

		return booleanQuery.addShouldQueryClauses(matchQuery);
	}

	private boolean _hasAssociationWords(String search) {
		String[] words = StringUtil.split(
			StringUtil.toLowerCase(search), CharPool.SPACE);

		for (String word : words) {
			if (_associationWords.contains(word)) {
				return true;
			}

			String preposition = _associationVerbPrepositions.get(word);

			if ((preposition != null) &&
				ArrayUtil.contains(words, preposition)) {

				return true;
			}
		}

		return false;
	}

	private boolean _hasBatchWords(String search) {
		String[] words = StringUtil.split(
			StringUtil.toLowerCase(search), CharPool.SPACE);

		for (int i = 0; i < words.length; i++) {
			String word = words[i];

			if (_batchWords.contains(word)) {
				return true;
			}

			if (Validator.isNumber(word) &&
				(GetterUtil.getInteger(word) >= _BATCH_MIN_QUANTITY) &&
				_isQuantityExpression(i, words)) {

				return true;
			}
		}

		return false;
	}

	private boolean _isQuantityExpression(int index, String[] words) {

		// A number counts something when a verb precedes it and a plural
		// follows it, as in "delete 20 users".

		if ((index == (words.length - 1)) ||
			!WordUtil.isPlural(words[index + 1])) {

			return false;
		}

		if (index == 0) {
			return true;
		}

		List<String> intents = IntentUtil.getIntents(words[index - 1]);

		if (intents != null) {
			return true;
		}

		return false;
	}

	private String _replaceEntityWordWithObjectTerm(
		List<String> intents, String search) {

		Matcher matcher = _entityWordPattern.matcher(search);

		return matcher.replaceAll(_getObjectTerm(intents));
	}

	private void _setSorts(SearchSearchRequest searchSearchRequest) {
		ScoreSort scoreSort = _sorts.score();

		scoreSort.setSortOrder(SortOrder.DESC);

		searchSearchRequest.addSorts(
			scoreSort,
			_sorts.field(MCPToolFields.PATH_SEGMENTS_COUNT, SortOrder.ASC),
			_sorts.field(MCPToolFields.UID, SortOrder.ASC));
	}

	private Map<String, Document> _toCollectionToolDocuments(
		TermsAggregationResult termsAggregationResult) {

		Map<String, Document> collectionToolDocuments = new HashMap<>();

		for (Bucket bucket : termsAggregationResult.getBuckets()) {
			TopHitsAggregationResult topHitsAggregationResult =
				(TopHitsAggregationResult)bucket.getChildAggregationResult(
					_TOP_HITS_AGGREGATION_NAME);

			SearchHits searchHits = topHitsAggregationResult.getSearchHits();

			List<SearchHit> searchHitsList = searchHits.getSearchHits();

			if (searchHitsList.isEmpty()) {
				continue;
			}

			SearchHit searchHit = searchHitsList.get(0);

			collectionToolDocuments.put(
				bucket.getKey(), searchHit.getDocument());
		}

		return collectionToolDocuments;
	}

	private ToolSearchResult _toToolSearchResult(Document document) {
		return new ToolSearchResult() {
			{
				setDescription(() -> _getDescription(document));
				setToolName(() -> document.getString(MCPToolFields.TOOL_NAME));
				setToolSetName(
					() -> document.getString(MCPToolFields.TOOL_SET_NAME));
			}
		};
	}

	private List<ToolSearchResult> _toToolSearchResults(
		long companyId, boolean includePrerequisites,
		List<SearchHit> searchHitsList) {

		List<ToolSearchResult> toolSearchResults = new ArrayList<>();

		Map<String, Document> collectionToolDocuments = null;

		if (includePrerequisites) {
			collectionToolDocuments = _getCollectionToolDocuments(
				companyId, searchHitsList);
		}

		for (SearchHit searchHit : searchHitsList) {
			Document document = searchHit.getDocument();

			ToolSearchResult toolSearchResult = _toToolSearchResult(document);

			if (includePrerequisites &&
				(toolSearchResults.size() <
					MCPToolConstants.DETAILED_HITS_MAX)) {

				List<Prerequisite> prerequisites = _getPrerequisites(
					collectionToolDocuments, toolSearchResults.isEmpty(),
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

	private static final int _BATCH_MIN_QUANTITY = 10;

	private static final float _BOOST_BATCH = 3.0F;

	private static final float _BOOST_ENTITY_NAME_WORD = 2.0F;

	private static final float _BOOST_FIELD_DESCRIPTION = 1.0F;

	private static final float _BOOST_FIELD_ENTITY_NAME_SPLIT = 4.0F;

	private static final float _BOOST_FIELD_EXPANSION = 1.5F;

	private static final float _BOOST_FIELD_PARAMETERS = 0.25F;

	private static final float _BOOST_FIELD_PATH = 2.0F;

	private static final float _BOOST_FIELD_SCHEMA_PROPERTIES = 0.75F;

	private static final float _BOOST_FIELD_TOOL_NAME = 5.0F;

	private static final float _BOOST_FIELD_TOOL_NAME_SPLIT = 2.0F;

	private static final float _NEGATIVE_BOOST_BATCH = 0.3F;

	private static final float _NEGATIVE_BOOST_DEPRECATED = 0.8F;

	private static final float _NEGATIVE_BOOST_OTHER_INTENTS = 0.5F;

	private static final float _NEGATIVE_BOOST_RARELY_WANTED = 0.3F;

	private static final float _NEGATIVE_BOOST_SECONDARY_ACTION = 0.8F;

	private static final float _NEGATIVE_BOOST_TRAVERSAL = 0.7F;

	private static final String _OBJECT_TERM_DEFINITION = "object definition";

	private static final String _OBJECT_TERM_ENTRY = "object entry";

	private static final String[] _OPERATION_VARIANTS_RARELY_WANTED = {
		MCPToolOperationVariants.OPERATION_VARIANT_HISTORY,
		MCPToolOperationVariants.OPERATION_VARIANT_KEYED,
		MCPToolOperationVariants.OPERATION_VARIANT_NESTED,
		MCPToolOperationVariants.OPERATION_VARIANT_OPENAPI,
		MCPToolOperationVariants.OPERATION_VARIANT_PERMISSIONS,
		MCPToolOperationVariants.OPERATION_VARIANT_PREVIEW,
		MCPToolOperationVariants.OPERATION_VARIANT_RATING,
		MCPToolOperationVariants.OPERATION_VARIANT_SUBSCRIPTION
	};

	private static final String[] _OPERATION_VARIANTS_SECONDARY_ACTION = {
		MCPToolOperationVariants.OPERATION_VARIANT_APPROVED,
		MCPToolOperationVariants.OPERATION_VARIANT_COPY,
		MCPToolOperationVariants.OPERATION_VARIANT_EXPIRE,
		MCPToolOperationVariants.OPERATION_VARIANT_MOVE,
		MCPToolOperationVariants.OPERATION_VARIANT_RESTORE,
		MCPToolOperationVariants.OPERATION_VARIANT_TRANSLATION,
		MCPToolOperationVariants.OPERATION_VARIANT_VALIDATE
	};

	private static final String _TOP_HITS_AGGREGATION_NAME = "topHits";

	private static final Set<String> _articles = SetUtil.fromArray(
		"a", "an", "the");
	private static final Map<String, String> _associationVerbPrepositions =
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
	private static final Set<String> _batchWords = SetUtil.fromArray(
		"batch", "batches", "bulk", "dozen", "hundred", "many", "ten",
		"thousand", "twelve", "twenty");
	private static final Set<String> _boundaryWords = SetUtil.fromArray(
		"and", "as", "at", "belonging", "by", "for", "from", "in", "inside",
		"into", "of", "on", "onto", "that", "to", "under", "using", "via",
		"whose", "with", "within");
	private static final Pattern _entityWordPattern = Pattern.compile(
		"\\b(entity|entities)\\b", Pattern.CASE_INSENSITIVE);
	private static final Set<String> _genericNouns = SetUtil.fromArray(
		"data", "detail", "details", "entries", "entry", "info", "information",
		"item", "items", "object", "objects", "record", "records", "row",
		"rows", "value", "values");
	private static final Map<String, String> _operationHints =
		HashMapBuilder.put(
			"postObjectDefinition",
			StringBundler.concat(
				"Once published, this object's entries get their own tools, ",
				"named after the object rather than after Liferay: to add ",
				"one, search for the object's own name, as in \"create a pet ",
				"store\", not \"create an object entry\".")
		).put(
			"postObjectDefinitionPublish",
			StringBundler.concat(
				"Publishing registers a tool set for this object's entries, ",
				"named after the object. To add one, search for the object's ",
				"own name, as in \"create a pet store\", not \"create an ",
				"object entry\".")
		).build();
	private static final Map<String, String> _parameterHints =
		HashMapBuilder.put(
			"assetLibraryId",
			StringBundler.concat(
				"The asset library's key, its numeric id or its external ",
				"reference code. The key is case sensitive.")
		).put(
			"siteId",
			StringBundler.concat(
				"The site's key, its numeric id or its external reference ",
				"code. The key is usually the site's name and is case ",
				"sensitive, so try it before listing sites.")
		).build();

	@Reference
	private Aggregations _aggregations;

	@Reference
	private MCPToolIndexCreator _mcpToolIndexCreator;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

	@Reference
	private Sorts _sorts;

}