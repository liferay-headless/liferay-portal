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
import com.liferay.launch.entry.type.LaunchEntryType;
import com.liferay.launch.entry.type.registry.LaunchEntryTypeRegistry;
import com.liferay.launch.set.publisher.LaunchSetPublisher;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.io.Serializable;

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
 */
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
		if (_otherLaunchSetObjectEntry != null) {
			_objectEntryLocalService.deleteObjectEntry(
				_otherLaunchSetObjectEntry);
		}

		_objectEntryLocalService.deleteObjectEntry(_launchSetObjectEntry);

		ServiceContextThreadLocal.popServiceContext();
	}

	@Test
	public void testPublish() throws Exception {
		JournalArticle journalArticle = JournalTestUtil.addArticle(
			_group.getGroupId(),
			JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID);

		String title = RandomTestUtil.randomString();

		LaunchEntryType.Version draftVersion = _addLaunchDraft(
			journalArticle, title);

		ObjectEntry launchEntryObjectEntry = _addLaunchEntryObjectEntry(
			_launchSetObjectEntry.getObjectEntryId(),
			JournalArticle.class.getName(), journalArticle.getResourcePrimKey(),
			draftVersion);

		ObjectEntry unknownLaunchEntryObjectEntry = _addLaunchEntryObjectEntry(
			_launchSetObjectEntry.getObjectEntryId(),
			RandomTestUtil.randomString(), RandomTestUtil.randomInt(),
			draftVersion);

		try {
			_launchSetPublisher.publish(
				_launchSetObjectEntry.getObjectEntryId());

			Assert.fail();
		}
		catch (PortalException portalException) {
			Assert.assertTrue(
				portalException.getMessage(),
				portalException.getMessage(
				).startsWith(
					"No launch entry type exists for "
				));
		}

		LaunchEntryType launchEntryType = _getJournalArticleLaunchEntryType();

		Assert.assertEquals(
			String.valueOf(journalArticle.getVersion()),
			launchEntryType.fetchPublishedVersion(
				journalArticle.getResourcePrimKey()
			).getClassVersion());

		ObjectEntry launchSetObjectEntry =
			_objectEntryLocalService.getObjectEntry(
				_launchSetObjectEntry.getObjectEntryId());

		Assert.assertNotEquals(
			WorkflowConstants.STATUS_APPROVED,
			launchSetObjectEntry.getStatus());

		_objectEntryLocalService.deleteObjectEntry(
			unknownLaunchEntryObjectEntry);

		_launchSetPublisher.publish(_launchSetObjectEntry.getObjectEntryId());

		LaunchEntryType.Version publishedVersion =
			launchEntryType.fetchPublishedVersion(
				journalArticle.getResourcePrimKey());

		Assert.assertNotEquals(
			draftVersion.getClassVersion(), publishedVersion.getClassVersion());
		Assert.assertEquals(
			title, publishedVersion.getTitle(LocaleUtil.getSiteDefault()));

		Assert.assertEquals(
			publishedVersion.getClassVersion(),
			MapUtil.getString(
				_getValues(launchEntryObjectEntry), "classVersion"));

		launchSetObjectEntry = _objectEntryLocalService.getObjectEntry(
			_launchSetObjectEntry.getObjectEntryId());

		Assert.assertEquals(
			WorkflowConstants.STATUS_APPROVED,
			launchSetObjectEntry.getStatus());

		try {
			_launchSetPublisher.publish(
				_launchSetObjectEntry.getObjectEntryId());

			Assert.fail();
		}
		catch (PortalException portalException) {
			Assert.assertTrue(
				portalException.getMessage(),
				portalException.getMessage(
				).endsWith(
					"was already published"
				));
		}

		_otherLaunchSetObjectEntry = _addLaunchSetObjectEntry();

		String otherTitle = RandomTestUtil.randomString();

		LaunchEntryType.Version otherDraftVersion = _addLaunchDraft(
			journalArticle, otherTitle);

		ObjectEntry otherLaunchEntryObjectEntry = _addLaunchEntryObjectEntry(
			_otherLaunchSetObjectEntry.getObjectEntryId(),
			JournalArticle.class.getName(), journalArticle.getResourcePrimKey(),
			otherDraftVersion);

		JournalTestUtil.updateArticle(
			_journalArticleLocalService.getLatestArticle(
				journalArticle.getResourcePrimKey(),
				WorkflowConstants.STATUS_APPROVED),
			RandomTestUtil.randomString());

		_launchSetPublisher.publish(
			_otherLaunchSetObjectEntry.getObjectEntryId());

		LaunchEntryType.Version otherPublishedVersion =
			launchEntryType.fetchPublishedVersion(
				journalArticle.getResourcePrimKey());

		Assert.assertEquals(
			otherTitle,
			otherPublishedVersion.getTitle(LocaleUtil.getSiteDefault()));
		Assert.assertNotEquals(
			otherDraftVersion.getClassVersion(),
			otherPublishedVersion.getClassVersion());

		Assert.assertEquals(
			otherPublishedVersion.getClassVersion(),
			MapUtil.getString(
				_getValues(otherLaunchEntryObjectEntry), "classVersion"));
	}

	private LaunchEntryType.Version _addLaunchDraft(
			JournalArticle journalArticle, String title)
		throws Exception {

		LaunchEntryType launchEntryType = _getJournalArticleLaunchEntryType();

		LaunchEntryType.Version draftVersion = launchEntryType.addVersion(
			journalArticle.getResourcePrimKey(),
			String.valueOf(journalArticle.getVersion()));

		JournalArticle launchDraftJournalArticle =
			_journalArticleLocalService.getArticle(
				GetterUtil.getLong(draftVersion.getPrimaryKey()));

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

		return draftVersion;
	}

	private ObjectEntry _addLaunchEntryObjectEntry(
			long launchSetId, String className, long classPK,
			LaunchEntryType.Version draftVersion)
		throws Exception {

		return _addObjectEntry(
			"LaunchEntry",
			HashMapBuilder.<String, Serializable>put(
				"baseClassVersion", draftVersion.getClassVersion()
			).put(
				"className", className
			).put(
				"classPK", classPK
			).put(
				"classVersion", draftVersion.getClassVersion()
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

	private LaunchEntryType _getJournalArticleLaunchEntryType() {
		return _launchEntryTypeRegistry.getLaunchEntryType(
			JournalArticle.class.getName());
	}

	private Map<String, Serializable> _getValues(ObjectEntry objectEntry)
		throws Exception {

		ObjectEntry reloadedObjectEntry =
			_objectEntryLocalService.getObjectEntry(
				objectEntry.getObjectEntryId());

		return reloadedObjectEntry.getValues();
	}

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private JournalArticleLocalService _journalArticleLocalService;

	@Inject
	private LaunchEntryTypeRegistry _launchEntryTypeRegistry;

	private ObjectEntry _launchSetObjectEntry;

	@Inject
	private LaunchSetPublisher _launchSetPublisher;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	private ObjectEntry _otherLaunchSetObjectEntry;
	private ServiceContext _serviceContext;

}