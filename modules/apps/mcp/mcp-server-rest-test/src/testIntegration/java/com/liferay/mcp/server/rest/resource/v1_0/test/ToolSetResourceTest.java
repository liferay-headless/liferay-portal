/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.mcp.server.rest.client.dto.v1_0.ToolSet;
import com.liferay.mcp.server.rest.client.dto.v1_0.ToolSummary;
import com.liferay.mcp.server.rest.client.pagination.Page;
import com.liferay.mcp.server.rest.client.resource.v1_0.ToolSummaryResource;
import com.liferay.object.constants.ObjectActionExecutorConstants;
import com.liferay.object.constants.ObjectActionTriggerConstants;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectActionLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.test.util.ObjectDefinitionTestUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.vulcan.util.LocalizedMapUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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

		_testNumberOfTools();
		_testNumberOfToolsIsAbsentForAggregate();
		_testNumberOfToolsSurvivesAnUnrelatedObjectDefinition();
		_testNumberOfToolsAfterAddingAStandaloneObjectAction();
	}

	private void _assertToolSet(Predicate<ToolSet> predicate) throws Exception {
		Page<ToolSet> toolSetsPage = toolSetResource.getToolSetsPage();

		Assert.assertTrue(
			ListUtil.exists(
				new ArrayList<>(toolSetsPage.getItems()), predicate));
	}

	private Map<String, Integer> _getNumberOfToolsMap() throws Exception {
		Map<String, Integer> numberOfToolsMap = new HashMap<>();

		Page<ToolSet> toolSetsPage = toolSetResource.getToolSetsPage();

		for (ToolSet toolSet : toolSetsPage.getItems()) {
			if (toolSet.getNumberOfTools() != null) {
				numberOfToolsMap.put(
					toolSet.getName(), toolSet.getNumberOfTools());
			}
		}

		return numberOfToolsMap;
	}

	private ToolSummaryResource _getToolSummaryResource() throws Exception {
		User user = UserTestUtil.getAdminUser(testCompany.getCompanyId());

		return ToolSummaryResource.builder(
		).authentication(
			user.getEmailAddress(), PropsValues.DEFAULT_ADMIN_PASSWORD
		).endpoint(
			testCompany.getVirtualHostname(),
			PortalUtil.getPortalServerPort(false), "http"
		).locale(
			LocaleUtil.getDefault()
		).build();
	}

	private void _testNumberOfTools() throws Exception {
		ToolSummaryResource toolSummaryResource = _getToolSummaryResource();

		Page<ToolSet> toolSetsPage = toolSetResource.getToolSetsPage();

		for (ToolSet toolSet : toolSetsPage.getItems()) {
			if (toolSet.getNumberOfTools() == null) {
				continue;
			}

			Page<ToolSummary> toolSummariesPage =
				toolSummaryResource.getToolSetToolSetNameToolSummariesPage(
					toolSet.getName());

			Assert.assertEquals(
				toolSet.getName(),
				Integer.valueOf((int)toolSummariesPage.getTotalCount()),
				toolSet.getNumberOfTools());
		}
	}

	private void _testNumberOfToolsAfterAddingAStandaloneObjectAction()
		throws Exception {

		ObjectDefinition objectDefinition =
			ObjectDefinitionTestUtil.publishObjectDefinition();

		String restContextPath = objectDefinition.getRESTContextPath();

		String toolSetName = "c-" + restContextPath.substring(3);

		// Cache the count while the object definition carries no action

		Assert.assertNotNull(
			toolSetName, _getNumberOfToolsMap().get(toolSetName));

		_objectActionLocalService.addObjectAction(
			null, TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId(), true, null, null,
			LocalizedMapUtil.getLocalizedMap(RandomTestUtil.randomString()),
			LocalizedMapUtil.getLocalizedMap(RandomTestUtil.randomString()),
			"a" + RandomTestUtil.randomString(4),
			ObjectActionExecutorConstants.KEY_ADD_OBJECT_ENTRY,
			ObjectActionTriggerConstants.KEY_STANDALONE,
			UnicodePropertiesBuilder.create(
				true
			).put(
				"objectDefinitionExternalReferenceCode",
				objectDefinition.getExternalReferenceCode()
			).build(),
			false);

		// A standalone action adds paths, so the count has to follow. Adding a
		// field regenerates the document while counts are deliberately kept,
		// which is what exposes a count the action failed to invalidate.

		String objectFieldName = "a" + RandomTestUtil.randomString(4);

		_objectFieldLocalService.addCustomObjectField(
			null, TestPropsValues.getUserId(), 0,
			objectDefinition.getObjectDefinitionId(),
			ObjectFieldConstants.BUSINESS_TYPE_TEXT,
			ObjectFieldConstants.DB_TYPE_STRING, false, false, null,
			LocalizedMapUtil.getLocalizedMap(objectFieldName), false,
			objectFieldName, null, null, false, false, Collections.emptyList());

		ToolSummaryResource toolSummaryResource = _getToolSummaryResource();

		Page<ToolSummary> toolSummariesPage =
			toolSummaryResource.getToolSetToolSetNameToolSummariesPage(
				toolSetName);

		Assert.assertEquals(
			toolSetName,
			Integer.valueOf((int)toolSummariesPage.getTotalCount()),
			_getNumberOfToolsMap().get(toolSetName));
	}

	private void _testNumberOfToolsIsAbsentForAggregate() throws Exception {

		// The aggregate tool set merges every other document, so a cached
		// count would go stale on any object change, and refreshing it would
		// regenerate the whole aggregate on this endpoint

		Page<ToolSet> toolSetsPage = toolSetResource.getToolSetsPage();

		for (ToolSet toolSet : toolSetsPage.getItems()) {
			if (Objects.equals(toolSet.getName(), "openapi")) {
				Assert.assertNull(toolSet.getNumberOfTools());

				return;
			}
		}
	}

	private void _testNumberOfToolsSurvivesAnUnrelatedObjectDefinition()
		throws Exception {

		Map<String, Integer> numberOfToolsMap = _getNumberOfToolsMap();

		ObjectDefinition objectDefinition =
			ObjectDefinitionTestUtil.publishObjectDefinition();

		String restContextPath = objectDefinition.getRESTContextPath();

		String toolSetName = "c-" + restContextPath.substring(3);

		Map<String, Integer> newNumberOfToolsMap = _getNumberOfToolsMap();

		// Publishing one object definition must invalidate that tool set alone

		for (Map.Entry<String, Integer> entry : numberOfToolsMap.entrySet()) {
			Assert.assertEquals(
				entry.getKey(), entry.getValue(),
				newNumberOfToolsMap.get(entry.getKey()));
		}

		Assert.assertNotNull(toolSetName, newNumberOfToolsMap.get(toolSetName));

		_testNumberOfTools();
	}

	@Inject
	private ObjectActionLocalService _objectActionLocalService;

	@Inject
	private ObjectFieldLocalService _objectFieldLocalService;

}