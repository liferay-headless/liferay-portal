/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.portal.instances.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.headless.portal.instances.client.dto.v1_0.PortalInstanceOperation;
import com.liferay.headless.portal.instances.client.problem.Problem;
import com.liferay.headless.portal.instances.client.resource.v1_0.PortalInstanceOperationResource;
import com.liferay.headless.portal.instances.resource.v1_0.test.util.PortalInstanceOperationTestUtil;
import com.liferay.portal.instances.background.task.constants.PortalInstanceBackgroundTaskConstants;
import com.liferay.portal.instances.background.task.constants.PortalInstanceBackgroundTaskExecutorNames;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.io.Serializable;

import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Luis Ortiz
 */
@RunWith(Arquillian.class)
public class PortalInstanceOperationResourceTest
	extends BasePortalInstanceOperationResourceTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Override
	@Test
	public void testGetPortalInstanceOperation() throws Exception {
		long backgroundTaskId = _testGetPortalInstanceOperationWhenFailed();

		_testGetPortalInstanceOperationWithUnrelatedBackgroundTask();
		_testGetPortalInstanceOperationWithNonexistentBackgroundTask();
		_testGetPortalInstanceOperationWithoutOmniadminPermission(
			backgroundTaskId);
	}

	private long _addPortalInstance(
			String defaultAdminEmailAddress, String webId)
		throws Exception {

		BackgroundTask backgroundTask =
			_backgroundTaskManager.addBackgroundTask(
				TestPropsValues.getUserId(),
				BackgroundTaskConstants.GROUP_ID_DEFAULT,
				"addPortalInstance-" + webId,
				PortalInstanceBackgroundTaskExecutorNames.
					ADD_PORTAL_INSTANCE_BACKGROUND_TASK_EXECUTOR,
				HashMapBuilder.<String, Serializable>put(
					PortalInstanceBackgroundTaskConstants.ACTIVE, true
				).put(
					PortalInstanceBackgroundTaskConstants.
						DEFAULT_ADMIN_EMAIL_ADDRESS,
					defaultAdminEmailAddress
				).put(
					PortalInstanceBackgroundTaskConstants.
						DEFAULT_ADMIN_FIRST_NAME,
					RandomTestUtil.randomString()
				).put(
					PortalInstanceBackgroundTaskConstants.
						DEFAULT_ADMIN_LAST_NAME,
					RandomTestUtil.randomString()
				).put(
					PortalInstanceBackgroundTaskConstants.MAX_USERS, 0
				).put(
					PortalInstanceBackgroundTaskConstants.MX, webId + ".com"
				).put(
					PortalInstanceBackgroundTaskConstants.VIRTUAL_HOSTNAME,
					webId + ".com"
				).put(
					PortalInstanceBackgroundTaskConstants.WEB_ID, webId
				).build(),
				new ServiceContext());

		return backgroundTask.getBackgroundTaskId();
	}

	private void _assertUnableToExecuteBackgroundTask(LogCapture logCapture) {
		List<LogEntry> logEntries = logCapture.getLogEntries();

		Assert.assertEquals(logEntries.toString(), 1, logEntries.size());

		LogEntry logEntry = logEntries.get(0);

		Assert.assertEquals(
			"Unable to execute background task", logEntry.getMessage());
	}

	private long _testGetPortalInstanceOperationWhenFailed() throws Exception {
		String defaultAdminEmailAddress = StringUtil.toLowerCase(
			RandomTestUtil.randomString());

		String webId = StringUtil.toLowerCase(RandomTestUtil.randomString());

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				_CLASS_NAME_BACKGROUND_TASK_MESSAGE_LISTENER,
				LoggerTestUtil.ERROR)) {

			long backgroundTaskId = _addPortalInstance(
				defaultAdminEmailAddress, webId);

			PortalInstanceOperation portalInstanceOperation =
				PortalInstanceOperationTestUtil.waitForCompletion(
					backgroundTaskId, portalInstanceOperationResource);

			_assertUnableToExecuteBackgroundTask(logCapture);

			Assert.assertEquals(
				PortalInstanceOperation.Status.FAILED,
				portalInstanceOperation.getStatus());
			Assert.assertEquals(
				PortalInstanceOperation.OperationType.ADD,
				portalInstanceOperation.getOperationType());
			Assert.assertEquals(
				webId, portalInstanceOperation.getPortalInstanceId());

			_company = _companyLocalService.getCompanyByWebId(webId);

			Assert.assertEquals(
				_language.get(
					LocaleUtil.getDefault(),
					"please-enter-a-valid-email-address"),
				portalInstanceOperation.getErrorMessage());

			return backgroundTaskId;
		}
	}

	private void _testGetPortalInstanceOperationWithNonexistentBackgroundTask()
		throws Exception {

		try {
			portalInstanceOperationResource.getPortalInstanceOperation(
				RandomTestUtil.randomLong());

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("NOT_FOUND", problem.getStatus());
		}
	}

	private void _testGetPortalInstanceOperationWithoutOmniadminPermission(
			long backgroundTaskId)
		throws Exception {

		User user = UserTestUtil.addUser(testCompany, "test");

		PortalInstanceOperationResource userPortalInstanceOperationResource =
			PortalInstanceOperationResource.builder(
			).authentication(
				user.getEmailAddress(), "test"
			).endpoint(
				testCompany.getVirtualHostname(),
				PortalUtil.getPortalServerPort(false), "http"
			).locale(
				LocaleUtil.getDefault()
			).build();

		assertHttpResponseStatusCode(
			404,
			userPortalInstanceOperationResource.
				getPortalInstanceOperationHttpResponse(backgroundTaskId));
	}

	private void _testGetPortalInstanceOperationWithUnrelatedBackgroundTask()
		throws Exception {

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				_CLASS_NAME_BACKGROUND_TASK_MESSAGE_LISTENER,
				LoggerTestUtil.ERROR)) {

			BackgroundTask backgroundTask =
				_backgroundTaskManager.addBackgroundTask(
					TestPropsValues.getUserId(),
					BackgroundTaskConstants.GROUP_ID_DEFAULT,
					RandomTestUtil.randomString(),
					RandomTestUtil.randomString(),
					new HashMap<String, Serializable>(), new ServiceContext());

			long backgroundTaskId = backgroundTask.getBackgroundTaskId();

			PortalInstanceOperationTestUtil.waitForCompletion(
				backgroundTaskId, _backgroundTaskManager);

			_assertUnableToExecuteBackgroundTask(logCapture);

			try {
				portalInstanceOperationResource.getPortalInstanceOperation(
					backgroundTaskId);

				Assert.fail();
			}
			catch (Problem.ProblemException problemException) {
				Problem problem = problemException.getProblem();

				Assert.assertEquals("NOT_FOUND", problem.getStatus());
			}
		}
	}

	private static final String _CLASS_NAME_BACKGROUND_TASK_MESSAGE_LISTENER =
		"com.liferay.portal.background.task.internal.messaging." +
			"BackgroundTaskMessageListener";

	@Inject
	private BackgroundTaskManager _backgroundTaskManager;

	@DeleteAfterTestRun
	private Company _company;

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private Language _language;

}