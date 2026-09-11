/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.instances.internal.background.task.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.background.task.model.BackgroundTask;
import com.liferay.portal.background.task.service.BackgroundTaskLocalService;
import com.liferay.portal.instances.background.task.constants.PortalInstanceBackgroundTaskExecutorNames;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.encryptor.EncryptorUtil;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.UserNotificationDeliveryConstants;
import com.liferay.portal.kernel.model.UserNotificationEvent;
import com.liferay.portal.kernel.security.auth.Authenticator;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.service.UserNotificationEventLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Luis Ortiz
 */
@RunWith(Arquillian.class)
public class AddPortalInstanceBackgroundTaskExecutorTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@After
	public void tearDown() throws Exception {
		for (UserNotificationEvent userNotificationEvent :
				_getUserNotificationEvents()) {

			_userNotificationEventLocalService.deleteUserNotificationEvent(
				userNotificationEvent);
		}
	}

	@Test
	public void testExecute() throws Exception {
		BackgroundTask backgroundTask = _addBackgroundTask(null, null);

		Assert.assertEquals(
			BackgroundTaskConstants.STATUS_SUCCESSFUL,
			backgroundTask.getStatus());

		Company company = _companyLocalService.getCompanyByWebId(_WEB_ID);

		JSONObject statusMessageJSONObject = _jsonFactory.createJSONObject(
			backgroundTask.getStatusMessage());

		Assert.assertEquals(
			company.getCompanyId(),
			statusMessageJSONObject.getLong("companyId"));

		JSONObject payloadJSONObject = _getPayloadJSONObject();

		Assert.assertEquals(
			BackgroundTaskConstants.STATUS_SUCCESSFUL,
			payloadJSONObject.getInt("status"));
		Assert.assertEquals(
			PortalInstanceBackgroundTaskExecutorNames.
				ADD_PORTAL_INSTANCE_BACKGROUND_TASK_EXECUTOR,
			payloadJSONObject.getString("taskExecutorClassName"));
		Assert.assertEquals(_WEB_ID, payloadJSONObject.getString("webId"));

		_companyLocalService.deleteCompany(company);
	}

	@Test
	public void testExecuteWhenDefaultAdminEmailAddressIsInvalid()
		throws Exception {

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				"com.liferay.portal.background.task.internal.messaging." +
					"BackgroundTaskMessageListener",
				LoggerTestUtil.ERROR)) {

			BackgroundTask backgroundTask = _addBackgroundTask(
				RandomTestUtil.randomString(), null);

			Assert.assertEquals(
				BackgroundTaskConstants.STATUS_FAILED,
				backgroundTask.getStatus());

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertEquals(logEntries.toString(), 1, logEntries.size());

			LogEntry logEntry = logEntries.get(0);

			Assert.assertEquals(
				"Unable to execute background task", logEntry.getMessage());

			Map<String, Serializable> taskContextMap =
				backgroundTask.getTaskContextMap();

			Assert.assertEquals(
				"please-enter-a-valid-email-address",
				taskContextMap.get("errorMessageKey"));

			JSONObject payloadJSONObject = _getPayloadJSONObject();

			Assert.assertEquals(
				"please-enter-a-valid-email-address",
				payloadJSONObject.getString("errorMessageKey"));
			Assert.assertEquals(
				BackgroundTaskConstants.STATUS_FAILED,
				payloadJSONObject.getInt("status"));
			Assert.assertEquals(
				PortalInstanceBackgroundTaskExecutorNames.
					ADD_PORTAL_INSTANCE_BACKGROUND_TASK_EXECUTOR,
				payloadJSONObject.getString("taskExecutorClassName"));
		}

		Company company = _companyLocalService.getCompanyByWebId(_WEB_ID);

		_companyLocalService.deleteCompany(company);
	}

	@Test
	public void testExecuteWhenDefaultAdminPasswordIsEncrypted()
		throws Exception {

		String defaultAdminPassword = RandomTestUtil.randomString();

		Company defaultCompany = _companyLocalService.getCompany(
			PortalUtil.getDefaultCompanyId());

		BackgroundTask backgroundTask = _addBackgroundTask(
			null,
			EncryptorUtil.encrypt(
				defaultCompany.getKeyObj(), defaultAdminPassword));

		Assert.assertEquals(
			BackgroundTaskConstants.STATUS_SUCCESSFUL,
			backgroundTask.getStatus());

		Company company = _companyLocalService.getCompanyByWebId(_WEB_ID);

		Map<String, Serializable> taskContextMap =
			backgroundTask.getTaskContextMap();

		Assert.assertNotEquals(
			defaultAdminPassword, taskContextMap.get("defaultAdminPassword"));

		String emailAddress =
			PropsUtil.get(PropsKeys.DEFAULT_ADMIN_EMAIL_ADDRESS_PREFIX) +
				StringPool.AT + _VIRTUAL_HOSTNAME;

		Assert.assertEquals(
			Authenticator.SUCCESS,
			_userLocalService.authenticateByEmailAddress(
				company.getCompanyId(), emailAddress, defaultAdminPassword,
				new HashMap<>(), new HashMap<>(), new HashMap<>()));

		_companyLocalService.deleteCompany(company);
	}

	private BackgroundTask _addBackgroundTask(
			String defaultAdminEmailAddress, String defaultAdminPassword)
		throws Exception {

		Map<String, Serializable> taskContextMap =
			HashMapBuilder.<String, Serializable>put(
				"active", true
			).put(
				"defaultAdminEmailAddress", () -> defaultAdminEmailAddress
			).put(
				"defaultAdminPassword", () -> defaultAdminPassword
			).put(
				"maxUsers", 0
			).put(
				"mx", _VIRTUAL_HOSTNAME
			).put(
				"siteInitializerKey", StringPool.BLANK
			).put(
				"virtualHostname", _VIRTUAL_HOSTNAME
			).put(
				"webId", _WEB_ID
			).build();

		BackgroundTask backgroundTask =
			_backgroundTaskLocalService.addBackgroundTask(
				TestPropsValues.getUserId(),
				BackgroundTaskConstants.GROUP_ID_DEFAULT,
				"addPortalInstance-" + _WEB_ID,
				PortalInstanceBackgroundTaskExecutorNames.
					ADD_PORTAL_INSTANCE_BACKGROUND_TASK_EXECUTOR,
				taskContextMap, new ServiceContext());

		_backgroundTasks.add(backgroundTask);

		long backgroundTaskId = backgroundTask.getBackgroundTaskId();

		long endTime = System.currentTimeMillis() + 600000;

		while (System.currentTimeMillis() < endTime) {
			backgroundTask = _backgroundTaskLocalService.fetchBackgroundTask(
				backgroundTaskId);

			if ((backgroundTask != null) && backgroundTask.isCompleted()) {
				return backgroundTask;
			}

			Thread.sleep(500);
		}

		throw new AssertionError(
			"Background task " + backgroundTaskId + " did not complete");
	}

	private JSONObject _getPayloadJSONObject() throws Exception {
		List<UserNotificationEvent> userNotificationEvents =
			_getUserNotificationEvents();

		Assert.assertEquals(
			userNotificationEvents.toString(), 1,
			userNotificationEvents.size());

		UserNotificationEvent userNotificationEvent =
			userNotificationEvents.get(0);

		return _jsonFactory.createJSONObject(
			userNotificationEvent.getPayload());
	}

	private List<UserNotificationEvent> _getUserNotificationEvents()
		throws Exception {

		List<UserNotificationEvent> userNotificationEvents = new ArrayList<>();

		for (UserNotificationEvent userNotificationEvent :
				_userNotificationEventLocalService.getUserNotificationEvents(
					TestPropsValues.getUserId(),
					UserNotificationDeliveryConstants.TYPE_WEBSITE)) {

			JSONObject payloadJSONObject = _jsonFactory.createJSONObject(
				userNotificationEvent.getPayload());

			if (Objects.equals(_WEB_ID, payloadJSONObject.getString("webId"))) {
				userNotificationEvents.add(userNotificationEvent);
			}
		}

		return userNotificationEvents;
	}

	private static final String _VIRTUAL_HOSTNAME =
		StringUtil.toLowerCase(RandomTestUtil.randomString()) + ".com";

	private static final String _WEB_ID = StringUtil.toLowerCase(
		RandomTestUtil.randomString());

	@Inject
	private BackgroundTaskLocalService _backgroundTaskLocalService;

	@DeleteAfterTestRun
	private final List<BackgroundTask> _backgroundTasks = new ArrayList<>();

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private JSONFactory _jsonFactory;

	@Inject
	private UserLocalService _userLocalService;

	@Inject
	private UserNotificationEventLocalService
		_userNotificationEventLocalService;

}