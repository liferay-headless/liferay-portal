/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.mcp.server.rest.test.util.MCPServerTestUtil;
import com.liferay.object.model.ObjectEntry;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.util.HTTPTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.URLCodec;
import com.liferay.portal.test.rule.FeatureFlag;

import java.io.Serializable;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alejandro Tardín
 * @author Petteri Karttunen
 */
@FeatureFlag("LPD-63311")
@RunWith(Arquillian.class)
public class ToolSearchResultResourceTest
	extends BaseToolSearchResultResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		MCPServerTestUtil.processBatchEngineUnits();
	}

	@Override
	@Test
	public void testGetToolSearchPage() throws Exception {
		_testGetToolSearchPageWithADefaultLimit();
		_testGetToolSearchPageWithALongSearch();
		_testGetToolSearchPageWithAnAction();
		_testGetToolSearchPageWithAnAmbiguousAssociation();
		_testGetToolSearchPageWithAnAssociation();
		_testGetToolSearchPageWithANestedScope();
		_testGetToolSearchPageWithAQualifier();
		_testGetToolSearchPageWithAPathParameter();
		_testGetToolSearchPageWithARequiredBodyIdentifier();
		_testGetToolSearchPageWithATraversal();
		_testGetToolSearchPageWithATurnedOffOperation();
		_testGetToolSearchPageWithBrowsingAsTheFallback();
		_testGetToolSearchPageWithNothingRelevant();
		_testGetToolSearchPageWithOneResult();
		_testGetToolSearchPageWithTheDefaultProfile();
		_testGetToolSearchPageWithTheToolSearchProfile();
	}

	private String _getName(JSONArray jsonArray, int index) {
		JSONObject jsonObject = jsonArray.getJSONObject(index);

		return jsonObject.getString("name");
	}

	private String _getNames(JSONArray jsonArray) {
		StringBundler sb = new StringBundler(jsonArray.length() * 2);

		for (int i = 0; i < jsonArray.length(); i++) {
			sb.append(_getName(jsonArray, i));
			sb.append(StringPool.SPACE);
		}

		return sb.toString();
	}

	private boolean _hasNameEndingWith(JSONArray jsonArray, String suffix) {
		for (int i = 0; i < jsonArray.length(); i++) {
			if (StringUtil.endsWith(_getName(jsonArray, i), suffix)) {
				return true;
			}
		}

		return false;
	}

	private JSONObject _invoke(String search) throws Exception {
		return HTTPTestUtil.invokeToJSONObject(
			JSONUtil.put(
				"search", search
			).toString(),
			"mcp-server/v1.0/tool-sets/mcp-server-v1.0/tools" +
				"/getToolSearchPage/invoke",
			Http.Method.POST);
	}

	private JSONObject _search(
			boolean includeRequiredInputSchema, int limit, String search)
		throws Exception {

		StringBundler sb = new StringBundler(5);

		sb.append("mcp-server/v1.0/tool-search?search=");
		sb.append(URLCodec.encodeURL(search));

		if (includeRequiredInputSchema) {
			sb.append("&includeRequiredInputSchema=true");
		}

		if (limit > 0) {
			sb.append("&limit=");
			sb.append(limit);
		}

		return HTTPTestUtil.invokeToJSONObject(
			null, sb.toString(), Http.Method.GET);
	}

	private JSONArray _searchToJSONArray(String search) throws Exception {
		JSONObject jsonObject = _search(false, 10, search);

		JSONArray jsonArray = jsonObject.getJSONArray("items");

		Assert.assertNotNull("The search returned no items", jsonArray);
		Assert.assertTrue(
			"The search returned nothing", jsonArray.length() > 0);

		return jsonArray;
	}

	private CompanyConfigurationTemporarySwapper _swapDefaultLimitConfiguration(
			int defaultLimit)
		throws Exception {

		return new CompanyConfigurationTemporarySwapper(
			TestPropsValues.getCompanyId(),
			"com.liferay.mcp.server.rest.internal.configuration." +
				"MCPServerConfiguration",
			HashMapDictionaryBuilder.<String, Object>put(
				"enabled", true
			).put(
				"searchToolDefaultLimit", defaultLimit
			).build());
	}

	private void _testGetToolSearchPageWithADefaultLimit() throws Exception {
		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					_swapDefaultLimitConfiguration(3)) {

			Assert.assertEquals(
				3,
				_search(
					true, 0, "create a site"
				).getInt(
					"totalCount"
				));
		}
	}

	private void _testGetToolSearchPageWithALongSearch() throws Exception {
		StringBundler sb = new StringBundler(400);

		for (int i = 0; i < 400; i++) {
			sb.append("create a blog posting on a site ");
		}

		JSONObject jsonObject = _invoke(sb.toString());

		Assert.assertEquals(
			"A search of 12,800 characters was answered rather than refused",
			"BAD_REQUEST", jsonObject.getString("status"));

		String title = jsonObject.getString("title");

		Assert.assertTrue(
			"The refusal does not say what to do instead: " + title,
			title.contains("one action at a time"));
	}

	private void _testGetToolSearchPageWithAnAction() throws Exception {
		Assert.assertEquals(
			"postCTCollectionPublish",
			_getName(_searchToJSONArray("publish a change list"), 0));
	}

	private void _testGetToolSearchPageWithAnAmbiguousAssociation()
		throws Exception {

		JSONArray jsonArray = _searchToJSONArray(
			"remove a user from an account");

		Assert.assertTrue(
			"An ambiguous association word paired with its direction word " +
				"did not reach an account tool: " + _getNames(jsonArray),
			StringUtil.startsWith(
				_getName(jsonArray, 0), "deleteAccountUserAccount"));
	}

	private void _testGetToolSearchPageWithAnAssociation() throws Exception {
		JSONArray jsonArray = _searchToJSONArray(
			"assign a role to a user account");

		Assert.assertTrue(
			"No association tool came back: " + _getNames(jsonArray),
			_hasNameEndingWith(jsonArray, "Association"));
	}

	private void _testGetToolSearchPageWithANestedScope() throws Exception {
		Assert.assertEquals(
			"postSiteFragmentSetFragment",
			_getName(
				_searchToJSONArray("create a fragment in a fragment set"), 0));
	}

	private void _testGetToolSearchPageWithAPathParameter() throws Exception {
		JSONObject jsonObject = _search(
			true, 1, "create a web content article");

		JSONArray jsonArray = jsonObject.getJSONArray("items");

		JSONObject itemJSONObject = jsonArray.getJSONObject(0);

		JSONArray prerequisitesJSONArray = itemJSONObject.getJSONArray(
			"prerequisites");

		JSONObject prerequisiteJSONObject =
			prerequisitesJSONArray.getJSONObject(0);

		Assert.assertEquals(
			"siteId", prerequisiteJSONObject.getString("parameter"));
		Assert.assertEquals(
			"getSitesPage", prerequisiteJSONObject.getString("toolName"));
	}

	private void _testGetToolSearchPageWithAQualifier() throws Exception {
		Assert.assertEquals(
			"postBlogPostingComment",
			_getName(
				_searchToJSONArray("create a comment on a blog posting"), 0));
	}

	private void _testGetToolSearchPageWithARequiredBodyIdentifier()
		throws Exception {

		JSONObject jsonObject = _search(
			true, 1, "create a web content article");

		JSONArray jsonArray = jsonObject.getJSONArray("items");

		JSONObject itemJSONObject = jsonArray.getJSONObject(0);

		Assert.assertEquals(
			"postSiteStructuredContent", itemJSONObject.getString("name"));

		JSONArray prerequisitesJSONArray = itemJSONObject.getJSONArray(
			"prerequisites");

		Assert.assertNotNull(
			"The best match carries no prerequisites", prerequisitesJSONArray);

		String resolverToolName = null;

		for (int i = 0; i < prerequisitesJSONArray.length(); i++) {
			JSONObject prerequisiteJSONObject =
				prerequisitesJSONArray.getJSONObject(i);

			if (StringUtil.equals(
					"contentStructureId",
					prerequisiteJSONObject.getString("parameter"))) {

				resolverToolName = prerequisiteJSONObject.getString("toolName");
			}
		}

		Assert.assertEquals(
			"Nothing says where to find a contentStructureId: " +
				prerequisitesJSONArray.toString(3),
			"getSiteContentStructuresPage", resolverToolName);
	}

	private void _testGetToolSearchPageWithATraversal() throws Exception {
		Assert.assertEquals(
			"getUserGroupsPage",
			_getName(_searchToJSONArray("list user groups"), 0));
	}

	private void _testGetToolSearchPageWithATurnedOffOperation()
		throws Exception {

		JSONObject jsonObject = HTTPTestUtil.invokeToJSONObject(
			null, _TURNED_OFF_ENDPOINT, Http.Method.GET);

		Assert.assertEquals(
			"A turned-off operation no longer answers the exception the " +
				"catalogue recognises it by: " + jsonObject,
			"UnsupportedOperationException", jsonObject.getString("type"));
		Assert.assertEquals("BAD_REQUEST", jsonObject.getString("status"));
	}

	private void _testGetToolSearchPageWithBrowsingAsTheFallback()
		throws Exception {

		JSONObject jsonObject = HTTPTestUtil.invokeToJSONObject(
			null, "mcp-server/v1.0/tool-sets", Http.Method.GET);

		JSONArray jsonArray = jsonObject.getJSONArray("items");

		Assert.assertTrue(
			"Browsing returns no tool sets", jsonArray.length() > 0);

		JSONObject toolSetJSONObject = jsonArray.getJSONObject(0);

		String toolSetName = toolSetJSONObject.getString("name");

		jsonObject = HTTPTestUtil.invokeToJSONObject(
			null,
			"mcp-server/v1.0/tool-sets/" + toolSetName + "/tool-summaries",
			Http.Method.GET);

		Assert.assertTrue(
			"Browsing " + toolSetName + " returns no tools",
			jsonObject.getInt("totalCount") > 0);
	}

	private void _testGetToolSearchPageWithNothingRelevant() throws Exception {
		JSONObject jsonObject = _search(
			false, 0,
			String.join(StringPool.SPACE, "zyxwvu", "qponml", "kjihgf"));

		JSONArray jsonArray = jsonObject.getJSONArray("items");

		Assert.assertEquals(
			"A search matching nothing answered " + jsonArray, 0,
			jsonArray.length());

		Assert.assertEquals(0, jsonObject.getInt("totalCount"));
	}

	private void _testGetToolSearchPageWithOneResult() throws Exception {
		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					_swapDefaultLimitConfiguration(1)) {

			JSONObject jsonObject = _search(true, 0, "create a site");

			Assert.assertEquals(1, jsonObject.getInt("totalCount"));

			JSONArray jsonArray = jsonObject.getJSONArray("items");

			JSONObject itemJSONObject = jsonArray.getJSONObject(0);

			Assert.assertTrue(
				"A search asked for one result did not carry its arguments",
				itemJSONObject.has("requiredInputSchema"));
		}
	}

	private void _testGetToolSearchPageWithTheDefaultProfile()
		throws Exception {

		ObjectEntry objectEntry =
			MCPServerTestUtil.fetchMCPServerProfileObjectEntry("default");

		Assert.assertNotNull("There is no default profile", objectEntry);

		Map<String, Serializable> values = objectEntry.getValues();

		String tools = GetterUtil.getString(values.get("tools"));

		for (String toolName : _BROWSE_TOOL_NAMES) {
			Assert.assertTrue(
				StringBundler.concat(
					"The default profile no longer offers ", toolName,
					", so a model that finds nothing has nowhere to go: ",
					tools),
				tools.contains(toolName));
		}

		Assert.assertFalse(
			"The default profile offers the search tool: " + tools,
			tools.contains("getToolSearchPage"));
	}

	private void _testGetToolSearchPageWithTheToolSearchProfile()
		throws Exception {

		ObjectEntry objectEntry =
			MCPServerTestUtil.fetchMCPServerProfileObjectEntry("tool-search");

		Assert.assertNotNull(
			"There is no \"tool-search\" profile", objectEntry);

		Map<String, Serializable> values = objectEntry.getValues();

		String tools = GetterUtil.getString(values.get("tools"));

		Assert.assertTrue(
			"The \"tool-search\" profile does not offer the search tool: " +
				tools,
			tools.contains("getToolSearchPage"));

		for (String toolName : _BROWSE_TOOL_NAMES) {
			Assert.assertTrue(
				StringBundler.concat(
					"The \"tool-search\" profile does not offer ", toolName,
					", so a model that finds nothing has nowhere to go: ",
					tools),
				tools.contains(toolName));
		}
	}

	private static final String[] _BROWSE_TOOL_NAMES = {
		"getToolSetToolSetNameToolSummariesPage", "getToolSetsPage"
	};

	/**
	 * Gated on LPD-49855, which no test environment turns on.
	 */
	private static final String _TURNED_OFF_ENDPOINT =
		"oauth-client/v1.0/oauth-client-entries";

}