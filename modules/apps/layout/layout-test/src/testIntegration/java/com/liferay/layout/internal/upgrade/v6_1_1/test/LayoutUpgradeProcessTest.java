/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.internal.upgrade.v6_1_1.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.change.tracking.model.CTCollection;
import com.liferay.change.tracking.service.CTCollectionLocalService;
import com.liferay.change.tracking.test.util.BaseCTUpgradeProcessTestCase;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.layout.page.template.model.LayoutPageTemplateStructure;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureLocalService;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureRelLocalService;
import com.liferay.layout.test.util.ContentLayoutTestUtil;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.cache.MultiVMPool;
import com.liferay.portal.kernel.change.tracking.CTCollectionThreadLocal;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.dao.orm.EntityCache;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.LayoutSetPrototype;
import com.liferay.portal.kernel.model.change.tracking.CTModel;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.LayoutSetPrototypeLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.change.tracking.CTService;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.version.Version;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;
import com.liferay.segments.model.SegmentsExperience;
import com.liferay.segments.service.SegmentsExperienceLocalService;
import com.liferay.segments.test.util.SegmentsTestUtil;
import com.liferay.sites.kernel.util.Sites;

import java.sql.Connection;
import java.sql.PreparedStatement;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Adolfo Pérez
 */
@FeatureFlag("LPD-82107")
@RunWith(Arquillian.class)
@Sync
public class LayoutUpgradeProcessTest extends BaseCTUpgradeProcessTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_connection = DataAccess.getConnection();

		_layoutSetPrototype = LayoutTestUtil.addLayoutSetPrototype(
			RandomTestUtil.randomString());

		_layoutSetPrototypeGroup = _layoutSetPrototype.getGroup();

		_parentLayout = LayoutTestUtil.addTypePortletLayout(
			_layoutSetPrototypeGroup, true);

		Layout layout = LayoutTestUtil.addTypePortletLayout(
			_layoutSetPrototypeGroup, true);

		_childLayout = _layoutLocalService.updateParentLayoutId(
			layout.getPlid(), _parentLayout.getPlid());

		_contentLayout = LayoutTestUtil.addTypeContentLayout(
			_layoutSetPrototypeGroup, true, false);

		Layout draftLayout = _contentLayout.fetchDraftLayout();

		LayoutPageTemplateStructure layoutPageTemplateStructure =
			_layoutPageTemplateStructureLocalService.
				fetchLayoutPageTemplateStructure(
					draftLayout.getGroupId(), draftLayout.getPlid());

		SegmentsExperience segmentsExperience =
			SegmentsTestUtil.addSegmentsExperience(
				draftLayout.getGroupId(), draftLayout.getPlid());

		_layoutPageTemplateStructureRelLocalService.
			addLayoutPageTemplateStructureRel(
				TestPropsValues.getUserId(), draftLayout.getGroupId(),
				layoutPageTemplateStructure.getLayoutPageTemplateStructureId(),
				segmentsExperience.getSegmentsExperienceId(),
				layoutPageTemplateStructure.getDefaultSegmentsExperienceData(),
				ServiceContextTestUtil.getServiceContext(
					draftLayout.getGroupId(), TestPropsValues.getUserId()));

		ContentLayoutTestUtil.addFragmentEntryLinkToLayout(
			"{}", draftLayout,
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperienceId(
				draftLayout.getPlid()));

		ContentLayoutTestUtil.publishLayout(draftLayout, _contentLayout);

		_group = GroupTestUtil.addGroup();

		_sites.updateLayoutSetPrototypesLinks(
			_group, _layoutSetPrototype.getLayoutSetPrototypeId(),
			_layoutSetPrototype.getLayoutSetPrototypeId(), true, true);

		_sites.mergeLayoutSetPrototypeLayouts(
			_layoutSetPrototype, TestPropsValues.getUserId());
	}

	@After
	public void tearDown() throws Exception {
		DataAccess.cleanUp(_connection);

		GroupTestUtil.deleteGroup(_group);

		_layoutSetPrototypeLocalService.deleteLayoutSetPrototype(
			_layoutSetPrototype);
	}

	@Test
	@TestInfo("LPD-108617")
	public void testUpgrade() throws Exception {
		Layout linkedChildLayout = _getLinkedLayout(_childLayout, false);

		Layout linkToLayoutLayout = LayoutTestUtil.addTypeLinkToLayoutLayout(
			_group.getGroupId(), false, linkedChildLayout.getLayoutId());

		_resetExternalReferenceCodes();
		_resetUUIDs();

		Layout privateLinkedContentLayout = _getLinkedLayout(
			_contentLayout, true);

		_copyLayoutSetPrototypeLayout(
			_contentLayout, privateLinkedContentLayout);
		_copyLayoutSetPrototypeLayout(
			_contentLayout.fetchDraftLayout(),
			privateLinkedContentLayout.fetchDraftLayout());

		UnicodeProperties unicodeProperties =
			linkToLayoutLayout.getTypeSettingsProperties();

		unicodeProperties.setProperty(
			"linkToLayoutExternalReferenceCode",
			String.valueOf(linkedChildLayout.getPlid()));

		_executeUpdate(
			"update Layout set typeSettings = ? where plid = ?",
			unicodeProperties.toString(), linkToLayoutLayout.getPlid());

		runUpgrade();

		_assertExternalReferenceCodes(_childLayout, false);
		_assertExternalReferenceCodes(_childLayout, true);
		_assertExternalReferenceCodes(_contentLayout, false);
		_assertExternalReferenceCodes(_contentLayout, true);
		_assertExternalReferenceCodes(_parentLayout, false);
		_assertExternalReferenceCodes(_parentLayout, true);

		linkedChildLayout = _layoutLocalService.getLayout(
			linkedChildLayout.getPlid());
		linkToLayoutLayout = _layoutLocalService.getLayout(
			linkToLayoutLayout.getPlid());

		Assert.assertEquals(
			linkedChildLayout.getExternalReferenceCode(),
			linkToLayoutLayout.getTypeSettingsProperty(
				"linkToLayoutExternalReferenceCode"));

		int privateLayoutsCount = _layoutLocalService.getLayoutsCount(
			_group, true);
		int publicLayoutsCount = _layoutLocalService.getLayoutsCount(
			_group, false);

		String languageId = LocaleUtil.toLanguageId(
			LocaleUtil.getSiteDefault());
		String name = RandomTestUtil.randomString();

		_layoutLocalService.updateName(
			_childLayout.getPlid(), name, languageId);

		_sites.mergeLayoutSetPrototypeLayouts(
			_layoutSetPrototype, TestPropsValues.getUserId());

		Assert.assertEquals(
			privateLayoutsCount,
			_layoutLocalService.getLayoutsCount(_group, true));
		Assert.assertEquals(
			publicLayoutsCount,
			_layoutLocalService.getLayoutsCount(_group, false));

		Layout privateLinkedChildLayout = _getLinkedLayout(_childLayout, true);

		Assert.assertEquals(name, privateLinkedChildLayout.getName(languageId));

		Layout publicLinkedChildLayout = _getLinkedLayout(_childLayout, false);

		Assert.assertEquals(name, publicLinkedChildLayout.getName(languageId));
	}

	@Test
	@TestInfo("LPD-108617")
	public void testUpgradeWithCTCollection() throws Exception {
		Layout layout = LayoutTestUtil.addTypePortletLayout(
			_layoutSetPrototypeGroup, true);

		CTCollection ctCollection = _ctCollectionLocalService.addCTCollection(
			null, TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
			0, RandomTestUtil.randomString(), RandomTestUtil.randomString());

		try {
			Layout linkedLayout = null;

			try (SafeCloseable safeCloseable =
					CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
						ctCollection.getCtCollectionId())) {

				ServiceContext serviceContext =
					ServiceContextTestUtil.getServiceContext(
						_group.getGroupId(), TestPropsValues.getUserId());

				serviceContext.setAttribute(
					"layoutSetPrototypeLayoutERC",
					layout.getExternalReferenceCode());

				linkedLayout = _layoutLocalService.addLayout(
					null, TestPropsValues.getUserId(), _group.getGroupId(),
					false, LayoutConstants.DEFAULT_PARENT_LAYOUT_ID,
					RandomTestUtil.randomString(), null, null,
					LayoutConstants.TYPE_PORTLET, false, null, serviceContext);
			}

			_resetExternalReferenceCodes();

			runUpgrade();

			try (SafeCloseable safeCloseable =
					CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
						ctCollection.getCtCollectionId())) {

				linkedLayout = _layoutLocalService.getLayout(
					linkedLayout.getPlid());

				Assert.assertEquals(
					layout.getExternalReferenceCode(),
					linkedLayout.getExternalReferenceCode());
			}
		}
		finally {
			_ctCollectionLocalService.deleteCTCollection(ctCollection);
		}
	}

	@Override
	protected CTModel<?> addCTModel() throws Exception {
		_resetExternalReferenceCodes();

		return _getLinkedLayout(_contentLayout, false);
	}

	@Override
	protected void deleteCTModel(long primaryKey) throws Exception {
		Layout layout = _layoutLocalService.fetchLayout(primaryKey);

		if (layout != null) {
			_layoutLocalService.deleteLayout(layout);
		}
	}

	@Override
	protected CTService<?> getCTService() {
		return _layoutLocalService;
	}

	@Override
	protected void runUpgrade() throws Exception {
		UpgradeProcess[] upgradeProcesses = UpgradeTestUtil.getUpgradeSteps(
			_upgradeStepRegistrator, new Version(6, 1, 1));

		for (UpgradeProcess upgradeProcess : upgradeProcesses) {
			upgradeProcess.upgrade();
		}

		_entityCache.clearCache();
		_multiVMPool.clear();
	}

	@Override
	protected CTModel<?> updateCTModel(CTModel<?> ctModel) {
		return _layoutLocalService.updateLayout((Layout)ctModel);
	}

	private void _assertExternalReferenceCodes(
			Layout layout, boolean privateLayout)
		throws Exception {

		Layout linkedLayout = _getLinkedLayout(layout, privateLayout);

		_assertExternalReferenceCodes(layout, linkedLayout, privateLayout);

		Layout draftLayout = layout.fetchDraftLayout();

		if (draftLayout != null) {
			_assertExternalReferenceCodes(
				draftLayout, linkedLayout.fetchDraftLayout(), privateLayout);
		}
	}

	private void _assertExternalReferenceCodes(
		Layout layout, Layout linkedLayout, boolean privateLayout) {

		String suffix = StringPool.BLANK;

		if (privateLayout) {
			suffix = LayoutConstants.EXTERNAL_REFERENCE_CODE_SUFFIX_PRIVATE;
		}

		Assert.assertEquals(
			layout.getExternalReferenceCode() + suffix,
			linkedLayout.getExternalReferenceCode());
		Assert.assertEquals(
			layout.getExternalReferenceCode(),
			linkedLayout.getLayoutSetPrototypeLayoutERC());

		List<FragmentEntryLink> fragmentEntryLinks =
			_fragmentEntryLinkLocalService.getFragmentEntryLinksByPlid(
				layout.getGroupId(), layout.getPlid());
		List<FragmentEntryLink> linkedFragmentEntryLinks =
			_fragmentEntryLinkLocalService.getFragmentEntryLinksByPlid(
				linkedLayout.getGroupId(), linkedLayout.getPlid());

		Assert.assertEquals(
			_getExternalReferenceCodes(fragmentEntryLinks, suffix),
			_getExternalReferenceCodes(linkedFragmentEntryLinks, null));

		if (privateLayout) {
			Set<String> uuids = _getUUIDs(fragmentEntryLinks);

			uuids.retainAll(_getUUIDs(linkedFragmentEntryLinks));

			Assert.assertEquals(uuids.toString(), 0, uuids.size());
		}

		for (SegmentsExperience segmentsExperience :
				_segmentsExperienceLocalService.getSegmentsExperiences(
					layout.getGroupId(), layout.getPlid())) {

			SegmentsExperience linkedSegmentsExperience =
				_segmentsExperienceLocalService.fetchSegmentsExperience(
					linkedLayout.getGroupId(),
					segmentsExperience.getSegmentsExperienceKey(),
					linkedLayout.getPlid());

			Assert.assertEquals(
				segmentsExperience.getExternalReferenceCode() + suffix,
				linkedSegmentsExperience.getExternalReferenceCode());

			Assert.assertFalse(
				privateLayout &&
				Objects.equals(
					segmentsExperience.getUuid(),
					linkedSegmentsExperience.getUuid()));
		}
	}

	private void _copyLayoutSetPrototypeLayout(
			Layout layoutSetPrototypeLayout, Layout layout)
		throws Exception {

		_executeUpdate(
			"update Layout set externalReferenceCode = ? where plid = ?",
			layoutSetPrototypeLayout.getExternalReferenceCode(),
			layout.getPlid());

		for (SegmentsExperience segmentsExperience :
				_segmentsExperienceLocalService.getSegmentsExperiences(
					layoutSetPrototypeLayout.getGroupId(),
					layoutSetPrototypeLayout.getPlid())) {

			_executeUpdate(
				"update SegmentsExperience set uuid_ = ? where plid = ? and " +
					"segmentsExperienceKey = ?",
				segmentsExperience.getUuid(), layout.getPlid(),
				segmentsExperience.getSegmentsExperienceKey());
		}

		List<FragmentEntryLink> fragmentEntryLinks =
			_fragmentEntryLinkLocalService.getFragmentEntryLinksByPlid(
				layoutSetPrototypeLayout.getGroupId(),
				layoutSetPrototypeLayout.getPlid());

		Assert.assertEquals(
			fragmentEntryLinks.toString(), 1, fragmentEntryLinks.size());

		FragmentEntryLink fragmentEntryLink = fragmentEntryLinks.get(0);

		_executeUpdate(
			"update FragmentEntryLink set uuid_ = ? where plid = ?",
			fragmentEntryLink.getUuid(), layout.getPlid());
	}

	private void _executeUpdate(String sql, Object... parameters)
		throws Exception {

		try (PreparedStatement preparedStatement = _connection.prepareStatement(
				sql)) {

			for (int i = 0; i < parameters.length; i++) {
				preparedStatement.setObject(i + 1, parameters[i]);
			}

			preparedStatement.executeUpdate();
		}

		_entityCache.clearCache();
		_multiVMPool.clear();
	}

	private Set<String> _getExternalReferenceCodes(
		List<FragmentEntryLink> fragmentEntryLinks, String suffix) {

		Set<String> externalReferenceCodes = new HashSet<>();

		for (FragmentEntryLink fragmentEntryLink : fragmentEntryLinks) {
			String externalReferenceCode =
				fragmentEntryLink.getExternalReferenceCode();

			if (suffix != null) {
				externalReferenceCode += suffix;
			}

			externalReferenceCodes.add(externalReferenceCode);
		}

		return externalReferenceCodes;
	}

	private Layout _getLinkedLayout(Layout layout, boolean privateLayout)
		throws Exception {

		for (Layout linkedLayout :
				_layoutLocalService.getLayouts(
					_group.getGroupId(), privateLayout)) {

			if (Objects.equals(
					layout.getExternalReferenceCode(),
					linkedLayout.getLayoutSetPrototypeLayoutERC())) {

				return linkedLayout;
			}
		}

		throw new AssertionError(
			"No layout is linked to " + layout.getExternalReferenceCode());
	}

	private Set<String> _getUUIDs(List<FragmentEntryLink> fragmentEntryLinks) {
		Set<String> uuids = new HashSet<>();

		for (FragmentEntryLink fragmentEntryLink : fragmentEntryLinks) {
			uuids.add(fragmentEntryLink.getUuid());
		}

		return uuids;
	}

	private void _resetExternalReferenceCodes() throws Exception {
		DB db = DBManagerUtil.getDB();

		db.runSQL(
			"update Layout set externalReferenceCode = CAST_TEXT(plid) where " +
				"groupId = " + _group.getGroupId());
		db.runSQL(
			"update SegmentsExperience set externalReferenceCode = " +
				"CAST_TEXT(segmentsExperienceId) where groupId = " +
					_group.getGroupId());
		db.runSQL(
			"update FragmentEntryLink set externalReferenceCode = " +
				"CAST_TEXT(fragmentEntryLinkId) where groupId = " +
					_group.getGroupId());

		_entityCache.clearCache();
		_multiVMPool.clear();
	}

	private void _resetUUIDs() throws Exception {
		DB db = DBManagerUtil.getDB();

		db.runSQL(
			"update SegmentsExperience set uuid_ = " +
				"CAST_TEXT(segmentsExperienceId) where groupId = " +
					_group.getGroupId());
		db.runSQL(
			"update FragmentEntryLink set uuid_ = " +
				"CAST_TEXT(fragmentEntryLinkId) where groupId = " +
					_group.getGroupId());

		_entityCache.clearCache();
		_multiVMPool.clear();
	}

	private Layout _childLayout;
	private Connection _connection;
	private Layout _contentLayout;

	@Inject
	private CTCollectionLocalService _ctCollectionLocalService;

	@Inject
	private EntityCache _entityCache;

	@Inject
	private FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;

	private Group _group;

	@Inject
	private LayoutLocalService _layoutLocalService;

	@Inject
	private LayoutPageTemplateStructureLocalService
		_layoutPageTemplateStructureLocalService;

	@Inject
	private LayoutPageTemplateStructureRelLocalService
		_layoutPageTemplateStructureRelLocalService;

	private LayoutSetPrototype _layoutSetPrototype;
	private Group _layoutSetPrototypeGroup;

	@Inject
	private LayoutSetPrototypeLocalService _layoutSetPrototypeLocalService;

	@Inject
	private MultiVMPool _multiVMPool;

	private Layout _parentLayout;

	@Inject
	private SegmentsExperienceLocalService _segmentsExperienceLocalService;

	@Inject
	private Sites _sites;

	@Inject(
		filter = "component.name=com.liferay.layout.internal.upgrade.registry.LayoutServiceUpgradeStepRegistrator"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

}