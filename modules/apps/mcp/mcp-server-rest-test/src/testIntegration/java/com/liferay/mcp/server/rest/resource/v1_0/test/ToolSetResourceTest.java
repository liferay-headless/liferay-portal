/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.mcp.server.rest.client.dto.v1_0.ToolSet;
import com.liferay.mcp.server.rest.client.pagination.Page;
import com.liferay.object.constants.ObjectActionExecutorConstants;
import com.liferay.object.constants.ObjectActionTriggerConstants;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.constants.ObjectRelationshipConstants;
import com.liferay.object.field.util.ObjectFieldUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.object.test.util.ObjectActionTestUtil;
import com.liferay.object.test.util.ObjectDefinitionTestUtil;
import com.liferay.object.test.util.ObjectRelationshipTestUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.HTTPTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alejandro Tardín
 */
@FeatureFlag("LPD-63311")
@RunWith(Arquillian.class)
public class ToolSetResourceTest extends BaseToolSetResourceTestCase {

	@Override
	@Test
	public void testGetToolSetsPage() throws Exception {
		_assertToolSet(
			toolSet ->
				Objects.equals(toolSet.getName(), "mcp-server-v1.0") &&
				Validator.isNotNull(toolSet.getDescription()));

		ObjectDefinition objectDefinition =
			ObjectDefinitionTestUtil.publishObjectDefinition();

		String restContextPath = objectDefinition.getRESTContextPath();

		_assertToolSet(
			toolSet -> Objects.equals(
				"c-" + restContextPath.substring(3), toolSet.getName()));

		_assertToolSet(
			toolSet -> Objects.equals(
				toolSet.getName(), "headless-commerce-admin-pricing-v1.0"));
		_assertToolSet(
			toolSet -> Objects.equals(
				toolSet.getName(), "headless-commerce-admin-pricing-v2.0"));

		_company = CompanyTestUtil.addCompany();

		User user = UserTestUtil.getAdminUser(_company.getCompanyId());

		ObjectDefinition companyObjectDefinition =
			ObjectDefinitionTestUtil.addCustomObjectDefinition(
				ObjectDefinitionTestUtil.getRandomName(), user.getUserId());

		companyObjectDefinition =
			_objectDefinitionLocalService.publishCustomObjectDefinition(
				user.getUserId(),
				companyObjectDefinition.getObjectDefinitionId());

		String companyRESTContextPath =
			companyObjectDefinition.getRESTContextPath();

		String companyToolSetName = "c-" + companyRESTContextPath.substring(3);

		Page<ToolSet> toolSetsPage = toolSetResource.getToolSetsPage();

		Assert.assertFalse(
			ListUtil.exists(
				new ArrayList<>(toolSetsPage.getItems()),
				toolSet -> Objects.equals(
					companyToolSetName, toolSet.getName())));

		String toolSetName = "c-" + restContextPath.substring(3);

		HTTPTestUtil.customize(
		).withCredentials(
			user.getEmailAddress(), PropsValues.DEFAULT_ADMIN_PASSWORD
		).apply(
			() -> {
				List<String> toolSetNames = _getToolSetNames(
					HTTPTestUtil.invokeToString(
						null, "mcp-server/v1.0/tool-sets",
						HashMapBuilder.put(
							"Host", _company.getVirtualHostname()
						).build(),
						Http.Method.GET));

				Assert.assertTrue(
					toolSetNames.toString(),
					toolSetNames.contains(companyToolSetName));
				Assert.assertFalse(
					toolSetNames.toString(),
					toolSetNames.contains(toolSetName));
			}
		);
	}

	@Test
	public void testGetToolSetsPageNumberOfTools() throws Exception {
		_assertNumberOfTools("mcp-server-v1.0");

		// An object definition exposes its own operations, one per standalone
		// action, and one set per relationship it owns or shares

		ObjectDefinition objectDefinition1 =
			ObjectDefinitionTestUtil.publishObjectDefinition();
		ObjectDefinition objectDefinition2 =
			ObjectDefinitionTestUtil.publishObjectDefinition();

		ObjectActionTestUtil.addObjectAction(
			ObjectActionExecutorConstants.KEY_WEBHOOK,
			ObjectActionTriggerConstants.KEY_STANDALONE, objectDefinition1,
			UnicodePropertiesBuilder.put(
				"secret", RandomTestUtil.randomString()
			).put(
				"url", "https://" + RandomTestUtil.randomString() + ".com"
			).build());
		ObjectRelationshipTestUtil.addObjectRelationship(
			_objectRelationshipLocalService, objectDefinition1,
			objectDefinition2);
		ObjectRelationshipTestUtil.addObjectRelationship(
			_objectRelationshipLocalService, objectDefinition1,
			objectDefinition2,
			ObjectRelationshipConstants.DELETION_TYPE_CASCADE,
			StringUtil.randomId(),
			ObjectRelationshipConstants.TYPE_MANY_TO_MANY);

		_assertNumberOfTools(_getToolSetName(objectDefinition1));
		_assertNumberOfTools(_getToolSetName(objectDefinition2));

		// A site scoped object definition exposes the scoped operations
		// instead of the company ones

		_assertNumberOfTools(
			_getToolSetName(
				ObjectDefinitionTestUtil.publishObjectDefinition(
					Collections.singletonList(
						ObjectFieldUtil.createObjectField(
							ObjectFieldConstants.BUSINESS_TYPE_TEXT,
							ObjectFieldConstants.DB_TYPE_STRING,
							"a" + RandomTestUtil.randomString())),
					ObjectDefinitionConstants.SCOPE_SITE)));
	}

	private void _assertNumberOfTools(String toolSetName) throws Exception {
		ToolSet toolSet = _getToolSet(toolSetName);

		Assert.assertEquals(
			_getToolSummariesTotalCount(toolSetName),
			toolSet.getNumberOfTools());
	}

	private void _assertToolSet(Predicate<ToolSet> predicate) throws Exception {
		Page<ToolSet> toolSetsPage = toolSetResource.getToolSetsPage();

		Assert.assertTrue(
			ListUtil.exists(
				new ArrayList<>(toolSetsPage.getItems()), predicate));
	}

	private ToolSet _getToolSet(String toolSetName) throws Exception {
		Page<ToolSet> toolSetsPage = toolSetResource.getToolSetsPage();

		for (ToolSet toolSet : toolSetsPage.getItems()) {
			if (Objects.equals(toolSetName, toolSet.getName())) {
				return toolSet;
			}
		}

		throw new AssertionError(
			"No tool-set was found with name \"" + toolSetName + "\"");
	}

	private String _getToolSetName(ObjectDefinition objectDefinition) {
		String restContextPath = objectDefinition.getRESTContextPath();

		return "c-" + restContextPath.substring(3);
	}

	private List<String> _getToolSetNames(String json) throws Exception {
		List<String> toolSetNames = new ArrayList<>();

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(json);

		JSONArray itemsJSONArray = jsonObject.getJSONArray("items");

		for (int i = 0; i < itemsJSONArray.length(); i++) {
			JSONObject itemJSONObject = itemsJSONArray.getJSONObject(i);

			toolSetNames.add(itemJSONObject.getString("name"));
		}

		return toolSetNames;
	}

	private Integer _getToolSummariesTotalCount(String toolSetName)
		throws Exception {

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
			HTTPTestUtil.invokeToString(
				null,
				"mcp-server/v1.0/tool-sets/" + toolSetName + "/tool-summaries",
				Http.Method.GET));

		return jsonObject.getInt("totalCount");
	}

	@DeleteAfterTestRun
	private Company _company;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

}