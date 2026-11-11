/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.rest.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.launch.set.publisher.LaunchSetPublisher;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.util.FeatureFlagTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;

import java.io.Serializable;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
public class LaunchResourceTest extends BaseLaunchResourceTestCase {

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
	public void testPostLaunchPublish() throws Exception {
		_testPostLaunchPublish();
		_testPostLaunchPublishWithFeatureFlagDisabled();
		_testPostLaunchPublishWithMissingLaunch();
		_testPostLaunchPublishWithScheduledLaunch();
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

		serviceContext.setWorkflowAction(WorkflowConstants.ACTION_SAVE_DRAFT);

		return _objectEntryLocalService.addObjectEntry(
			0, TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId(), 0,
			LocaleUtil.toLanguageId(LocaleUtil.getSiteDefault()), values,
			serviceContext);
	}

	private void _assertStatus(long launchSetId, int expectedStatus)
		throws Exception {

		ObjectEntry launchSetObjectEntry =
			_objectEntryLocalService.getObjectEntry(launchSetId);

		Assert.assertEquals(expectedStatus, launchSetObjectEntry.getStatus());
	}

	private void _testPostLaunchPublish() throws Exception {
		ObjectEntry launchSetObjectEntry = _addLaunchSetObjectEntry();

		long launchSetId = launchSetObjectEntry.getObjectEntryId();

		assertHttpResponseStatusCode(
			202, launchResource.postLaunchPublishHttpResponse(launchSetId));

		_assertStatus(launchSetId, WorkflowConstants.STATUS_APPROVED);

		List<BackgroundTask> backgroundTasks = ListUtil.filter(
			_backgroundTaskManager.getBackgroundTasks(
				BackgroundTaskConstants.GROUP_ID_DEFAULT,
				_TASK_EXECUTOR_CLASS_NAME),
			backgroundTask -> Objects.equals(
				backgroundTask.getName(), String.valueOf(launchSetId)));

		Assert.assertEquals(
			backgroundTasks.toString(), 1, backgroundTasks.size());

		BackgroundTask backgroundTask = backgroundTasks.get(0);

		Assert.assertEquals(
			BackgroundTaskConstants.STATUS_SUCCESSFUL,
			backgroundTask.getStatus());

		assertHttpResponseStatusCode(
			409, launchResource.postLaunchPublishHttpResponse(launchSetId));
	}

	private void _testPostLaunchPublishWithFeatureFlagDisabled()
		throws Exception {

		ObjectEntry launchSetObjectEntry = _addLaunchSetObjectEntry();

		long launchSetId = launchSetObjectEntry.getObjectEntryId();

		try (SafeCloseable safeCloseable =
				FeatureFlagTestUtil.setFeatureFlagsWithSafeCloseable(
					false, "LPD-72278")) {

			assertHttpResponseStatusCode(
				404, launchResource.postLaunchPublishHttpResponse(launchSetId));
		}

		_assertStatus(launchSetId, WorkflowConstants.STATUS_DRAFT);
	}

	private void _testPostLaunchPublishWithMissingLaunch() throws Exception {
		assertHttpResponseStatusCode(
			404,
			launchResource.postLaunchPublishHttpResponse(
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
			launchResource.postLaunchPublishHttpResponse(
				launchEntryObjectEntry.getObjectEntryId()));
	}

	private void _testPostLaunchPublishWithScheduledLaunch() throws Exception {
		ObjectEntry launchSetObjectEntry = _addLaunchSetObjectEntry();

		long launchSetId = launchSetObjectEntry.getObjectEntryId();

		_launchSetPublisher.schedulePublish(
			new Date(System.currentTimeMillis() + Time.DAY), launchSetId,
			TestPropsValues.getUserId());

		try {
			assertHttpResponseStatusCode(
				409, launchResource.postLaunchPublishHttpResponse(launchSetId));

			_assertStatus(launchSetId, WorkflowConstants.STATUS_SCHEDULED);
		}
		finally {
			_launchSetPublisher.unschedulePublish(launchSetId);
		}
	}

	private static final String _TASK_EXECUTOR_CLASS_NAME =
		"com.liferay.launch.internal.background.task." +
			"LaunchSetPublishBackgroundTaskExecutor";

	@Inject
	private BackgroundTaskManager _backgroundTaskManager;

	@Inject
	private LaunchSetPublisher _launchSetPublisher;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

}