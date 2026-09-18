/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.internal.entry.type.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.launch.entry.type.LaunchEntryType;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.TreeMapBuilder;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;

import java.io.Serializable;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alejandro Tardín
 */
@FeatureFlags(
	featureFlags = {@FeatureFlag("LPD-10622"), @FeatureFlag("LPD-72278")}
)
@RunWith(Arquillian.class)
public class LayoutLaunchEntryTypeTest extends BaseLaunchEntryTypeTestCase {

	@Override
	@Test
	public void testAddVersion() throws Exception {
		super.testAddVersion();

		long classPK = addEntry();

		LaunchEntryType launchEntryType = getLaunchEntryType();

		String title1 = RandomTestUtil.randomString();

		LaunchEntryType.Version version1 = addVersion(classPK);

		updateEntry(title1, version1);

		LaunchEntryType.Version publishedVersion1 =
			launchEntryType.publishVersion(classPK, version1.getClassVersion());

		String title2 = RandomTestUtil.randomString();

		LaunchEntryType.Version version2 = addVersion(classPK);

		updateEntry(title2, version2);

		launchEntryType.publishVersion(classPK, version2.getClassVersion());

		Assert.assertEquals(title2, getEntryTitle(classPK));

		LaunchEntryType.Version rebranchedVersion = launchEntryType.addVersion(
			classPK, publishedVersion1.getClassVersion());

		Assert.assertEquals(
			title1, rebranchedVersion.getTitle(LocaleUtil.getSiteDefault()));
	}

	@Test
	public void testGetPreviewBaseModel() throws Exception {
		long classPK = addEntry();

		Layout layout = _layoutLocalService.getLayout(classPK);

		LaunchEntryType launchEntryType = getLaunchEntryType();

		String title1 = RandomTestUtil.randomString();

		LaunchEntryType.Version version1 = addVersion(classPK);

		updateEntry(title1, version1);

		Layout launchDraftLayout = (Layout)version1.getPreviewBaseModel();

		Assert.assertEquals(
			GetterUtil.getLong(version1.getClassVersion()),
			launchDraftLayout.getPlid());

		Assert.assertEquals(
			layout.getFriendlyURL(), launchDraftLayout.getFriendlyURL());
		Assert.assertEquals(
			layout.getFriendlyURL(LocaleUtil.getSiteDefault()),
			launchDraftLayout.getFriendlyURL(LocaleUtil.getSiteDefault()));
		Assert.assertEquals(
			layout.getFriendlyURLMap(), launchDraftLayout.getFriendlyURLMap());

		LaunchEntryType.Version publishedVersion1 =
			launchEntryType.publishVersion(classPK, version1.getClassVersion());

		Assert.assertEquals(
			classPK,
			_getPreviewBaseModelPlid(
				classPK, publishedVersion1.getClassVersion()));

		String title2 = RandomTestUtil.randomString();

		LaunchEntryType.Version version2 = addVersion(classPK);

		updateEntry(title2, version2);

		LaunchEntryType.Version publishedVersion2 =
			launchEntryType.publishVersion(classPK, version2.getClassVersion());

		long previewPlid = _getPreviewBaseModelPlid(
			classPK, publishedVersion1.getClassVersion());

		Assert.assertNotEquals(classPK, previewPlid);

		Layout draftLayout = _layoutLocalService.fetchDraftLayout(classPK);

		Assert.assertNotEquals(draftLayout.getPlid(), previewPlid);

		Layout previewLayout = _layoutLocalService.getLayout(previewPlid);

		Assert.assertEquals(
			title1, previewLayout.getName(LocaleUtil.getSiteDefault()));

		Assert.assertEquals(
			previewPlid,
			_getPreviewBaseModelPlid(
				classPK, publishedVersion1.getClassVersion()));

		List<String> classVersions = TransformUtil.transform(
			launchEntryType.getVersions(classPK, LocaleUtil.getSiteDefault()),
			LaunchEntryType.Version::getClassVersion);

		Assert.assertFalse(
			classVersions.toString(),
			classVersions.contains(String.valueOf(previewPlid)));

		LaunchEntryType.Version version3 = addVersion(classPK);

		updateEntry(RandomTestUtil.randomString(), version3);

		launchEntryType.publishVersion(classPK, version3.getClassVersion());

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		ServiceContextThreadLocal.popServiceContext();

		try {
			PermissionThreadLocal.setPermissionChecker(
				PermissionCheckerFactoryUtil.create(
					_userLocalService.getGuestUser(group.getCompanyId())));

			previewLayout = _layoutLocalService.getLayout(
				_getPreviewBaseModelPlid(
					classPK, publishedVersion2.getClassVersion()));

			Assert.assertEquals(
				title2, previewLayout.getName(LocaleUtil.getSiteDefault()));
		}
		finally {
			PermissionThreadLocal.setPermissionChecker(permissionChecker);

			ServiceContextThreadLocal.pushServiceContext(serviceContext);
		}
	}

