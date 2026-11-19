/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.internal.set.publisher.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.journal.constants.JournalFolderConstants;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.journal.test.util.JournalTestUtil;
import com.liferay.launch.entry.manager.LaunchEntryManager;
import com.liferay.launch.entry.manager.LaunchEntryManagerRegistry;
import com.liferay.launch.set.publisher.LaunchSetPublisher;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.WorkflowDefinitionLinkLocalService;
import com.liferay.portal.kernel.test.portlet.MockActionResponse;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionRequest;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import jakarta.portlet.PortletException;

import java.io.Serializable;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alejandro Tardín
 * @author Jose Luis Navarro
 */
@DataGuard(scope = DataGuard.Scope.METHOD)
@RunWith(Arquillian.class)
public class LaunchSetPublisherTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_group.getGroupId(), TestPropsValues.getUserId());

		ServiceContextThreadLocal.pushServiceContext(_serviceContext);

		_launchSetObjectEntry = _addLaunchSetObjectEntry();
	}

	@After
	public void tearDown() throws Exception {
		_launchSetPublisher.unschedulePublish(
			_launchSetObjectEntry.getObjectEntryId());

		ServiceContextThreadLocal.popServiceContext();
	}

	@Test
	public void testPublish() throws Exception {
		JournalArticle journalArticle = JournalTestUtil.addArticle(
			_group.getGroupId(),
			JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID);

		ObjectEntry launchEntryObjectEntry = _addLaunchEntryObjectEntry(
			_launchSetObjectEntry.getObjectEntryId(),
			JournalArticle.class.getName(), journalArticle.getResourcePrimKey(),
			String.valueOf(journalArticle.getVersion()));

		Assert.assertThrows(
			PortalException.class,
			() -> _launchSetPublisher.publish(
				launchEntryObjectEntry.getObjectEntryId()));

		String title = RandomTestUtil.randomString();

		long draftId = _updateLaunchDraft(launchEntryObjectEntry, title);

		_launchSetPublisher.publish(_launchSetObjectEntry.getObjectEntryId());

		Assert.assertEquals(title, _getTitle(journalArticle));

		LaunchEntryManager launchEntryManager =
			_getJournalArticleLaunchEntryManager();

		_assertPublished(
			draftId, launchEntryObjectEntry, launchEntryManager,
			launchEntryManager.fetchPublishedVersion(
				journalArticle.getResourcePrimKey()));

		ObjectEntry launchSetObjectEntry =
			_objectEntryLocalService.getObjectEntry(
				_launchSetObjectEntry.getObjectEntryId());

		Assert.assertEquals(
			WorkflowConstants.STATUS_DRAFT, launchSetObjectEntry.getStatus());
	}

	@Test
	public void testRequestPublish() throws Exception {
		JournalArticle journalArticle = JournalTestUtil.addArticle(
			_group.getGroupId(),
			JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID);

		ObjectEntry launchEntryObjectEntry1 = _addLaunchEntryObjectEntry(
			_launchSetObjectEntry.getObjectEntryId(),
			JournalArticle.class.getName(), journalArticle.getResourcePrimKey(),
			String.valueOf(journalArticle.getVersion()));

		Assert.assertThrows(
			PortalException.class,
			() -> _launchSetPublisher.requestPublish(
				launchEntryObjectEntry1.getObjectEntryId(),
				TestPropsValues.getUserId()));

		String title = RandomTestUtil.randomString();

		long draftId = _updateLaunchDraft(launchEntryObjectEntry1, title);

		String unknownClassName = RandomTestUtil.randomString();

		ObjectEntry unknownLaunchEntryObjectEntry = _addLaunchEntryObjectEntry(
			_launchSetObjectEntry.getObjectEntryId(), unknownClassName,
			RandomTestUtil.randomInt(), RandomTestUtil.randomString());

		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			new MockLiferayPortletActionRequest();

		mockLiferayPortletActionRequest.setParameter(
			"launchSetId",
			String.valueOf(_launchSetObjectEntry.getObjectEntryId()));

		try {
			_mvcActionCommand.processAction(
				mockLiferayPortletActionRequest, new MockActionResponse());

			Assert.fail();
		}
		catch (PortletException portletException) {
			Throwable throwable = portletException.getCause();

			Assert.assertEquals(
				"No launch entry manager exists for " + unknownClassName,
				throwable.getMessage());
		}

		LaunchEntryManager launchEntryManager =
			_getJournalArticleLaunchEntryManager();

		LaunchEntryManager.Version version =
			launchEntryManager.fetchPublishedVersion(
				journalArticle.getResourcePrimKey());

		Assert.assertEquals(
			String.valueOf(journalArticle.getVersion()),
			version.getVersionKey());

		ObjectEntry launchSetObjectEntry =
			_objectEntryLocalService.getObjectEntry(
				_launchSetObjectEntry.getObjectEntryId());

		Assert.assertNotEquals(
			WorkflowConstants.STATUS_APPROVED,
			launchSetObjectEntry.getStatus());

		_objectEntryLocalService.deleteObjectEntry(
			unknownLaunchEntryObjectEntry);

		_launchSetPublisher.requestPublish(
			_launchSetObjectEntry.getObjectEntryId(),
			TestPropsValues.getUserId());

		LaunchEntryManager.Version publishedVersion =
			launchEntryManager.fetchPublishedVersion(
				journalArticle.getResourcePrimKey());

		Assert.assertEquals(title, _getTitle(journalArticle));
		Assert.assertNotEquals(
			String.valueOf(journalArticle.getVersion()),
			publishedVersion.getVersionKey());

		_assertPublished(
			draftId, launchEntryObjectEntry1, launchEntryManager,
			publishedVersion);

		launchSetObjectEntry = _objectEntryLocalService.getObjectEntry(
			_launchSetObjectEntry.getObjectEntryId());

		Assert.assertEquals(
			WorkflowConstants.STATUS_APPROVED,
			launchSetObjectEntry.getStatus());

		try {
			_launchSetPublisher.requestPublish(
				_launchSetObjectEntry.getObjectEntryId(),
				TestPropsValues.getUserId());

			Assert.fail();
		}
		catch (PortalException portalException) {
			Assert.assertTrue(
				portalException.getMessage(),
				StringUtil.endsWith(
					portalException.getMessage(), "was already published"));
		}

		ObjectEntry otherLaunchSetObjectEntry = _addLaunchSetObjectEntry();

		ObjectEntry otherLaunchEntryObjectEntry = _addLaunchEntryObjectEntry(
			otherLaunchSetObjectEntry.getObjectEntryId(),
			JournalArticle.class.getName(), journalArticle.getResourcePrimKey(),
			String.valueOf(journalArticle.getVersion()));

		String otherTitle = RandomTestUtil.randomString();

		long otherDraftId = _updateLaunchDraft(
			otherLaunchEntryObjectEntry, otherTitle);

		JournalTestUtil.updateArticle(
			_journalArticleLocalService.getLatestArticle(
				journalArticle.getResourcePrimKey(),
				WorkflowConstants.STATUS_APPROVED),
			RandomTestUtil.randomString());

		_launchSetPublisher.requestPublish(
			otherLaunchSetObjectEntry.getObjectEntryId(),
			TestPropsValues.getUserId());

		LaunchEntryManager.Version otherPublishedVersion =
			launchEntryManager.fetchPublishedVersion(
				journalArticle.getResourcePrimKey());

		Assert.assertEquals(otherTitle, _getTitle(journalArticle));
		Assert.assertNotEquals(
			publishedVersion.getVersionKey(),
			otherPublishedVersion.getVersionKey());

		_assertPublished(
			otherDraftId, otherLaunchEntryObjectEntry, launchEntryManager,
			otherPublishedVersion);

		// Publish with workflow

		launchSetObjectEntry = _addLaunchSetObjectEntry();

		journalArticle = JournalTestUtil.addArticle(
			_group.getGroupId(),
			JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID);

		ObjectEntry launchEntryObjectEntry2 = _addLaunchEntryObjectEntry(
			launchSetObjectEntry.getObjectEntryId(),
			JournalArticle.class.getName(), journalArticle.getResourcePrimKey(),
			String.valueOf(journalArticle.getVersion()));

		title = RandomTestUtil.randomString();

		draftId = _updateLaunchDraft(launchEntryObjectEntry2, title);

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.getObjectDefinition(
				launchSetObjectEntry.getObjectDefinitionId());

		_workflowDefinitionLinkLocalService.updateWorkflowDefinitionLink(
			TestPropsValues.getUserId(), TestPropsValues.getCompanyId(), 0,
			objectDefinition.getClassName(), 0, 0, "Single Approver", 1);

		long launchSetId = launchSetObjectEntry.getObjectEntryId();

		_launchSetPublisher.requestPublish(
			launchSetId, TestPropsValues.getUserId());

		launchSetObjectEntry = _objectEntryLocalService.getObjectEntry(
			launchSetId);

		Assert.assertEquals(
			WorkflowConstants.STATUS_PENDING, launchSetObjectEntry.getStatus());

		version = launchEntryManager.fetchPublishedVersion(
			journalArticle.getResourcePrimKey());

		Assert.assertEquals(
			String.valueOf(journalArticle.getVersion()),
			version.getVersionKey());

		Assert.assertNotNull(
			launchEntryManager.fetchDraft(
				journalArticle.getResourcePrimKey(), draftId));

		try {
			_launchSetPublisher.requestPublish(
				launchSetId, TestPropsValues.getUserId());

			Assert.fail();
		}
		catch (PortalException portalException) {
			Assert.assertTrue(
				portalException.getMessage(),
				StringUtil.endsWith(
					portalException.getMessage(), "is pending approval"));
		}

		_completeWorkflowTask();

		launchSetObjectEntry = _objectEntryLocalService.getObjectEntry(
			launchSetId);

		Assert.assertEquals(
			WorkflowConstants.STATUS_APPROVED,
			launchSetObjectEntry.getStatus());

		Assert.assertEquals(title, _getTitle(journalArticle));

		_assertPublished(
			draftId, launchEntryObjectEntry2, launchEntryManager,
			launchEntryManager.fetchPublishedVersion(
				journalArticle.getResourcePrimKey()));
	}

	@Test
	public void testSchedulePublish() throws Exception {
		long launchSetId1 = _launchSetObjectEntry.getObjectEntryId();

		ObjectEntry launchEntryObjectEntry1 = _addLaunchEntryObjectEntry(
			launchSetId1, RandomTestUtil.randomString(),
			RandomTestUtil.randomInt(), RandomTestUtil.randomString());

		Assert.assertThrows(
			PortalException.class,
			() -> _launchSetPublisher.schedulePublish(
				new Date(System.currentTimeMillis() + Time.DAY),
				launchEntryObjectEntry1.getObjectEntryId(),
				TestPropsValues.getUserId()));

		Assert.assertThrows(
			PortalException.class,
			() -> _launchSetPublisher.schedulePublish(
				new Date(System.currentTimeMillis() - Time.MINUTE),
				launchSetId1, TestPropsValues.getUserId()));
		Assert.assertThrows(
			PortalException.class,
			() -> _launchSetPublisher.schedulePublish(
				null, launchSetId1, TestPropsValues.getUserId()));

		Date date = new Date(System.currentTimeMillis() + Time.DAY);

		_launchSetPublisher.schedulePublish(
			date, launchSetId1, TestPropsValues.getUserId());

		_assertDisplayDateAndStatus(
			date, WorkflowConstants.STATUS_SCHEDULED, launchSetId1);

		Assert.assertThrows(
			PortalException.class,
			() -> _launchSetPublisher.schedulePublish(
				new Date(System.currentTimeMillis() + Time.DAY), launchSetId1,
				TestPropsValues.getUserId()));
		_assertDisplayDateAndStatus(
			date, WorkflowConstants.STATUS_SCHEDULED, launchSetId1);

		// Schedule publish with workflow

		ObjectEntry launchSetObjectEntry = _addLaunchSetObjectEntry();

		JournalArticle journalArticle = JournalTestUtil.addArticle(
			_group.getGroupId(),
			JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID);

		ObjectEntry launchEntryObjectEntry2 = _addLaunchEntryObjectEntry(
			launchSetObjectEntry.getObjectEntryId(),
			JournalArticle.class.getName(), journalArticle.getResourcePrimKey(),
			String.valueOf(journalArticle.getVersion()));

		String title = RandomTestUtil.randomString();

		long draftId = _updateLaunchDraft(launchEntryObjectEntry2, title);

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.getObjectDefinition(
				launchSetObjectEntry.getObjectDefinitionId());

		_workflowDefinitionLinkLocalService.updateWorkflowDefinitionLink(
			TestPropsValues.getUserId(), TestPropsValues.getCompanyId(), 0,
			objectDefinition.getClassName(), 0, 0, "Single Approver", 1);

		long launchSetId2 = launchSetObjectEntry.getObjectEntryId();

		date = new Date(System.currentTimeMillis() + Time.DAY);

		_launchSetPublisher.schedulePublish(
			date, launchSetId2, TestPropsValues.getUserId());

		_assertDisplayDateAndStatus(
			date, WorkflowConstants.STATUS_PENDING, launchSetId2);

		Assert.assertThrows(
			PortalException.class,
			() -> _launchSetPublisher.unschedulePublish(launchSetId2));

		_completeWorkflowTask();

		_assertDisplayDateAndStatus(
			date, WorkflowConstants.STATUS_SCHEDULED, launchSetId2);

		LaunchEntryManager launchEntryManager =
			_getJournalArticleLaunchEntryManager();

		Assert.assertNotNull(
			launchEntryManager.fetchDraft(
				journalArticle.getResourcePrimKey(), draftId));

		launchSetObjectEntry = _objectEntryLocalService.getObjectEntry(
			launchSetId2);

		launchSetObjectEntry.setDisplayDate(
			new Date(System.currentTimeMillis() - Time.MINUTE));

		_objectEntryLocalService.updateObjectEntry(launchSetObjectEntry);

		_checkObjectEntries();

		launchSetObjectEntry = _objectEntryLocalService.getObjectEntry(
			launchSetId2);

		Assert.assertEquals(
			WorkflowConstants.STATUS_APPROVED,
			launchSetObjectEntry.getStatus());

		Assert.assertEquals(title, _getTitle(journalArticle));

		_assertPublished(
			draftId, launchEntryObjectEntry2, launchEntryManager,
			launchEntryManager.fetchPublishedVersion(
				journalArticle.getResourcePrimKey()));
	}

	@Test
	public void testUnschedulePublish() throws Exception {
		long launchSetId = _launchSetObjectEntry.getObjectEntryId();

		ObjectEntry launchEntryObjectEntry = _addLaunchEntryObjectEntry(
			launchSetId, RandomTestUtil.randomString(),
			RandomTestUtil.randomInt(), RandomTestUtil.randomString());

		Assert.assertThrows(
			PortalException.class,
			() -> _launchSetPublisher.unschedulePublish(
				launchEntryObjectEntry.getObjectEntryId()));

		_launchSetPublisher.unschedulePublish(launchSetId);

		_assertDisplayDateAndStatus(
			null, WorkflowConstants.STATUS_DRAFT, launchSetId);

		_launchSetPublisher.schedulePublish(
			new Date(System.currentTimeMillis() + Time.DAY), launchSetId,
			TestPropsValues.getUserId());

		_launchSetPublisher.unschedulePublish(launchSetId);

		_assertDisplayDateAndStatus(
			null, WorkflowConstants.STATUS_DRAFT, launchSetId);

		Date date = new Date(System.currentTimeMillis() + Time.WEEK);

		_launchSetPublisher.schedulePublish(
			date, launchSetId, TestPropsValues.getUserId());

		_assertDisplayDateAndStatus(
			date, WorkflowConstants.STATUS_SCHEDULED, launchSetId);
	}

	private ObjectEntry _addLaunchEntryObjectEntry(
			long launchSetId, String className, long classPK,
			String baseVersionKey)
		throws Exception {

		return _addObjectEntry(
			"LaunchEntry",
			HashMapBuilder.<String, Serializable>put(
				"baseVersionKey", baseVersionKey
			).put(
				"className", className
			).put(
				"classPK", classPK
			).put(
				"r_launchSetToLaunchEntries_c_launchSetId", launchSetId
			).build());
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
				TestPropsValues.getGroupId(), TestPropsValues.getUserId());

		serviceContext.setWorkflowAction(WorkflowConstants.ACTION_SAVE_DRAFT);

		return _objectEntryLocalService.addObjectEntry(
			0, TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId(), 0,
			LocaleUtil.toLanguageId(LocaleUtil.getSiteDefault()), values,
			serviceContext);
	}

	private void _assertDisplayDateAndStatus(
			Date expectedDate, int expectedStatus, long launchSetId)
		throws Exception {

		ObjectEntry launchSetObjectEntry =
			_objectEntryLocalService.getObjectEntry(launchSetId);

		Assert.assertEquals(expectedStatus, launchSetObjectEntry.getStatus());

		Date displayDate = launchSetObjectEntry.getDisplayDate();

		if (expectedDate == null) {
			Assert.assertNull(displayDate);

			return;
		}

		Assert.assertEquals(
			expectedDate.getTime() / Time.SECOND,
			displayDate.getTime() / Time.SECOND);
	}

	private void _assertPublished(
			long draftId, ObjectEntry launchEntryObjectEntry,
			LaunchEntryManager launchEntryManager,
			LaunchEntryManager.Version publishedVersion)
		throws Exception {

		Map<String, Serializable> values = _getValues(launchEntryObjectEntry);

		Assert.assertEquals(
			publishedVersion.getVersionKey(),
			MapUtil.getString(values, "versionKey"));
		Assert.assertEquals(0, MapUtil.getLong(values, "draftId"));

		Assert.assertNull(
			launchEntryManager.fetchDraft(
				MapUtil.getLong(values, "classPK"), draftId));
	}

	private void _checkObjectEntries() throws Exception {
		String name = PrincipalThreadLocal.getName();
		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		ServiceContext serviceContext =
			ServiceContextThreadLocal.popServiceContext();

		try {
			PermissionThreadLocal.setPermissionChecker(null);
			PrincipalThreadLocal.setName(null);

			Assert.assertNull(ServiceContextThreadLocal.getServiceContext());

			_objectEntryLocalService.checkObjectEntries(
				TestPropsValues.getCompanyId());
		}
		finally {
			PermissionThreadLocal.setPermissionChecker(permissionChecker);
			PrincipalThreadLocal.setName(name);

			ServiceContextThreadLocal.pushServiceContext(serviceContext);
		}
	}

	private void _completeWorkflowTask() throws Exception {
		List<WorkflowTask> workflowTasks =
			_workflowTaskManager.getWorkflowTasksBySubmittingUser(
				TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
				false, 0, 1, null);

		WorkflowTask workflowTask = workflowTasks.get(0);

		_workflowTaskManager.assignWorkflowTaskToUser(
			TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
			workflowTask.getWorkflowTaskId(), TestPropsValues.getUserId(),
			StringPool.BLANK, null, null);

		_workflowTaskManager.completeWorkflowTask(
			TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
			workflowTask.getWorkflowTaskId(), Constants.APPROVE,
			StringPool.BLANK, null);
	}

	private LaunchEntryManager _getJournalArticleLaunchEntryManager() {
		return _launchEntryManagerRegistry.getLaunchEntryManager(
			JournalArticle.class.getName());
	}

	private String _getTitle(JournalArticle journalArticle) throws Exception {
		JournalArticle latestJournalArticle =
			_journalArticleLocalService.getLatestArticle(
				journalArticle.getResourcePrimKey(),
				WorkflowConstants.STATUS_APPROVED);

		return latestJournalArticle.getTitle(LocaleUtil.getSiteDefault());
	}

	private Map<String, Serializable> _getValues(ObjectEntry objectEntry)
		throws Exception {

		ObjectEntry reloadedObjectEntry =
			_objectEntryLocalService.getObjectEntry(
				objectEntry.getObjectEntryId());

		return reloadedObjectEntry.getValues();
	}

	private long _updateLaunchDraft(
			ObjectEntry launchEntryObjectEntry, String title)
		throws Exception {

		long draftId = MapUtil.getLong(
			_getValues(launchEntryObjectEntry), "draftId");

		JournalArticle launchDraftJournalArticle =
			_journalArticleLocalService.getLatestArticle(
				draftId, WorkflowConstants.STATUS_ANY, false);

		ServiceContext launchDraftServiceContext =
			ServiceContextTestUtil.getServiceContext(
				_group.getGroupId(), TestPropsValues.getUserId());

		launchDraftServiceContext.setWorkflowAction(
			WorkflowConstants.ACTION_SAVE_DRAFT);

		_journalArticleLocalService.updateArticle(
			TestPropsValues.getUserId(), launchDraftJournalArticle.getGroupId(),
			launchDraftJournalArticle.getFolderId(),
			launchDraftJournalArticle.getArticleId(),
			launchDraftJournalArticle.getVersion(),
			HashMapBuilder.put(
				LocaleUtil.getSiteDefault(), title
			).build(),
			launchDraftJournalArticle.getDescriptionMap(),
			launchDraftJournalArticle.getContent(),
			launchDraftJournalArticle.getLayoutUuid(),
			launchDraftServiceContext);

		return draftId;
	}

	private Group _group;

	@Inject
	private JournalArticleLocalService _journalArticleLocalService;

	@Inject
	private LaunchEntryManagerRegistry _launchEntryManagerRegistry;

	private ObjectEntry _launchSetObjectEntry;

	@Inject
	private LaunchSetPublisher _launchSetPublisher;

	@Inject(filter = "mvc.command.name=/launch/publish_launch")
	private MVCActionCommand _mvcActionCommand;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	private ServiceContext _serviceContext;

	@Inject
	private WorkflowDefinitionLinkLocalService
		_workflowDefinitionLinkLocalService;

	@Inject
	private WorkflowTaskManager _workflowTaskManager;

}