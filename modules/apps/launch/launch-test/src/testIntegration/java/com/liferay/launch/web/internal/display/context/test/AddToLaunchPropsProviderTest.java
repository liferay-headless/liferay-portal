/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.web.internal.display.context.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.journal.model.JournalArticle;
import com.liferay.launch.display.context.AddToLaunchPropsProvider;
import com.liferay.object.constants.ObjectActionKeys;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Jorge González
 */
@FeatureFlag("LPD-72278")
@RunWith(Arquillian.class)
public class AddToLaunchPropsProviderTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testGetProps() {
		Assert.assertEquals(
			HashMapBuilder.<String, Object>put(
				"className", JournalArticle.class.getName()
			).put(
				"classPK", 1001L
			).put(
				"classVersion", "1.1"
			).build(),
			_addToLaunchPropsProvider.getProps(
				JournalArticle.class.getName(), 1001L, "1.1"));
	}

	@Test
	public void testIsEnabled() throws Exception {
		Assert.assertTrue(
			_addToLaunchPropsProvider.isEnabled(
				JournalArticle.class.getName(),
				PermissionCheckerFactoryUtil.create(
					TestPropsValues.getUser())));
		Assert.assertFalse(
			_addToLaunchPropsProvider.isEnabled(
				RandomTestUtil.randomString(),
				PermissionCheckerFactoryUtil.create(
					TestPropsValues.getUser())));

		_user = UserTestUtil.addUser();

		Assert.assertFalse(
			_addToLaunchPropsProvider.isEnabled(
				JournalArticle.class.getName(),
				PermissionCheckerFactoryUtil.create(_user)));

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_LAUNCH_ENTRY", TestPropsValues.getCompanyId());

		_role = RoleTestUtil.addRole(
			RandomTestUtil.randomString(), RoleConstants.TYPE_REGULAR,
			objectDefinition.getResourceName(), ResourceConstants.SCOPE_COMPANY,
			String.valueOf(TestPropsValues.getCompanyId()),
			ObjectActionKeys.ADD_OBJECT_ENTRY);

		_userLocalService.addRoleUser(_role.getRoleId(), _user);

		Assert.assertTrue(
			_addToLaunchPropsProvider.isEnabled(
				JournalArticle.class.getName(),
				PermissionCheckerFactoryUtil.create(_user)));
	}

	@FeatureFlag(enable = false, value = "LPD-72278")
	@Test
	public void testIsEnabledWithFeatureFlagDisabled() throws Exception {
		Assert.assertFalse(
			_addToLaunchPropsProvider.isEnabled(
				JournalArticle.class.getName(),
				PermissionCheckerFactoryUtil.create(
					TestPropsValues.getUser())));
	}

	@Inject
	private AddToLaunchPropsProvider _addToLaunchPropsProvider;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@DeleteAfterTestRun
	private Role _role;

	@DeleteAfterTestRun
	private User _user;

	@Inject
	private UserLocalService _userLocalService;

}