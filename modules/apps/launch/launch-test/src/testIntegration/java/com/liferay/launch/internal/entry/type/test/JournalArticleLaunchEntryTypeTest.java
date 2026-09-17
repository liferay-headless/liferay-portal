/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.internal.entry.type.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.journal.constants.JournalFolderConstants;
import com.liferay.journal.constants.JournalPortletKeys;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.journal.test.util.JournalTestUtil;
import com.liferay.launch.entry.type.LaunchEntryType;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.TreeMapBuilder;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;

import java.io.Serializable;

import java.util.List;
import java.util.Map;

import org.junit.runner.RunWith;

/**
 * @author Alejandro Tardín
 */
@FeatureFlag("LPD-72278")
@RunWith(Arquillian.class)
public class JournalArticleLaunchEntryTypeTest
	extends BaseLaunchEntryTypeTestCase {

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
	protected String getEntryTitle(long classPK) {
		JournalArticle journalArticle =
			_journalArticleLocalService.fetchLatestArticle(
				classPK, WorkflowConstants.STATUS_APPROVED);

		return journalArticle.getTitle(LocaleUtil.getSiteDefault());
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
	protected void updateEntry(String title, LaunchEntryType.Version version)
		throws Exception {

		JournalArticle launchDraftJournalArticle =
			_journalArticleLocalService.getArticle(
				GetterUtil.getLong(version.getPrimaryKey()));

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
			_getLaunchDraftServiceContext());
	}

	@Override
	protected void updateLiveEntry(String title, long classPK)
		throws Exception {

		JournalTestUtil.updateArticle(
			_journalArticleLocalService.getLatestArticle(
				classPK, WorkflowConstants.STATUS_APPROVED),
			title);
	}

	private ServiceContext _getLaunchDraftServiceContext() throws Exception {
		ServiceContext launchDraftServiceContext =
			ServiceContextTestUtil.getServiceContext(
				group.getGroupId(), TestPropsValues.getUserId());

		launchDraftServiceContext.setWorkflowAction(
			WorkflowConstants.ACTION_SAVE_DRAFT);

		return launchDraftServiceContext;
	}

	@Inject
	private JournalArticleLocalService _journalArticleLocalService;

}