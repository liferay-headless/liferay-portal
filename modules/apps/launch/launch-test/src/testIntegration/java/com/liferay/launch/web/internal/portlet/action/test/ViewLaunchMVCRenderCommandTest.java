/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.web.internal.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.journal.constants.JournalFolderConstants;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.test.util.JournalTestUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.ResourcePermission;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletRenderRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletRenderResponse;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

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
 * @author Jose Luis Navarro
 */
@RunWith(Arquillian.class)
public class ViewLaunchMVCRenderCommandTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		ServiceContextThreadLocal.pushServiceContext(
			ServiceContextTestUtil.getServiceContext(
				_group.getGroupId(), TestPropsValues.getUserId()));

		_launchSetObjectEntry = _addObjectEntry(
			"LaunchSet",
			HashMapBuilder.<String, Serializable>put(
				"name", RandomTestUtil.randomString()
			).build());

		_journalArticle = JournalTestUtil.addArticle(
			_group.getGroupId(),
			JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID);

		_addObjectEntry(
			"LaunchEntry",
			HashMapBuilder.<String, Serializable>put(
				"baseVersionKey", String.valueOf(_journalArticle.getVersion())
			).put(
				"className", JournalArticle.class.getName()
			).put(
				"classPK", _journalArticle.getResourcePrimKey()
			).put(
				"r_launchSetToLaunchEntries_c_launchSetId",
				_launchSetObjectEntry.getObjectEntryId()
			).build());

		_removeViewPermissions(
			JournalArticle.class.getName(),
			_journalArticle.getResourcePrimKey());

		_removeViewPermissions(
			_getClassName("LaunchSet"),
			_launchSetObjectEntry.getObjectEntryId());

		_role = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);
		_user = UserTestUtil.addUser();

		_roleLocalService.addUserRole(_user.getUserId(), _role.getRoleId());
	}

	@After
	public void tearDown() throws Exception {
		ServiceContextThreadLocal.popServiceContext();
	}

	@Test
	public void testRender() throws Exception {

		// Cannot view the launch

		_setViewPermission(
			JournalArticle.class.getName(),
			_journalArticle.getResourcePrimKey());

		Assert.assertFalse(_isPreviewable());

		// Can view the launch but not one of its articles

		_removeViewPermissions(
			JournalArticle.class.getName(),
			_journalArticle.getResourcePrimKey());
		_setViewPermission(
			_getClassName("LaunchSet"),
			_launchSetObjectEntry.getObjectEntryId());

		Assert.assertFalse(_isPreviewable());

		// Can view everything

		_setViewPermission(
			JournalArticle.class.getName(),
			_journalArticle.getResourcePrimKey());

		Assert.assertTrue(_isPreviewable());
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

	private String _getClassName(String objectDefinitionName) throws Exception {
		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.getObjectDefinition(
				TestPropsValues.getCompanyId(), objectDefinitionName);

		return objectDefinition.getClassName();
	}

	private boolean _isPreviewable() throws Exception {
		MockLiferayPortletRenderRequest mockLiferayPortletRenderRequest =
			new MockLiferayPortletRenderRequest();

		mockLiferayPortletRenderRequest.setParameter(
			"launchSetId",
			String.valueOf(_launchSetObjectEntry.getObjectEntryId()));

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			_companyLocalService.getCompany(TestPropsValues.getCompanyId()));
		themeDisplay.setLocale(LocaleUtil.US);
		themeDisplay.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(_user));
		themeDisplay.setScopeGroupId(TestPropsValues.getGroupId());
		themeDisplay.setSiteGroupId(TestPropsValues.getGroupId());
		themeDisplay.setUser(_user);

		mockLiferayPortletRenderRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		_mvcRenderCommand.render(
			mockLiferayPortletRenderRequest,
			new MockLiferayPortletRenderResponse());

		return ReflectionTestUtil.invoke(
			mockLiferayPortletRenderRequest.getAttribute(
				"VIEW_LAUNCH_DISPLAY_CONTEXT"),
			"isPreviewable", new Class<?>[0]);
	}

	private void _removeViewPermissions(String name, long primKey)
		throws Exception {

		for (ResourcePermission resourcePermission :
				_resourcePermissionLocalService.getResourcePermissions(
					TestPropsValues.getCompanyId(), name,
					ResourceConstants.SCOPE_INDIVIDUAL,
					String.valueOf(primKey))) {

			_resourcePermissionLocalService.removeResourcePermission(
				TestPropsValues.getCompanyId(), name,
				ResourceConstants.SCOPE_INDIVIDUAL, String.valueOf(primKey),
				resourcePermission.getRoleId(), ActionKeys.VIEW);
		}
	}

	private void _setViewPermission(String name, long primKey)
		throws Exception {

		_resourcePermissionLocalService.setResourcePermissions(
			TestPropsValues.getCompanyId(), name,
			ResourceConstants.SCOPE_INDIVIDUAL, String.valueOf(primKey),
			_role.getRoleId(), new String[] {ActionKeys.VIEW});
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	@DeleteAfterTestRun
	private Group _group;

	private JournalArticle _journalArticle;
	private ObjectEntry _launchSetObjectEntry;

	@Inject(filter = "mvc.command.name=/launch/view_launch")
	private MVCRenderCommand _mvcRenderCommand;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	@DeleteAfterTestRun
	private Role _role;

	@Inject
	private RoleLocalService _roleLocalService;

	@DeleteAfterTestRun
	private User _user;

}