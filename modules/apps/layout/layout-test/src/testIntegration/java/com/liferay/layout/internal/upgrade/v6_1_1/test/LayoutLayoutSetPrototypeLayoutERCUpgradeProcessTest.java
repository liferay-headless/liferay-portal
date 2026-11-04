/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.internal.upgrade.v6_1_1.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.kernel.cache.MultiVMPool;
import com.liferay.portal.kernel.dao.orm.EntityCache;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutSetPrototype;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.LayoutSetPrototypeLocalService;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;
import com.liferay.sites.kernel.util.Sites;

import java.util.Objects;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alberto Chaparro
 */
@FeatureFlag("LPD-82107")
@RunWith(Arquillian.class)
@Sync
public class LayoutLayoutSetPrototypeLayoutERCUpgradeProcessTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_layoutSetPrototype = LayoutTestUtil.addLayoutSetPrototype(
			RandomTestUtil.randomString());

		_layoutSetPrototypeGroup = _layoutSetPrototype.getGroup();

		_parentLayout = LayoutTestUtil.addTypePortletLayout(
			_layoutSetPrototypeGroup, true);

		Layout layout = LayoutTestUtil.addTypePortletLayout(
			_layoutSetPrototypeGroup, true);

		_childLayout = _layoutLocalService.updateParentLayoutId(
			layout.getPlid(), _parentLayout.getPlid());

		_group = GroupTestUtil.addGroup();

		_sites.updateLayoutSetPrototypesLinks(
			_group, _layoutSetPrototype.getLayoutSetPrototypeId(), 0, true,
			false);

		_sites.mergeLayoutSetPrototypeLayouts(
			_layoutSetPrototype, TestPropsValues.getUserId());
	}

	@After
	public void tearDown() throws Exception {
		GroupTestUtil.deleteGroup(_group);

		_layoutSetPrototypeLocalService.deleteLayoutSetPrototype(
			_layoutSetPrototype);
	}

	@Test
	@TestInfo("LPD-108617")
	public void testUpgrade() throws Exception {
		long linkedChildPlid = _getLinkedLayout(
			_childLayout
		).getPlid();

		Layout linkedParentLayout = _getLinkedLayout(_parentLayout);

		linkedParentLayout.setLayoutSetPrototypeLayoutERC(
			_childLayout.getExternalReferenceCode());

		linkedParentLayout = _layoutLocalService.updateLayout(
			linkedParentLayout);

		UpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator,
			"LayoutLayoutSetPrototypeLayoutERCUpgradeProcess"
		).upgrade();

		_entityCache.clearCache();
		_multiVMPool.clear();

		Layout linkedChildLayout = _layoutLocalService.getLayout(
			linkedChildPlid);

		Assert.assertEquals(
			_childLayout.getExternalReferenceCode(),
			linkedChildLayout.getLayoutSetPrototypeLayoutERC());

		linkedParentLayout = _layoutLocalService.getLayout(
			linkedParentLayout.getPlid());

		Assert.assertEquals(
			_parentLayout.getExternalReferenceCode(),
			linkedParentLayout.getLayoutSetPrototypeLayoutERC());
	}

	private Layout _getLinkedLayout(Layout layout) throws Exception {
		for (Layout linkedLayout :
				_layoutLocalService.getLayouts(_group.getGroupId(), false)) {

			if (Objects.equals(
					layout.getExternalReferenceCode(),
					linkedLayout.getLayoutSetPrototypeLayoutERC())) {

				return linkedLayout;
			}
		}

		throw new AssertionError(
			"No layout is linked to " + layout.getExternalReferenceCode());
	}

	private Layout _childLayout;

	@Inject
	private EntityCache _entityCache;

	private Group _group;

	@Inject
	private LayoutLocalService _layoutLocalService;

	private LayoutSetPrototype _layoutSetPrototype;
	private Group _layoutSetPrototypeGroup;

	@Inject
	private LayoutSetPrototypeLocalService _layoutSetPrototypeLocalService;

	@Inject
	private MultiVMPool _multiVMPool;

	private Layout _parentLayout;

	@Inject
	private Sites _sites;

	@Inject(
		filter = "component.name=com.liferay.layout.internal.upgrade.registry.LayoutServiceUpgradeStepRegistrator"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

}