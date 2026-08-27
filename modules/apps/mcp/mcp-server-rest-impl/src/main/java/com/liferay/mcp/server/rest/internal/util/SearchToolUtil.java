/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.util;

import com.liferay.mcp.server.rest.dto.v1_0.Tool;
import com.liferay.mcp.server.rest.dto.v1_0.ToolSearchResult;
import com.liferay.mcp.server.rest.internal.configuration.MCPServerConfiguration;
import com.liferay.mcp.server.rest.internal.constants.MCPToolClientAdvices;
import com.liferay.mcp.server.rest.internal.search.exception.ToolSearchUnavailableException;
import com.liferay.mcp.server.rest.internal.search.index.util.MCPToolIndexReaderUtil;
import com.liferay.mcp.server.rest.internal.search.index.util.MCPToolIndexWriterUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.capabilities.SearchCapabilities;
import com.liferay.portal.vulcan.pagination.Page;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Petteri Karttunen
 */
public class SearchToolUtil {

	public static Page<ToolSearchResult> getSearchToolsPage(
		HttpServletRequest httpServletRequest,
		boolean includeRequiredInputSchema, int limit, String search) {

		_checkSearchEngineSupport();

		_validateQuery(search);

		long companyId = PortalUtil.getCompanyId(httpServletRequest);

		MCPToolIndexWriterUtil.rebuildIfStale(
			companyId, httpServletRequest, OpenAPIBriefUtil.getChangeCount());

		try {
			return Page.of(
				_getToolSearchResults(
					companyId, httpServletRequest, includeRequiredInputSchema,
					limit, search));
		}
		catch (RuntimeException runtimeException) {
			_log.error("Unable to search the tool index", runtimeException);

			throw new ToolSearchUnavailableException(
				MCPToolClientAdvices.TOOL_SEARCH_UNAVAILABLE);
		}
	}

	private static void _checkSearchEngineSupport() {
		SearchCapabilities searchCapabilities =
			_searchCapabilitiesSnapshot.get();

		if ((searchCapabilities != null) &&
			searchCapabilities.isMCPToolSearchSupported()) {

			return;
		}

		throw new ToolSearchUnavailableException(
			MCPToolClientAdvices.TOOL_SEARCH_UNSUPPORTED);
	}

	private static float _getConfidenceMargin() {
		return _CONFIDENCE_MARGIN_PERCENTAGE / 100F;
	}

	private static int _getLimit(long companyId, int limit) {
		if (limit > 0) {
			return Math.min(limit, _MAX_RESULT_LIMIT);
		}

		try {
			ConfigurationProvider configurationProvider =
				_configurationProviderSnapshot.get();

			MCPServerConfiguration mcpServerConfiguration =
				configurationProvider.getCompanyConfiguration(
					MCPServerConfiguration.class, companyId);

			int defaultLimit = mcpServerConfiguration.searchToolDefaultLimit();

			if (defaultLimit > 0) {
				return Math.min(defaultLimit, _MAX_RESULT_LIMIT);
			}
		}
		catch (Exception exception) {
			_log.error(exception);
		}

		return _DEFAULT_LIMIT;
	}

	private static Map<String, Object> _getRequiredInputSchema(
		HttpServletRequest httpServletRequest, String toolName,
		String toolSetName) {

		try {
			Tool tool = ToolSetUtil.getTool(
				httpServletRequest, false, toolName, toolSetName);

			Map<String, ?> inputSchema = tool.getInputSchema();

			if (inputSchema == null) {
				return null;
			}

			Map<String, Object> requiredInputSchema =
				ToolSetUtil.toRequiredPropertiesOnly(inputSchema);

			if (toolName.endsWith("Page")) {
				Map<String, ?> properties = (Map<String, ?>)inputSchema.get(
					"properties");

				if ((properties != null) && properties.containsKey("fields")) {
					if (requiredInputSchema == null) {
						requiredInputSchema =
							HashMapBuilder.<String, Object>put(
								"properties", new HashMap<String, Object>()
							).put(
								"type", "object"
							).build();
					}

					Map<String, Object> requiredProperties =
						(Map<String, Object>)requiredInputSchema.get(
							"properties");

					if (requiredProperties == null) {
						requiredProperties = new HashMap<>();

						requiredInputSchema.put(
							"properties", requiredProperties);
					}

					requiredProperties.put("fields", properties.get("fields"));
				}
			}

			return requiredInputSchema;
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			return null;
		}
	}

	private static List<ToolSearchResult> _getToolSearchResults(
		long companyId, HttpServletRequest httpServletRequest,
		boolean includeRequiredInputSchema, int limit, String search) {

		List<ToolSearchResult> toolSearchResults =
			MCPToolIndexReaderUtil.search(
				companyId, includeRequiredInputSchema,
				_getLimit(companyId, limit), search, _getConfidenceMargin());

		if (includeRequiredInputSchema && !toolSearchResults.isEmpty()) {
			ToolSearchResult toolSearchResult = toolSearchResults.get(0);

			toolSearchResult.setRequiredInputSchema(
				() -> _getRequiredInputSchema(
					httpServletRequest, toolSearchResult.getName(),
					toolSearchResult.getToolSetName()));
		}

		return toolSearchResults;
	}

	private static void _validateQuery(String query) {
		if (Validator.isBlank(query)) {
			throw new IllegalArgumentException(
				StringBundler.concat(
					"The search is empty. Say what the user wants to do, as ",
					"\"create a blog entry\", and search for one action at a ",
					"time."));
		}

		if (query.length() > _MAX_SEARCH_LENGTH) {
			throw new IllegalArgumentException(
				StringBundler.concat(
					"The search is ", query.length(),
					" characters long and the most this tool answers is ",
					_MAX_SEARCH_LENGTH,
					". Search for one action at a time, phrased the way a ",
					"person would ask for it, as in \"create a blog entry\". ",
					"A request made of several steps needs one search for ",
					"each."));
		}
	}

	private static final int _CONFIDENCE_MARGIN_PERCENTAGE = 40;

	private static final int _DEFAULT_LIMIT = 10;

	private static final int _MAX_RESULT_LIMIT = 100;

	private static final int _MAX_SEARCH_LENGTH = 500;

	private static final Log _log = LogFactoryUtil.getLog(SearchToolUtil.class);

	private static final Snapshot<ConfigurationProvider>
		_configurationProviderSnapshot = new Snapshot<>(
			SearchToolUtil.class, ConfigurationProvider.class);
	private static final Snapshot<SearchCapabilities>
		_searchCapabilitiesSnapshot = new Snapshot<>(
			SearchToolUtil.class, SearchCapabilities.class);

}