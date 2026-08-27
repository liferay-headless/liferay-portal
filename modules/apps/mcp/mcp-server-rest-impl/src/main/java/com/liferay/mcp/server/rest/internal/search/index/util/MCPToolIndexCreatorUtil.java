/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index.util;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.index.CreateIndexRequest;
import com.liferay.portal.search.engine.adapter.index.DeleteIndexRequest;
import com.liferay.portal.search.engine.adapter.index.IndicesExistsIndexRequest;
import com.liferay.portal.search.engine.adapter.index.IndicesExistsIndexResponse;
import com.liferay.portal.search.index.IndexNameBuilder;

/**
 * @author Petteri Karttunen
 */
public class MCPToolIndexCreatorUtil {

	public static void createIfNotExists(long companyId) {
		if (indexExists(companyId)) {
			return;
		}

		CreateIndexRequest createIndexRequest = new CreateIndexRequest(
			getIndexName(companyId));

		createIndexRequest.setMappings(_readFile(_INDEX_MAPPINGS_FILE_NAME));
		createIndexRequest.setSettings(_getSettingsJSON());

		SearchEngineAdapter searchEngineAdapter =
			_searchEngineAdapterSnapshot.get();

		try {
			searchEngineAdapter.execute(createIndexRequest);
		}
		catch (RuntimeException runtimeException) {
			if (!indexExists(companyId)) {
				throw runtimeException;
			}

			if (_log.isDebugEnabled()) {
				_log.debug(runtimeException);
			}
		}
	}

	public static void deleteIfExists(long companyId) {
		if (!indexExists(companyId)) {
			return;
		}

		SearchEngineAdapter searchEngineAdapter =
			_searchEngineAdapterSnapshot.get();

		try {
			searchEngineAdapter.execute(
				new DeleteIndexRequest(getIndexName(companyId)));
		}
		catch (RuntimeException runtimeException) {
			if (indexExists(companyId)) {
				throw runtimeException;
			}

			if (_log.isDebugEnabled()) {
				_log.debug(runtimeException);
			}
		}
	}

	public static String getIndexName(long companyId) {
		IndexNameBuilder indexNameBuilder = _indexNameBuilderSnapshot.get();

		return indexNameBuilder.getIndexName(companyId) + "-mcp-tools";
	}

	public static boolean indexExists(long companyId) {
		IndicesExistsIndexRequest indicesExistsIndexRequest =
			new IndicesExistsIndexRequest(getIndexName(companyId));

		SearchEngineAdapter searchEngineAdapter =
			_searchEngineAdapterSnapshot.get();

		IndicesExistsIndexResponse indicesExistsIndexResponse =
			searchEngineAdapter.execute(indicesExistsIndexRequest);

		return indicesExistsIndexResponse.isExists();
	}

	private static String _getSettingsJSON() {
		String settingsJSON = _readFile(_INDEX_SETTINGS_FILE_NAME);

		try {
			JSONObject settingsJSONObject = JSONFactoryUtil.createJSONObject(
				settingsJSON);

			JSONObject analysisJSONObject = settingsJSONObject.getJSONObject(
				"analysis");

			JSONObject filterJSONObject = analysisJSONObject.getJSONObject(
				"filter");

			JSONObject synonymJSONObject = filterJSONObject.getJSONObject(
				"mcp_tool_synonym");

			synonymJSONObject.put("synonyms", _getSynonymsJSONArray());

			return settingsJSONObject.toString();
		}
		catch (Exception exception) {
			_log.error("Unable to inline the synonyms", exception);

			return settingsJSON;
		}
	}

	private static JSONArray _getSynonymsJSONArray() {
		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		for (String line :
				StringUtil.splitLines(_readFile(_INDEX_SYNONYMS_FILE_NAME))) {

			line = line.trim();

			if (Validator.isNull(line) || line.startsWith(StringPool.POUND)) {
				continue;
			}

			jsonArray.put(line);
		}

		return jsonArray;
	}

	private static String _readFile(String fileName) {
		return StringUtil.read(
			MCPToolIndexCreatorUtil.class, "/META-INF/search/" + fileName);
	}

	private static final String _INDEX_MAPPINGS_FILE_NAME =
		"liferay-mcp-tools-mappings.json";

	private static final String _INDEX_SETTINGS_FILE_NAME =
		"liferay-mcp-tools-settings.json";

	private static final String _INDEX_SYNONYMS_FILE_NAME =
		"liferay-mcp-tools-synonyms.txt";

	private static final Log _log = LogFactoryUtil.getLog(
		MCPToolIndexCreatorUtil.class);

	private static final Snapshot<IndexNameBuilder> _indexNameBuilderSnapshot =
		new Snapshot<>(MCPToolIndexCreatorUtil.class, IndexNameBuilder.class);
	private static final Snapshot<SearchEngineAdapter>
		_searchEngineAdapterSnapshot = new Snapshot<>(
			MCPToolIndexCreatorUtil.class, SearchEngineAdapter.class);

}