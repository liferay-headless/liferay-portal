/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.user.internal.exportimport.data.handler.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.exportimport.test.util.exportimport.data.handler.BaseCompanyBatchEnginePortletDataHandlerTestCase;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.UserGroup;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.UserGroupLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Date;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;

/**
 * @author Alberto Javier Moreno Lage
 */
@RunWith(Arquillian.class)
public class CompanyUserGroupBatchEnginePortletDataHandlerTest
	extends BaseCompanyBatchEnginePortletDataHandlerTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Override
	protected void addEmptyEntry(String externalReferenceCode)
		throws Exception {

		_userGroupLocalService.getOrAddEmptyUserGroup(
			externalReferenceCode, TestPropsValues.getCompanyId(),
			TestPropsValues.getUserId(), RandomTestUtil.randomString());
	}

	@Override
	protected void addEntry(String externalReferenceCode) throws Exception {
		Group group = getGroup();

		_userGroupLocalService.addUserGroup(
			externalReferenceCode, TestPropsValues.getUserId(),
			TestPropsValues.getCompanyId(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString(),
			ServiceContextTestUtil.getServiceContext(
				group.getGroupId(), TestPropsValues.getUserId()));
	}

	@Override
	protected void deleteEntry(String externalReferenceCode) throws Exception {
		Group group = getGroup();

		_userGroupLocalService.deleteUserGroup(
			_getUserGroup(externalReferenceCode, group.getGroupId()));
	}

	@Override
	protected long getCreatorUserId(String externalReferenceCode, long groupId)
		throws Exception {

		UserGroup userGroup = _getUserGroup(externalReferenceCode, groupId);

		return userGroup.getUserId();
	}

	@Override
	protected String getEntityClassName() {
		return com.liferay.headless.admin.user.dto.v1_0.UserGroup.class.
			getName();
	}

	@Override
	protected long getPrimaryKey(String externalReferenceCode, long groupId)
		throws Exception {

		UserGroup userGroup = _getUserGroup(externalReferenceCode, groupId);

		return userGroup.getUserGroupId();
	}

	@Override
	protected int getStatus(String externalReferenceCode, long groupId)
		throws Exception {

		UserGroup userGroup = _getUserGroup(externalReferenceCode, groupId);

		return userGroup.getStatus();
	}

	@Override
	protected boolean hasEntry(String externalReferenceCode, long groupId) {
		Group group = _groupLocalService.fetchGroup(groupId);

		if (group == null) {
			return false;
		}

		UserGroup userGroup =
			_userGroupLocalService.fetchUserGroupByExternalReferenceCode(
				externalReferenceCode, group.getCompanyId());

		if (userGroup == null) {
			return false;
		}

		return true;
	}

	@Override
	protected boolean supportsComments() {
		return false;
	}

	@Override
	protected boolean supportsEmptyEntries() {
		return true;
	}

	@Override
	protected boolean supportsPermissions() {
		return true;
	}

	@Override
	protected void updateCreatorUserId(
			String externalReferenceCode, long userId)
		throws Exception {

		Group group = getGroup();

		UserGroup userGroup = _getUserGroup(
			externalReferenceCode, group.getGroupId());

		userGroup.setUserId(userId);

		_userGroupLocalService.updateUserGroup(userGroup);
	}

	@Override
	protected void updateEntry(String externalReferenceCode) throws Exception {
		Group group = getGroup();

		UserGroup userGroup = _getUserGroup(
			externalReferenceCode, group.getGroupId());

		_userGroupLocalService.updateUserGroup(
			userGroup.getExternalReferenceCode(), userGroup.getCompanyId(),
			userGroup.getUserGroupId(), userGroup.getName(),
			RandomTestUtil.randomString(),
			ServiceContextTestUtil.getServiceContext(
				userGroup.getCompanyId(), group.getGroupId(),
				userGroup.getUserId()));
	}

	@Override
	protected void updateModifiedDate(
			String externalReferenceCode, Date modifiedDate)
		throws Exception {

		Group group = getGroup();

		UserGroup userGroup = _getUserGroup(
			externalReferenceCode, group.getGroupId());

		userGroup.setModifiedDate(modifiedDate);

		_userGroupLocalService.updateUserGroup(userGroup);
	}

	private UserGroup _getUserGroup(String externalReferenceCode, long groupId)
		throws Exception {

		Group group = _groupLocalService.getGroup(groupId);

		return _userGroupLocalService.getUserGroupByExternalReferenceCode(
			externalReferenceCode, group.getCompanyId());
	}

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject
	private UserGroupLocalService _userGroupLocalService;

}