/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.internal.entry.manager.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.journal.constants.JournalFolderConstants;
import com.liferay.journal.constants.JournalPortletKeys;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.journal.test.util.JournalTestUtil;
import com.liferay.launch.entry.manager.LaunchEntryManager;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.TreeMapBuilder;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;

import java.io.Serializable;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alejandro Tardín
 * @author Lourdes Fernández Besada
 */
@FeatureFlag("LPD-72278")
@RunWith(Arquillian.class)
public class JournalArticleLaunchEntryManagerTest
	extends BaseLaunchEntryManagerTestCase {

	@Test
	public void testAddDraftFromHistoricalVersion() throws Exception {
		long classPK = addEntry();

		Map<String, Object> entryValuesMap = getEntryValuesMap(classPK);

		LaunchEntryManager.Version publishedVersion = getPublishedVersion(
			classPK);

		updateLiveEntry(classPK, randomValuesMap());

		Assert.assertNotEquals(entryValuesMap, getEntryValuesMap(classPK));

		LaunchEntryManager launchEntryManager = getLaunchEntryManager();

		Assert.assertEquals(
			entryValuesMap,
			getDraftValuesMap(
				launchEntryManager.addDraft(
					classPK, publishedVersion.getVersionKey())));
	}

	@Test
	public void testGetPreviewBaseModelForHistoricalVersion() throws Exception {
		long classPK = addEntry();

		Map<String, Object> valuesMap1 = randomValuesMap();

		LaunchEntryManager.Draft draft1 = addDraft(classPK);

		updateDraft(draft1, valuesMap1);

		Map<String, Object> valuesMap2 = randomValuesMap();

		LaunchEntryManager.Draft draft2 = addDraft(classPK);

		updateDraft(draft2, valuesMap2);

		LaunchEntryManager launchEntryManager = getLaunchEntryManager();

		LaunchEntryManager.Version publishedVersion1 =
			launchEntryManager.publishDraft(classPK, draft1.getDraftId());

		launchEntryManager.publishDraft(classPK, draft2.getDraftId());

		LaunchEntryManager.Version version1 = getVersion(
			classPK, publishedVersion1.getVersionKey());

		try (SafeCloseable safeCloseable = setPreviewIdWithSafeCloseable(
				classPK, version1.getPreviewPrimaryKey())) {

			Assert.assertEquals(valuesMap1, getEntryValuesMap(classPK));
		}

		Assert.assertEquals(valuesMap2, getEntryValuesMap(classPK));
	}

	@Test
	public void testPublishDraftProducesDistinctVersionKey() throws Exception {
		long classPK = addEntry();

		LaunchEntryManager.Draft draft1 = addDraft(classPK);

		updateDraft(draft1, randomValuesMap());

		LaunchEntryManager.Draft draft2 = addDraft(classPK);

		updateDraft(draft2, randomValuesMap());

		LaunchEntryManager launchEntryManager = getLaunchEntryManager();

		LaunchEntryManager.Version publishedVersion2 =
			launchEntryManager.publishDraft(classPK, draft2.getDraftId());

		LaunchEntryManager.Version publishedVersion1 =
			launchEntryManager.publishDraft(classPK, draft1.getDraftId());

		Assert.assertNotEquals(
			publishedVersion2.getVersionKey(),
			publishedVersion1.getVersionKey());
	}

	@Override
	protected long addEntry() throws Exception {
		JournalArticle journalArticle = JournalTestUtil.addArticle(
			group.getGroupId(),
			JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID);

		return journalArticle.getResourcePrimKey();
	}

	@Override
	protected String getBackURLParameterName() {
		return PortalUtil.getPortletNamespace(JournalPortletKeys.JOURNAL) +
			"backURL";
	}

	@Override
	protected Map<String, Object> getDraftValuesMap(
			LaunchEntryManager.Draft draft)
		throws Exception {

		JournalArticle journalArticle =
			_journalArticleLocalService.getLatestArticle(
				draft.getDraftId(), WorkflowConstants.STATUS_ANY, false);

		return HashMapBuilder.<String, Object>put(
			"title", journalArticle.getTitle(LocaleUtil.getSiteDefault())
		).build();
	}

	@Override
	protected Map<String, Object> getEntryValuesMap(long classPK) {
		JournalArticle journalArticle =
			_journalArticleLocalService.fetchLatestArticle(
				classPK, WorkflowConstants.STATUS_APPROVED);

		return HashMapBuilder.<String, Object>put(
			"title", journalArticle.getTitle(LocaleUtil.getSiteDefault())
		).build();
	}

	@Override
	protected List<Serializable> getListedPrimaryKeys(long classPK) {
		JournalArticle journalArticle =
			_journalArticleLocalService.fetchLatestArticle(
				classPK,
				new int[] {
					WorkflowConstants.STATUS_APPROVED,
					WorkflowConstants.STATUS_IN_TRASH
				});

		return ListUtil.fromArray(journalArticle.getId());
	}

	@Override
	protected Class<?> getModelClass() {
		return JournalArticle.class;
	}

	@Override
	protected Map<String, String> getSelectedItemData(long classPK) {
		return TreeMapBuilder.<String, String>create(
			String.CASE_INSENSITIVE_ORDER
		).put(
			"classPK", String.valueOf(classPK)
		).build();
	}

	@Override
	protected Map<String, Object> randomValuesMap() {
		return HashMapBuilder.<String, Object>put(
			"title", RandomTestUtil.randomString()
		).build();
	}

	@Override
	protected void updateDraft(
			LaunchEntryManager.Draft draft, Map<String, Object> valuesMap)
		throws Exception {

		JournalArticle journalArticle =
			_journalArticleLocalService.getLatestArticle(
				draft.getDraftId(), WorkflowConstants.STATUS_ANY, false);

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				group.getGroupId(), TestPropsValues.getUserId());

		serviceContext.setWorkflowAction(WorkflowConstants.ACTION_SAVE_DRAFT);

		_journalArticleLocalService.updateArticle(
			TestPropsValues.getUserId(), journalArticle.getGroupId(),
			journalArticle.getFolderId(), journalArticle.getArticleId(),
			journalArticle.getVersion(),
			HashMapBuilder.put(
				LocaleUtil.getSiteDefault(),
				MapUtil.getString(valuesMap, "title")
			).build(),
			journalArticle.getDescriptionMap(), journalArticle.getContent(),
			journalArticle.getLayoutUuid(), serviceContext);
	}

	@Override
	protected void updateLiveEntry(long classPK, Map<String, Object> valuesMap)
		throws Exception {

		JournalTestUtil.updateArticle(
			_journalArticleLocalService.getLatestArticle(
				classPK, WorkflowConstants.STATUS_APPROVED),
			MapUtil.getString(valuesMap, "title"));
	}

	@Inject
	private JournalArticleLocalService _journalArticleLocalService;

}