	@Override
	protected long addEntry() throws Exception {
		Layout layout = LayoutTestUtil.addTypeContentPublishedLayout(
			group, RandomTestUtil.randomString(),
			WorkflowConstants.STATUS_APPROVED);

		return layout.getPlid();
	}

	@Override
	protected void assertLiveEntryIsUnchanged(long classPK) throws Exception {
		Layout layout = _layoutLocalService.getLayout(classPK);

		Assert.assertTrue(layout.isPublished());

		Layout draftLayout = _layoutLocalService.fetchDraftLayout(classPK);

		String externalReferenceCode = draftLayout.getExternalReferenceCode();

		Assert.assertFalse(
			externalReferenceCode,
			externalReferenceCode.contains(
				LayoutConstants.EXTERNAL_REFERENCE_CODE_INFIX_LAUNCH_DRAFT));
	}

	@Override
	protected String getBackURLParameterName() {
		return "p_l_back_url";
	}

	@Override
	protected String getEntryTitle(long classPK) throws Exception {
		Layout layout = _layoutLocalService.getLayout(classPK);

		return layout.getName(LocaleUtil.getSiteDefault());
	}

	@Override
	protected List<Serializable> getListedPrimaryKeys(long classPK)
		throws Exception {

		Layout layout = _layoutLocalService.getLayout(classPK);

		return TransformUtil.transform(
			_layoutLocalService.getLayouts(
				layout.getGroupId(), layout.isPrivateLayout()),
			Layout::getPlid);
	}

	@Override
	protected Class<?> getModelClass() {
		return Layout.class;
	}

	@Override
	protected Map<String, String> getSelectedItemData(long classPK) {
		return TreeMapBuilder.<String, String>create(
			String.CASE_INSENSITIVE_ORDER
		).put(
			"plid", String.valueOf(classPK)
		).build();
	}

	@Override
	protected List<Map<String, String>> getUnbranchableSelectedItemsData(
			long classPK)
		throws Exception {

		Layout draftLayout = _layoutLocalService.fetchDraftLayout(classPK);

		Layout typePortletLayout = LayoutTestUtil.addTypePortletLayout(group);

		return ListUtil.fromArray(
			getSelectedItemData(draftLayout.getPlid()),
			getSelectedItemData(typePortletLayout.getPlid()));
	}

	@Override
	protected void updateEntry(String title, LaunchEntryType.Version version)
		throws Exception {

		_layoutLocalService.updateName(
			GetterUtil.getLong(version.getPrimaryKey()), title,
			LocaleUtil.toLanguageId(LocaleUtil.getSiteDefault()));
	}

	@Override
	protected void updateLiveEntry(String title, long classPK)
		throws Exception {

		_layoutLocalService.updateName(
			classPK, title,
			LocaleUtil.toLanguageId(LocaleUtil.getSiteDefault()));
	}

	private long _getPreviewBaseModelPlid(long classPK, String classVersion)
		throws Exception {

		LaunchEntryType.Version version = getVersion(classPK, classVersion);

		Layout layout = (Layout)version.getPreviewBaseModel();

		return layout.getPlid();
	}

	@Inject
	private LayoutLocalService _layoutLocalService;

	@Inject
	private UserLocalService _userLocalService;

}