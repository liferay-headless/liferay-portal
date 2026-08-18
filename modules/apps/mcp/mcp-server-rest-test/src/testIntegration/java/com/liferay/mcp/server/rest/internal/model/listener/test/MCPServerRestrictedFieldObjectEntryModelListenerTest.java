/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.model.listener.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.mcp.server.rest.test.util.MCPServerTestUtil;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.AssertUtils;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.HTTPTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.util.PortalInstances;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alberto Javier Moreno Lage
 */
@FeatureFlags(featureFlags = @FeatureFlag("LPD-63311"))
@RunWith(Arquillian.class)
public class MCPServerRestrictedFieldObjectEntryModelListenerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() {
		MCPServerTestUtil.processBatchEngineUnits();
	}

	@Test
	public void testOnBeforeRemove() throws Exception {
		ObjectEntry mcpServerProfileObjectEntry =
			MCPServerTestUtil.addMCPServerProfileObjectEntry(
				RandomTestUtil.randomString(), RandomTestUtil.randomString());

		ObjectEntry mcpServerProfileToolObjectEntry =
			MCPServerTestUtil.addMCPServerProfileToolObjectEntry(
				mcpServerProfileObjectEntry.getExternalReferenceCode(),
				"getMCPServerProfilesPage", "mcp-server-profiles");

		ObjectEntry mcpServerRestrictedFieldObjectEntry =
			MCPServerTestUtil.addMCPServerRestrictedFieldObjectEntry(
				"description", mcpServerProfileToolObjectEntry);

		AssertUtils.assertFailure(
			ModelListenerException.class,
			"jakarta.validation.ValidationException: Unable to remove a " +
				"restricted field without a delete reason",
			() -> _objectEntryLocalService.deleteObjectEntry(
				mcpServerRestrictedFieldObjectEntry.getObjectEntryId()));

		Assert.assertNotNull(
			_objectEntryLocalService.fetchObjectEntry(
				mcpServerRestrictedFieldObjectEntry.getObjectEntryId()));

		MCPServerTestUtil.deleteMCPServerRestrictedFieldObjectEntry(
			RandomTestUtil.randomString(), mcpServerRestrictedFieldObjectEntry);

		Assert.assertNull(
			_objectEntryLocalService.fetchObjectEntry(
				mcpServerRestrictedFieldObjectEntry.getObjectEntryId()));
	}

	@Test
	public void testOnBeforeRemoveWhenCompanyInDeletionProcess()
		throws Exception {

		ObjectEntry mcpServerProfileObjectEntry =
			MCPServerTestUtil.addMCPServerProfileObjectEntry(
				RandomTestUtil.randomString(), RandomTestUtil.randomString());

		ObjectEntry mcpServerProfileToolObjectEntry =
			MCPServerTestUtil.addMCPServerProfileToolObjectEntry(
				mcpServerProfileObjectEntry.getExternalReferenceCode(),
				"getMCPServerProfilesPage", "mcp-server-profiles");

		ObjectEntry mcpServerRestrictedFieldObjectEntry =
			MCPServerTestUtil.addMCPServerRestrictedFieldObjectEntry(
				"description", mcpServerProfileToolObjectEntry);

		try (SafeCloseable safeCloseable =
				PortalInstances.setCompanyInDeletionProcessWithSafeCloseable(
					TestPropsValues.getCompanyId())) {

			_objectEntryLocalService.deleteObjectEntry(
				mcpServerRestrictedFieldObjectEntry.getObjectEntryId());
		}

		Assert.assertNull(
			_objectEntryLocalService.fetchObjectEntry(
				mcpServerRestrictedFieldObjectEntry.getObjectEntryId()));
	}

	@Test
	public void testRESTAPI() throws Exception {
		ObjectEntry mcpServerProfileObjectEntry =
			MCPServerTestUtil.addMCPServerProfileObjectEntry(
				RandomTestUtil.randomString(), RandomTestUtil.randomString());

		ObjectEntry mcpServerProfileToolObjectEntry =
			MCPServerTestUtil.addMCPServerProfileToolObjectEntry(
				mcpServerProfileObjectEntry.getExternalReferenceCode(),
				"getMCPServerProfilesPage", "mcp-server-profiles");

		JSONObject jsonObject = HTTPTestUtil.invokeToJSONObject(
			JSONUtil.put(
				"fieldName", "description"
			).put(
				"r_mcpServerToolToRestrictedFields_l_mcpServerProfileToolERC",
				mcpServerProfileToolObjectEntry.getExternalReferenceCode()
			).toString(),
			"mcp/server-restricted-fields", Http.Method.POST);

		long objectEntryId = jsonObject.getLong("id");

		Assert.assertNotNull(
			_objectEntryLocalService.fetchObjectEntry(objectEntryId));

		Assert.assertEquals(
			400,
			HTTPTestUtil.invokeToHttpCode(
				null, "mcp/server-restricted-fields/" + objectEntryId,
				Http.Method.DELETE));

		Assert.assertNotNull(
			_objectEntryLocalService.fetchObjectEntry(objectEntryId));

		HTTPTestUtil.invokeToJSONObject(
			JSONUtil.put(
				"deleteReason", "Removed by test."
			).toString(),
			"mcp/server-restricted-fields/" + objectEntryId, Http.Method.PATCH);

		Assert.assertEquals(
			204,
			HTTPTestUtil.invokeToHttpCode(
				null, "mcp/server-restricted-fields/" + objectEntryId,
				Http.Method.DELETE));

		Assert.assertNull(
			_objectEntryLocalService.fetchObjectEntry(objectEntryId));
	}

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

}