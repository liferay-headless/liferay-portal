/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.rest.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.launch.constants.LaunchStatusConstants;
import com.liferay.launch.rest.client.dto.v1_0.LaunchPublishStatus;
import com.liferay.launch.rest.client.resource.v1_0.LaunchPublishStatusResource;
import com.liferay.launch.set.publisher.LaunchSetPublisher;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.background.task.service.BackgroundTaskLocalService;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.util.FeatureFlagTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;

import java.io.Serializable;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alberto Moreno
 */
@FeatureFlag("LPD-72278")
@RunWith(Arquillian.class)
@Sync(cleanTransaction = true)
public class LaunchPublishStatusResourceTest
	extends BaseLaunchPublishStatusResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		UserTestUtil.setUser(TestPropsValues.getUser());
	}

	@After
	@Override
	public void tearDown() throws Exception {
		for (BackgroundTask backgroundTask :
				_backgroundTaskManager.getBackgroundTasks(
					BackgroundTaskConstants.GROUP_ID_DEFAULT,
					_TASK_EXECUTOR_CLASS_NAME)) {

			_backgroundTaskManager.deleteBackgroundTask(
				backgroundTask.getBackgroundTaskId());
		}

		super.tearDown();
	}

	@Override
	@Test
	public void testGetLaunchPublishStatus() throws Exception {
		_testGetLaunchPublishStatus();
		_testGetLaunchPublishStatusWithFailedPublish();
		_testGetLaunchPublishStatusWithFeatureFlagDisabled();
		_testGetLaunchPublishStatusWithMissingLaunch();
		_testGetLaunchPublishStatusWithoutViewPermission();
		_testGetLaunchPublishStatusWithRunningPublish();
		_testGetLaunchPublishStatusWithScheduledLaunch();
	}

	private ObjectEntry _addLaunchSetObjectEntry() throws Exception {
		return _addObjectEntry(
			"LaunchSet",
			HashMapBuilder.<String, Serializable>put(
				"description", RandomTestUtil.randomString()
			).put(
				"name", RandomTestUtil.randomString()
			).build());
	}

	private ObjectEntry _addObjectEntry(
			String objectDefinitionName, Map<String, Serializable> values)
		throws Exception {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.getObjectDefinition(
				TestPropsValues.getCompanyId(), objectDefinitionName);

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				testGroup.getGroupId(), TestPropsValues.getUserId());

		serviceContext.setAddGuestPermissions(false);
		serviceContext.setWorkflowAction(WorkflowConstants.ACTION_SAVE_DRAFT);

		return _objectEntryLocalService.addObjectEntry(
			0, TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId(), 0,
			LocaleUtil.toLanguageId(LocaleUtil.getSiteDefault()), values,
			serviceContext);
	}

	private void _assertLaunchPublishStatus(
			String expectedErrorMessage, String expectedLabel,
			String expectedLabel_i18n, long launchSetId)
		throws Exception {

		LaunchPublishStatus launchPublishStatus =
			launchPublishStatusResource.getLaunchPublishStatus(launchSetId);

		Assert.assertEquals(
			expectedErrorMessage, launchPublishStatus.getErrorMessage());
		Assert.assertEquals(expectedLabel, launchPublishStatus.getLabel());
		Assert.assertEquals(
			expectedLabel_i18n, launchPublishStatus.getLabel_i18n());
	}

	private void _testGetLaunchPublishStatus() throws Exception {
		ObjectEntry launchSetObjectEntry = _addLaunchSetObjectEntry();

		long launchSetId = launchSetObjectEntry.getObjectEntryId();

		_assertLaunchPublishStatus(
			null, LaunchStatusConstants.LABEL_IN_PROGRESS, "In Progress",
			launchSetId);

		_launchSetPublisher.publish(launchSetId, TestPropsValues.getUserId());

		_assertLaunchPublishStatus(
			null, LaunchStatusConstants.LABEL_PUBLISHED, "Published",
			launchSetId);
	}

	private void _testGetLaunchPublishStatusWithFailedPublish()
		throws Exception {

		ObjectEntry launchSetObjectEntry = _addLaunchSetObjectEntry();

		long launchSetId = launchSetObjectEntry.getObjectEntryId();

		String className = RandomTestUtil.randomString();

		_addObjectEntry(
			"LaunchEntry",
			HashMapBuilder.<String, Serializable>put(
				"className", className
			).put(
				"classPK", RandomTestUtil.randomInt()
			).put(
				"r_launchSetToLaunchEntries_c_launchSetId", launchSetId
			).build());

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				_TASK_EXECUTOR_CLASS_NAME, LoggerTestUtil.ERROR)) {

			_launchSetPublisher.publish(
				launchSetId, TestPropsValues.getUserId());

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertEquals(logEntries.toString(), 1, logEntries.size());
		}

		_assertLaunchPublishStatus(
			"No launch entry manager exists for " + className,
			LaunchStatusConstants.LABEL_FAILED, "Failed", launchSetId);
	}

	private void _testGetLaunchPublishStatusWithFeatureFlagDisabled()
		throws Exception {

		ObjectEntry launchSetObjectEntry = _addLaunchSetObjectEntry();

		try (SafeCloseable safeCloseable =
				FeatureFlagTestUtil.setFeatureFlagsWithSafeCloseable(
					false, "LPD-72278")) {

			assertHttpResponseStatusCode(
				404,
				launchPublishStatusResource.getLaunchPublishStatusHttpResponse(
					launchSetObjectEntry.getObjectEntryId()));
		}
	}

	private void _testGetLaunchPublishStatusWithMissingLaunch()
		throws Exception {

		assertHttpResponseStatusCode(
			404,
			launchPublishStatusResource.getLaunchPublishStatusHttpResponse(
				RandomTestUtil.randomLong()));

		ObjectEntry launchEntryObjectEntry = _addObjectEntry(
			"LaunchEntry",
			HashMapBuilder.<String, Serializable>put(
				"className", RandomTestUtil.randomString()
			).put(
				"classPK", RandomTestUtil.randomInt()
			).put(
				"r_launchSetToLaunchEntries_c_launchSetId",
				() -> {
					ObjectEntry launchSetObjectEntry =
						_addLaunchSetObjectEntry();

					return launchSetObjectEntry.getObjectEntryId();
				}
			).build());

		assertHttpResponseStatusCode(
			404,
			launchPublishStatusResource.getLaunchPublishStatusHttpResponse(
				launchEntryObjectEntry.getObjectEntryId()));
	}

	private void _testGetLaunchPublishStatusWithRunningPublish()
		throws Exception {

		ObjectEntry launchSetObjectEntry = _addLaunchSetObjectEntry();

		long launchSetId = launchSetObjectEntry.getObjectEntryId();

		_launchSetPublisher.publish(launchSetId, TestPropsValues.getUserId());

		BackgroundTask backgroundTask =
			_launchSetPublisher.fetchPublishBackgroundTask(launchSetId);

		com.liferay.portal.background.task.model.BackgroundTask
			backgroundTaskModel = _backgroundTaskLocalService.getBackgroundTask(
				backgroundTask.getBackgroundTaskId());

		backgroundTaskModel.setCompleted(false);
		backgroundTaskModel.setStatus(
			BackgroundTaskConstants.STATUS_IN_PROGRESS);

		_backgroundTaskLocalService.updateBackgroundTask(backgroundTaskModel);

		_assertLaunchPublishStatus(
			null, LaunchStatusConstants.LABEL_PUBLISHING, "Publishing",
			launchSetId);
	}

	private void _testGetLaunchPublishStatusWithScheduledLaunch()
		throws Exception {

		ObjectEntry launchSetObjectEntry = _addLaunchSetObjectEntry();

		long launchSetId = launchSetObjectEntry.getObjectEntryId();

		_launchSetPublisher.schedulePublish(
			new Date(System.currentTimeMillis() + Time.DAY), launchSetId,
			TestPropsValues.getUserId());

		try {
			_assertLaunchPublishStatus(
				null, LaunchStatusConstants.LABEL_SCHEDULED, "Scheduled",
				launchSetId);
		}
		finally {
			_launchSetPublisher.unschedulePublish(launchSetId);
		}
	}

	private void _testGetLaunchPublishStatusWithoutViewPermission()
		throws Exception {

		ObjectEntry launchSetObjectEntry = _addLaunchSetObjectEntry();

		long launchSetId = launchSetObjectEntry.getObjectEntryId();

		String password = RandomTestUtil.randomString();

		User user = UserTestUtil.addUser(testCompany, password);

		LaunchPublishStatusResource launchPublishStatusResource =
			LaunchPublishStatusResource.builder(
			).authentication(
				user.getEmailAddress(), password
			).endpoint(
				testCompany.getVirtualHostname(),
				PortalUtil.getPortalServerPort(false), "http"
			).locale(
				LocaleUtil.getDefault()
			).build();

		assertHttpResponseStatusCode(
			404,
			launchPublishStatusResource.getLaunchPublishStatusHttpResponse(
				launchSetId));
	}

	private static final String _TASK_EXECUTOR_CLASS_NAME =
		"com.liferay.launch.internal.background.task." +
			"LaunchSetPublishBackgroundTaskExecutor";

	@Inject
	private BackgroundTaskLocalService _backgroundTaskLocalService;

	@Inject
	private BackgroundTaskManager _backgroundTaskManager;

	@Inject
	private LaunchSetPublisher _launchSetPublisher;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

}