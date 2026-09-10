/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.servlet.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.mcp.server.rest.test.util.MCPServerTestUtil;
import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Petteri Karttunen
 */
@FeatureFlags(featureFlags = @FeatureFlag("LPD-63311"))
@RunWith(Arquillian.class)
public class MCPServerInstructionsTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() {
		MCPServerTestUtil.processBatchEngineUnits();
	}

	@After
	public void tearDown() throws Exception {
		for (ObjectEntry objectEntry : _mcpServerProfileObjectEntries) {
			_objectEntryLocalService.deleteObjectEntry(
				objectEntry.getObjectEntryId());
		}

		_mcpServerProfileObjectEntries.clear();
	}

	@Test
	public void testInstructionsAreScopedToTheProfile() throws Exception {
		String instructions = RandomTestUtil.randomString();
		String otherInstructions = RandomTestUtil.randomString();

		String mcpServerProfileName = _addMCPServerProfileName(instructions);
		String otherMCPServerProfileName = _addMCPServerProfileName(
			otherInstructions);

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					_swapMCPServerEnabled()) {

			Assert.assertEquals(
				instructions, _getServerInstructions(mcpServerProfileName));
			Assert.assertEquals(
				otherInstructions,
				_getServerInstructions(otherMCPServerProfileName));
		}
	}

	@Test
	public void testInstructionsAreServedToTheClient() throws Exception {
		String instructions = RandomTestUtil.randomString();

		String mcpServerProfileName = _addMCPServerProfileName(instructions);

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					_swapMCPServerEnabled()) {

			Assert.assertEquals(
				instructions, _getServerInstructions(mcpServerProfileName));
		}
	}

	@Test
	public void testInstructionsIsBlankWhenTheProfileHasNone()
		throws Exception {

		String mcpServerProfileName = _addMCPServerProfileName(null);

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					_swapMCPServerEnabled()) {

			Assert.assertTrue(
				Validator.isBlank(
					_getServerInstructions(mcpServerProfileName)));
		}
	}

	private String _addMCPServerProfileName(String instructions)
		throws Exception {

		String name = RandomTestUtil.randomString();

		ObjectEntry objectEntry =
			MCPServerTestUtil.addMCPServerProfileObjectEntry(
				RandomTestUtil.randomString(), name,
				"mcp-server-v1.0 getToolSetsPage");

		_mcpServerProfileObjectEntries.add(objectEntry);

		if (instructions == null) {
			return name;
		}

		_objectEntryLocalService.updateObjectEntry(
			TestPropsValues.getUserId(), objectEntry.getObjectEntryId(),
			ObjectEntryFolderConstants.PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
			HashMapBuilder.<String, Serializable>putAll(
				objectEntry.getValues()
			).put(
				"instructions", instructions
			).build(),
			ServiceContextTestUtil.getServiceContext());

		return name;
	}

	private McpSyncClient _getMcpSyncClient(String mcpServerProfileName) {
		String userNameAndPassword =
			"test@liferay.com:" + PropsValues.DEFAULT_ADMIN_PASSWORD;

		return McpClient.sync(
			HttpClientStreamableHttpTransport.builder(
				"http://localhost:" + PortalUtil.getPortalServerPort(false) +
					"/o/"
			).customizeRequest(
				builder -> builder.header(
					"Authorization",
					"Basic " + Base64.encode(userNameAndPassword.getBytes()))
			).endpoint(
				"mcp/" + mcpServerProfileName
			).build()
		).build();
	}

	private String _getServerInstructions(String mcpServerProfileName)
		throws Exception {

		McpSyncClient mcpSyncClient = _getMcpSyncClient(mcpServerProfileName);

		try {
			McpSchema.InitializeResult initializeResult =
				mcpSyncClient.initialize();

			return initializeResult.instructions();
		}
		finally {
			mcpSyncClient.closeGracefully();
		}
	}

	private CompanyConfigurationTemporarySwapper _swapMCPServerEnabled()
		throws Exception {

		return new CompanyConfigurationTemporarySwapper(
			TestPropsValues.getCompanyId(),
			"com.liferay.mcp.server.rest.internal.configuration." +
				"MCPServerConfiguration",
			HashMapDictionaryBuilder.<String, Object>put(
				"enabled", true
			).build());
	}

	private final List<ObjectEntry> _mcpServerProfileObjectEntries =
		new ArrayList<>();

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

}