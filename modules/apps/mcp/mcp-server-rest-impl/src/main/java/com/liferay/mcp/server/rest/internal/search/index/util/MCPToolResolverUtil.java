/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index.util;

import com.liferay.petra.string.CharPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves operations to list the things a path segment names, like {siteId}.
 *
 * @author Petteri Karttunen
 */
public class MCPToolResolverUtil {

	public static String getPathParameter(String segment) {
		if ((segment.length() > 2) &&
			(segment.charAt(0) == CharPool.OPEN_CURLY_BRACE) &&
			(segment.charAt(segment.length() - 1) ==
				CharPool.CLOSE_CURLY_BRACE)) {

			return segment.substring(1, segment.length() - 1);
		}

		return null;
	}

	public static MCPTool getResolverMCPTool(long companyId, String segment) {
		Map<String, MCPTool> resolverMCPTools = _resolverMCPTools.get(
			companyId);

		if (resolverMCPTools == null) {
			return null;
		}

		return resolverMCPTools.get(segment);
	}

	public static void merge(
		long companyId, Collection<MCPTool> mcpTools,
		Map<String, Integer> toolSetSizes) {

		Map<String, MCPTool> resolverMCPTools = _resolverMCPTools.get(
			companyId);

		if (resolverMCPTools == null) {
			_resolverMCPTools.put(
				companyId, _getResolverMCPTools(mcpTools, toolSetSizes));

			return;
		}

		for (Map.Entry<String, MCPTool> entry :
				_getResolverMCPTools(
					mcpTools, toolSetSizes
				).entrySet()) {

			if (_isPreferred(
					resolverMCPTools.get(entry.getKey()), entry.getValue(),
					toolSetSizes)) {

				resolverMCPTools.put(entry.getKey(), entry.getValue());
			}
		}
	}

	public static void replace(
		long companyId, Collection<MCPTool> mcpTools,
		Map<String, Integer> toolSetSizes) {

		_resolverMCPTools.put(
			companyId, _getResolverMCPTools(mcpTools, toolSetSizes));
	}

	private static Map<String, MCPTool> _getResolverMCPTools(
		Collection<MCPTool> mcpTools, Map<String, Integer> toolSetSizes) {

		Map<String, MCPTool> resolverMCPTools = new HashMap<>();

		for (MCPTool mcpTool : mcpTools) {
			if (Validator.isNotNull(mcpTool.getModifier()) ||
				!Objects.equals(mcpTool.getMethod(), "get") ||
				!StringUtil.endsWith(mcpTool.getToolName(), "Page")) {

				continue;
			}

			String[] segments = StringUtil.split(
				mcpTool.getPath(), CharPool.SLASH);

			if (segments.length == 0) {
				continue;
			}

			String segment = segments[segments.length - 1];

			if (getPathParameter(segment) != null) {
				continue;
			}

			if (_isPreferred(
					resolverMCPTools.get(segment), mcpTool, toolSetSizes)) {

				resolverMCPTools.put(segment, mcpTool);
			}
		}

		return resolverMCPTools;
	}

	private static boolean _isPreferred(
		MCPTool currentMCPTool, MCPTool newMCPTool,
		Map<String, Integer> toolSetSizes) {

		if (currentMCPTool == null) {
			return true;
		}

		if (newMCPTool.isDeprecated() != currentMCPTool.isDeprecated()) {
			return currentMCPTool.isDeprecated();
		}

		String currentPath = currentMCPTool.getPath();
		String path = newMCPTool.getPath();

		int currentPathParameters = StringUtil.count(
			currentPath, CharPool.OPEN_CURLY_BRACE);
		int pathParameters = StringUtil.count(path, CharPool.OPEN_CURLY_BRACE);

		if (pathParameters != currentPathParameters) {
			if (pathParameters < currentPathParameters) {
				return true;
			}

			return false;
		}

		int currentSegments = StringUtil.count(currentPath, CharPool.SLASH);
		int segments = StringUtil.count(path, CharPool.SLASH);

		if (segments != currentSegments) {
			if (segments < currentSegments) {
				return true;
			}

			return false;
		}

		int currentSize = GetterUtil.getInteger(
			toolSetSizes.get(currentMCPTool.getToolSetName()));
		int size = GetterUtil.getInteger(
			toolSetSizes.get(newMCPTool.getToolSetName()));

		if (size != currentSize) {
			if (size > currentSize) {
				return true;
			}

			return false;
		}

		if (path.length() < currentPath.length()) {
			return true;
		}

		return false;
	}

	private static final Map<Long, Map<String, MCPTool>> _resolverMCPTools =
		new ConcurrentHashMap<>();

}