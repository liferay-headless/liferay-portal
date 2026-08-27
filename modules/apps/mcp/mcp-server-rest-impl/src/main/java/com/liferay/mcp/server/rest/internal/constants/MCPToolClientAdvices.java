/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.constants;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.HashMapBuilder;

import java.util.Map;

/**
 * @author Petteri Karttunen
 */
public class MCPToolClientAdvices {

	public static final String BATCH_HINT = StringBundler.concat(
		"Takes many entities in one call, so use it rather than calling the ",
		"single-entity operation repeatedly. It answers 202 with an import ",
		"task, not a result: nothing is written yet and 202 is not success. ",
		"Before reporting the work done, read executeStatus and failedItems ",
		"by invoking getImportTask in headless-batch-engine-v1.0, passing the ",
		"id this returned.");

	public static final String BROWSE_INSTEAD = StringBundler.concat(
		"Browse instead: \"getToolSetsPage\" lists every tool set and ",
		"\"getToolSetToolSetNameToolSummariesPage\" lists the operations of ",
		"one.");

	public static final String EXTERNAL_REFERENCE_CODE_NOTE =
		StringBundler.concat(
			"A generated code rather than a name. Read it from the ",
			"externalReferenceCode field of the listing named here.");

	public static final String TOOL_SEARCH_UNAVAILABLE =
		"Tool search is unavailable on this instance right now. " +
			BROWSE_INSTEAD;

	public static final String TOOL_SEARCH_UNSUPPORTED = StringBundler.concat(
		"Tool search is unavailable on this instance because it needs ",
		"Elasticsearch or OpenSearch. ", BROWSE_INSTEAD);

	public static final Map<String, String> parameterHints = HashMapBuilder.put(
		"assetLibraryId",
		StringBundler.concat(
			"The asset library's key, its numeric id or its external ",
			"reference code. The key is case-sensitive.")
	).put(
		"siteId",
		StringBundler.concat(
			"The site's key, its numeric id or its external reference code. ",
			"The key is usually the site's name and is case-sensitive, so try ",
			"it before listing sites.")
	).build();
	public static final Map<String, String> toolHints = HashMapBuilder.put(
		"postObjectDefinition",
		StringBundler.concat(
			"Once published, this object's entries get their own tools, named ",
			"after the object rather than after Liferay: to add one, search ",
			"for the object's own name, as in \"create a pet store\", not ",
			"\"create an object entry\".")
	).put(
		"postObjectDefinitionPublish",
		StringBundler.concat(
			"Publishing registers a tool set for this object's entries, named ",
			"after the object. To add one, search for the object's own name, ",
			"as in \"create a pet store\", not \"create an object entry\".")
	).build();

}