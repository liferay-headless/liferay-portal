/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.internal.entry.type.test;

import com.liferay.launch.entry.type.LaunchEntryType;
import com.liferay.launch.entry.type.registry.LaunchEntryTypeRegistry;
import com.liferay.layout.test.util.ContentLayoutTestUtil;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.petra.function.UnsafeSupplierValue;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.preview.PreviewableResolverUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.io.Serializable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Alejandro Tardín
 */
public abstract class BaseLaunchEntryTypeTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		group = GroupTestUtil.addGroup();

		serviceContext = ServiceContextTestUtil.getServiceContext(
			group.getGroupId(), TestPropsValues.getUserId());

		ServiceContextThreadLocal.pushServiceContext(serviceContext);
	}

	@After
	public void tearDown() {
		ServiceContextThreadLocal.popServiceContext();
	}

	@Test
	public void testAddPublishedVersion() throws Exception {
		long classPK = addEntry();

		String title = RandomTestUtil.randomString();

		LaunchEntryType.Version version = addVersion(classPK);

		updateEntry(title, version);

		Assert.assertNotEquals(title, getEntryTitle(classPK));

		LaunchEntryType launchEntryType = getLaunchEntryType();

		LaunchEntryType.Version publishedVersion =
			launchEntryType.addPublishedVersion(
				classPK, version.getClassVersion());

		Assert.assertEquals(title, getEntryTitle(classPK));

		Assert.assertEquals(
			title, publishedVersion.getTitle(LocaleUtil.getSiteDefault()));
		Assert.assertNotEquals(
			version.getClassVersion(), publishedVersion.getClassVersion());

		LaunchEntryType.Version fetchedPublishedVersion = getPublishedVersion(
			classPK);

		Assert.assertEquals(
			publishedVersion.getClassVersion(),
			fetchedPublishedVersion.getClassVersion());

		Assert.assertEquals(
			WorkflowConstants.STATUS_APPROVED,
			getVersion(
				classPK, publishedVersion.getClassVersion()
			).getStatus());
	}

	@Test
	public void testAddVersion() throws Exception {
		long classPK = addEntry();

		String entryTitle = getEntryTitle(classPK);

		LaunchEntryType.Version version1 = addVersion(classPK);
		LaunchEntryType.Version version2 = addVersion(classPK);

		Assert.assertNotEquals(
			version1.getClassVersion(), version2.getClassVersion());

		String title1 = RandomTestUtil.randomString();

		updateEntry(title1, version1);

		String title2 = RandomTestUtil.randomString();

		updateEntry(title2, version2);

		Assert.assertEquals(title1, _getVersionTitle(classPK, version1));
		Assert.assertEquals(title2, _getVersionTitle(classPK, version2));

		Assert.assertEquals(entryTitle, getEntryTitle(classPK));

		assertLiveEntryIsUnchanged(classPK);

		String title3 = RandomTestUtil.randomString();

		updateLiveEntry(title3, classPK);

		Assert.assertEquals(title3, getEntryTitle(classPK));

		Assert.assertEquals(title1, _getVersionTitle(classPK, version1));
		Assert.assertEquals(title2, _getVersionTitle(classPK, version2));

		LaunchEntryType launchEntryType = getLaunchEntryType();

		LaunchEntryType.Version rebranchedVersion = launchEntryType.addVersion(
			classPK, version1.getClassVersion());

		Assert.assertNotEquals(
			version1.getClassVersion(), rebranchedVersion.getClassVersion());
		Assert.assertEquals(
			title1, rebranchedVersion.getTitle(LocaleUtil.getSiteDefault()));
	}

	@Test
	public void testFetchPublishedVersion() throws Exception {
		long classPK = addEntry();

		LaunchEntryType.Version publishedVersion = getPublishedVersion(classPK);

		Assert.assertEquals(
			WorkflowConstants.STATUS_APPROVED, publishedVersion.getStatus());
		Assert.assertEquals(
			getEntryTitle(classPK),
			publishedVersion.getTitle(LocaleUtil.getSiteDefault()));

		List<Serializable> listedPrimaryKeys = getListedPrimaryKeys(classPK);

		Assert.assertTrue(
			listedPrimaryKeys.toString(),
			listedPrimaryKeys.contains(publishedVersion.getPrimaryKey()));
	}

	@Test
	public void testFetchVersion() throws Exception {
		long classPK = addEntry();

		String title = RandomTestUtil.randomString();

		LaunchEntryType.Version version = addVersion(classPK);

		updateEntry(title, version);

		LaunchEntryType.Version fetchedVersion = getVersion(
			classPK, version.getClassVersion());

		Assert.assertEquals(
			version.getClassVersion(), fetchedVersion.getClassVersion());
		Assert.assertEquals(
			version.getPrimaryKey(), fetchedVersion.getPrimaryKey());
		Assert.assertEquals(
			title, fetchedVersion.getTitle(LocaleUtil.getSiteDefault()));

		LaunchEntryType launchEntryType = getLaunchEntryType();

		Assert.assertNull(
			launchEntryType.fetchVersion(
				classPK, String.valueOf(RandomTestUtil.randomLong())));
		Assert.assertNull(
			launchEntryType.fetchVersion(
				RandomTestUtil.randomLong(), version.getClassVersion()));
	}

	@Test
	public void testGetClassPK() throws Exception {
		long classPK = addEntry();

		LaunchEntryType launchEntryType = getLaunchEntryType();

		Assert.assertEquals(
			classPK, launchEntryType.getClassPK(getSelectedItemData(classPK)));

		for (Map<String, String> selectedItemData :
				getUnbranchableSelectedItemsData(classPK)) {

			Assert.assertEquals(
				selectedItemData.toString(), 0,
				launchEntryType.getClassPK(selectedItemData));
		}
	}

	@Test
	public void testGetEditURL() throws Exception {
		String editURL = _getEditURL(addVersion(addEntry()));

		Map<String, String[]> parameterMap = HttpComponentsUtil.getParameterMap(
			HttpComponentsUtil.getQueryString(editURL));

		Assert.assertArrayEquals(
			editURL, new String[] {_REDIRECT},
			parameterMap.get(getBackURLParameterName()));
	}

	@Test
	public void testGetPrimaryKey() throws Exception {
		long classPK = addEntry();

		String entryTitle = getEntryTitle(classPK);

		String title1 = RandomTestUtil.randomString();

		LaunchEntryType.Version version1 = addVersion(classPK);

		updateEntry(title1, version1);

		String title2 = RandomTestUtil.randomString();

		LaunchEntryType.Version version2 = addVersion(classPK);

		updateEntry(title2, version2);

		try (SafeCloseable safeCloseable = _setPreviewIdWithSafeCloseable(
				classPK, version1)) {

			Assert.assertEquals(title1, getEntryTitle(classPK));
		}

		try (SafeCloseable safeCloseable = _setPreviewIdWithSafeCloseable(
				classPK, version2)) {

			Assert.assertEquals(title2, getEntryTitle(classPK));
		}

		Assert.assertEquals(entryTitle, getEntryTitle(classPK));

		LaunchEntryType launchEntryType = getLaunchEntryType();

		LaunchEntryType.Version publishedVersion1 =
			launchEntryType.publishVersion(classPK, version1.getClassVersion());

		launchEntryType.publishVersion(classPK, version2.getClassVersion());

		try (SafeCloseable safeCloseable = _setPreviewIdWithSafeCloseable(
				classPK, publishedVersion1)) {

			Assert.assertEquals(title1, getEntryTitle(classPK));
		}

		Assert.assertEquals(title2, getEntryTitle(classPK));
	}

	@Test
	public void testGetVersions() throws Exception {
		long classPK = addEntry();

		List<LaunchEntryType.Version> versions = List.of(
			addVersion(classPK), addVersion(classPK),
			getPublishedVersion(classPK));

		LaunchEntryType launchEntryType = getLaunchEntryType();

		List<String> classVersions = TransformUtil.transform(
			launchEntryType.getVersions(classPK, LocaleUtil.getSiteDefault()),
			LaunchEntryType.Version::getClassVersion);

		for (LaunchEntryType.Version version : versions) {
			Assert.assertTrue(
				classVersions.toString(),
				classVersions.contains(version.getClassVersion()));
		}

		for (String classVersion : classVersions) {
			LaunchEntryType.Version version = getVersion(classPK, classVersion);

			Assert.assertEquals(classVersion, version.getClassVersion());

			Assert.assertNotNull(
				classVersion,
				launchEntryType.addVersion(classPK, classVersion));
		}
	}

	@Test
	public void testMarkVersionPublished() throws Exception {
		long classPK = addEntry();

		String title = RandomTestUtil.randomString();

		LaunchEntryType.Version version = addVersion(classPK);

		updateEntry(title, version);

		Assert.assertNotEquals(title, getEntryTitle(classPK));

		LaunchEntryType launchEntryType = getLaunchEntryType();

		LaunchEntryType.Version publishedVersion =
			launchEntryType.publishVersion(classPK, version.getClassVersion());

		Assert.assertEquals(title, getEntryTitle(classPK));

		Assert.assertEquals(
			title, publishedVersion.getTitle(LocaleUtil.getSiteDefault()));

		LaunchEntryType.Version fetchedPublishedVersion = getPublishedVersion(
			classPK);

		Assert.assertEquals(
			publishedVersion.getClassVersion(),
			fetchedPublishedVersion.getClassVersion());

		Assert.assertEquals(
			WorkflowConstants.STATUS_APPROVED,
			getVersion(
				classPK, publishedVersion.getClassVersion()
			).getStatus());
	}

	protected abstract long addEntry() throws Exception;

	protected LaunchEntryType.Version addVersion(long classPK)
		throws Exception {

		LaunchEntryType launchEntryType = getLaunchEntryType();

		LaunchEntryType.Version publishedVersion = getPublishedVersion(classPK);

		return launchEntryType.addVersion(
			classPK, publishedVersion.getClassVersion());
	}

	protected void assertLiveEntryIsUnchanged(long classPK) throws Exception {
	}

	protected abstract String getBackURLParameterName();

	protected abstract String getEntryTitle(long classPK) throws Exception;

	protected LaunchEntryType getLaunchEntryType() {
		Class<?> modelClass = getModelClass();

		return _launchEntryTypeRegistry.getLaunchEntryType(
			modelClass.getName());
	}

	protected abstract List<Serializable> getListedPrimaryKeys(long classPK)
		throws Exception;

	protected abstract Class<?> getModelClass();

	protected LaunchEntryType.Version getPublishedVersion(long classPK)
		throws Exception {

		LaunchEntryType launchEntryType = getLaunchEntryType();

		LaunchEntryType.Version publishedVersion =
			launchEntryType.fetchPublishedVersion(classPK);

		Assert.assertNotNull(publishedVersion);

		return publishedVersion;
	}

	protected abstract Map<String, String> getSelectedItemData(long classPK)
		throws Exception;

	protected List<Map<String, String>> getUnbranchableSelectedItemsData(
			long classPK)
		throws Exception {

		return Collections.emptyList();
	}

	protected LaunchEntryType.Version getVersion(
			long classPK, String classVersion)
		throws Exception {

		LaunchEntryType launchEntryType = getLaunchEntryType();

		LaunchEntryType.Version version = launchEntryType.fetchVersion(
			classPK, classVersion);

		Assert.assertNotNull(classVersion, version);

		return version;
	}

	protected abstract void updateEntry(
			String title, LaunchEntryType.Version version)
		throws Exception;

	protected abstract void updateLiveEntry(String title, long classPK)
		throws Exception;

	@DeleteAfterTestRun
	protected Group group;

	protected ServiceContext serviceContext;

	private String _getEditURL(LaunchEntryType.Version version)
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY,
			ContentLayoutTestUtil.getThemeDisplay(
				_companyLocalService.getCompany(group.getCompanyId()), group,
				LayoutTestUtil.addTypePortletLayout(group)));

		serviceContext.setRequest(mockHttpServletRequest);

		return version.getEditURL(
			_REDIRECT,
			RequestBackedPortletURLFactoryUtil.create(mockHttpServletRequest));
	}

	private Map<Serializable, Object> _getPreviewablePrimaryKeys(
			long classPK, LaunchEntryType.Version version)
		throws Exception {

		LaunchEntryType.Version previewVersion = getVersion(
			classPK, version.getClassVersion());

		return HashMapBuilder.<Serializable, Object>put(
			() -> {
				LaunchEntryType.Version publishedVersion = getPublishedVersion(
					classPK);

				return publishedVersion.getPrimaryKey();
			},
			new UnsafeSupplierValue<>(previewVersion::getPreviewBaseModel)
		).build();
	}

	private String _getVersionTitle(
			long classPK, LaunchEntryType.Version version)
		throws Exception {

		LaunchEntryType.Version fetchedVersion = getVersion(
			classPK, version.getClassVersion());

		return fetchedVersion.getTitle(LocaleUtil.getSiteDefault());
	}

	private SafeCloseable _setPreviewIdWithSafeCloseable(
			long classPK, LaunchEntryType.Version version)
		throws Exception {

		Long previewId = PreviewableResolverUtil.addPreviewableMap(
			HashMapBuilder.<Class<?>, Map<Serializable, Object>>put(
				getModelClass(), _getPreviewablePrimaryKeys(classPK, version)
			).build());

		SafeCloseable safeCloseable =
			PreviewableResolverUtil.setPreviewIdWithSafeCloseable(previewId);

		return () -> {
			safeCloseable.close();

			PreviewableResolverUtil.removePreviewableMap(previewId);
		};
	}

	private static final String _REDIRECT =
		"/group/control_panel/manage?p_p_id=" +
			"com_liferay_launch_web_portlet_LaunchPortlet";

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private LaunchEntryTypeRegistry _launchEntryTypeRegistry;

}