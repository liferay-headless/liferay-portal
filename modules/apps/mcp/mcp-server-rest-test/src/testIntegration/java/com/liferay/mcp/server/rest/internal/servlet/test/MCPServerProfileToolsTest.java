/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.servlet.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.mcp.server.rest.test.util.MCPServerTestUtil;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.HTTPTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

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
public class MCPServerProfileToolsTest {

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
	public void testProfileGrantingSearchWithoutInvokeIsReported()
		throws Exception {

		String name = _addMCPServerProfileName(
			"mcp-server-v1.0 getToolSearchPage",
			"mcp-server-v1.0 getToolSetsPage");

		List<LogEntry> logEntries = _getLogEntries(name);

		Assert.assertEquals(logEntries.toString(), 1, logEntries.size());

		LogEntry logEntry = logEntries.get(0);

		String message = logEntry.getMessage();

		Assert.assertTrue(message, message.contains(name));
		Assert.assertTrue(
			message, message.contains("postToolSetToolSetNameToolInvoke"));
	}

	private String _addMCPServerProfileName(String... tools) throws Exception {
		String name = RandomTestUtil.randomString();

		_mcpServerProfileObjectEntries.add(
			MCPServerTestUtil.addMCPServerProfileObjectEntry(
				RandomTestUtil.randomString(), name, tools));

		return name;
	}

	private List<LogEntry> _getLogEntries(String mcpServerProfileName)
		throws Exception {

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						TestPropsValues.getCompanyId(),
						"com.liferay.mcp.server.rest.internal.configuration." +
							"MCPServerConfiguration",
						HashMapDictionaryBuilder.<String, Object>put(
							"enabled", true
						).build());
			LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				_CLASS_NAME_MCP_SERVER_SERVLET, LoggerTestUtil.ERROR)) {

			HTTPTestUtil.invokeToHttpCode(
				null, "mcp/" + mcpServerProfileName, Http.Method.GET);

			return logCapture.getLogEntries();
		}
	}

	private static final String _CLASS_NAME_MCP_SERVER_SERVLET =
		"com.liferay.mcp.server.rest.internal.servlet.MCPServerServlet";

	private final List<ObjectEntry> _mcpServerProfileObjectEntries =
		new ArrayList<>();

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

}