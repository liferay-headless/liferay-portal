/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.internal.entry.type.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.fragment.constants.FragmentConstants;
import com.liferay.fragment.constants.FragmentPortletKeys;
import com.liferay.fragment.model.FragmentCollection;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.service.FragmentCollectionLocalService;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.fragment.service.FragmentEntryService;
import com.liferay.launch.entry.type.LaunchEntryType;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
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
public class FragmentEntryLaunchEntryTypeTest
	extends BaseLaunchEntryTypeTestCase {

	@Override
	protected long addEntry() throws Exception {
		FragmentCollection fragmentCollection =
			_fragmentCollectionLocalService.addFragmentCollection(
				null, TestPropsValues.getUserId(), group.getGroupId(),
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				serviceContext);

		FragmentEntry fragmentEntry =
			_fragmentEntryLocalService.addFragmentEntry(
				null, TestPropsValues.getUserId(), group.getGroupId(),
				fragmentCollection.getFragmentCollectionId(),
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				null, "<div>published</div>", null, false, null, null, 0, false,
				false, FragmentConstants.TYPE_COMPONENT, null,
				WorkflowConstants.STATUS_APPROVED, serviceContext);

		return fragmentEntry.getFragmentEntryId();
	}

	@Override
	protected String getBackURLParameterName() {
		return PortalUtil.getPortletNamespace(FragmentPortletKeys.FRAGMENT) +
			"backURL";
	}

	@Override
	protected String getEntryTitle(long classPK) {
		FragmentEntry fragmentEntry =
			_fragmentEntryLocalService.fetchFragmentEntry(classPK);

		return fragmentEntry.getName();
	}

	@Override
	protected List<Serializable> getListedPrimaryKeys(long classPK)
		throws Exception {

		FragmentEntry fragmentEntry =
			_fragmentEntryLocalService.fetchFragmentEntry(classPK);

		return TransformUtil.transform(
			_fragmentEntryService.getFragmentCompositionsAndFragmentEntries(
				fragmentEntry.getGroupId(),
				fragmentEntry.getFragmentCollectionId(),
				WorkflowConstants.STATUS_ANY, QueryUtil.ALL_POS,
				QueryUtil.ALL_POS, null),
			object -> {
				if (object instanceof FragmentEntry) {
					FragmentEntry listedFragmentEntry = (FragmentEntry)object;

					return listedFragmentEntry.getFragmentEntryId();
				}

				return null;
			});
	}

	@Override
	protected Class<?> getModelClass() {
		return FragmentEntry.class;
	}

	@Override
	protected Map<String, String> getSelectedItemData(long classPK) {
		FragmentEntry fragmentEntry =
			_fragmentEntryLocalService.fetchFragmentEntry(classPK);

		return TreeMapBuilder.<String, String>create(
			String.CASE_INSENSITIVE_ORDER
		).put(
			"fragmententrykey", fragmentEntry.getFragmentEntryKey()
		).put(
			"groupid", String.valueOf(fragmentEntry.getGroupId())
		).build();
	}

	@Override
	protected List<Map<String, String>> getUnbranchableSelectedItemsData(
		long classPK) {

		FragmentEntry fragmentEntry =
			_fragmentEntryLocalService.fetchFragmentEntry(classPK);

		return ListUtil.fromArray(
			TreeMapBuilder.<String, String>create(
				String.CASE_INSENSITIVE_ORDER
			).put(
				"fragmententrykey", RandomTestUtil.randomString()
			).put(
				"groupid", String.valueOf(fragmentEntry.getGroupId())
			).build());
	}

	@Override
	protected void updateEntry(String title, LaunchEntryType.Version version)
		throws Exception {

		_fragmentEntryLocalService.updateFragmentEntry(
			GetterUtil.getLong(version.getPrimaryKey()), title);
	}

	@Override
	protected void updateLiveEntry(String title, long classPK)
		throws Exception {

		_fragmentEntryLocalService.updateFragmentEntry(classPK, title);
	}

	@Inject
	private FragmentCollectionLocalService _fragmentCollectionLocalService;

	@Inject
	private FragmentEntryLocalService _fragmentEntryLocalService;

	@Inject
	private FragmentEntryService _fragmentEntryService;

}