/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.internal.servlet.filter.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.journal.constants.JournalFolderConstants;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
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
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.Serializable;

import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Jose Luis Navarro
 */
@FeatureFlag("LPD-72278")
@RunWith(Arquillian.class)
public class LaunchPreviewFilterTest {

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

		_addLaunchEntryObjectEntry(
			String.valueOf(_journalArticle.getVersion()),
			JournalArticle.class.getName(),
			_journalArticle.getResourcePrimKey());

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
	public void testProcessFilter() throws Exception {

		// Signed out

		_assertForbidden(_launchSetObjectEntry.getObjectEntryId(), null);

		// Cannot view the launch

		_setViewPermission(
			JournalArticle.class.getName(),
			_journalArticle.getResourcePrimKey());

		_assertForbidden(_launchSetObjectEntry.getObjectEntryId(), _user);

		// Can view the launch but not one of its articles

		_removeViewPermissions(
			JournalArticle.class.getName(),
			_journalArticle.getResourcePrimKey());
		_setViewPermission(
			_getClassName("LaunchSet"),
			_launchSetObjectEntry.getObjectEntryId());

		_assertForbidden(_launchSetObjectEntry.getObjectEntryId(), _user);

		// Can view everything

		_setViewPermission(
			JournalArticle.class.getName(),
			_journalArticle.getResourcePrimKey());

		_assertProcessed();

		// Can view everything, and the article of another entry no longer
		// exists

		JournalArticle journalArticle = JournalTestUtil.addArticle(
			_group.getGroupId(),
			JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID);

		_addLaunchEntryObjectEntry(
			String.valueOf(journalArticle.getVersion()),
			JournalArticle.class.getName(),
			journalArticle.getResourcePrimKey());

		_journalArticleLocalService.deleteArticle(journalArticle);

		_assertProcessed();

		// Can view everything, but another entry has an unknown type

		_addLaunchEntryObjectEntry(
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			RandomTestUtil.randomInt());

		_assertForbidden(_launchSetObjectEntry.getObjectEntryId(), _user);

		// No launch matches the ID

		_assertForbidden(RandomTestUtil.randomLong(), _user);
	}

	private ObjectEntry _addLaunchEntryObjectEntry(
			String baseVersionKey, String className, long classPK)
		throws Exception {

		return _addObjectEntry(
			"LaunchEntry",
			HashMapBuilder.<String, Serializable>put(
				"baseVersionKey", baseVersionKey
			).put(
				"className", className
			).put(
				"classPK", classPK
			).put(
				"r_launchSetToLaunchEntries_c_launchSetId",
				_launchSetObjectEntry.getObjectEntryId()
			).build());
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

	private void _assertForbidden(long previewLaunchSetId, User user)
		throws Exception {

		MockFilterChain mockFilterChain = new MockFilterChain();
		MockHttpServletResponse mockHttpServletResponse =
			new MockHttpServletResponse();

		MockHttpServletRequest mockHttpServletRequest = _processFilter(
			mockFilterChain, mockHttpServletResponse, previewLaunchSetId, user);

		Assert.assertNull(mockFilterChain.getRequest());
		Assert.assertEquals(
			HttpServletResponse.SC_FORBIDDEN,
			mockHttpServletResponse.getStatus());

		Exception exception = (Exception)mockHttpServletRequest.getAttribute(
			WebKeys.PORTAL_STATUS_EXCEPTION);

		Assert.assertTrue(exception instanceof PrincipalException);

		String message = exception.getMessage();

		Assert.assertFalse(message.contains(JournalArticle.class.getName()));
		Assert.assertFalse(
			message.contains(
				String.valueOf(_journalArticle.getResourcePrimKey())));
	}

	private void _assertProcessed() throws Exception {
		MockFilterChain mockFilterChain = new MockFilterChain();
		MockHttpServletResponse mockHttpServletResponse =
			new MockHttpServletResponse();

		_processFilter(
			mockFilterChain, mockHttpServletResponse,
			_launchSetObjectEntry.getObjectEntryId(), _user);

		Assert.assertNotNull(mockFilterChain.getRequest());
		Assert.assertEquals(
			HttpServletResponse.SC_OK, mockHttpServletResponse.getStatus());
	}

	private String _getClassName(String objectDefinitionName) throws Exception {
		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.getObjectDefinition(
				TestPropsValues.getCompanyId(), objectDefinitionName);

		return objectDefinition.getClassName();
	}

	private MockHttpServletRequest _processFilter(
			FilterChain filterChain,
			MockHttpServletResponse mockHttpServletResponse,
			long previewLaunchSetId, User user)
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setParameter("p_l_mode", Constants.PREVIEW);
		mockHttpServletRequest.setParameter(
			"previewLaunchSetId", String.valueOf(previewLaunchSetId));

		if (user != null) {
			mockHttpServletRequest.setAttribute(WebKeys.USER, user);
		}

		ReflectionTestUtil.invoke(
			_filter, "processFilter",
			new Class<?>[] {
				HttpServletRequest.class, HttpServletResponse.class,
				FilterChain.class
			},
			mockHttpServletRequest, mockHttpServletResponse, filterChain);

		return mockHttpServletRequest;
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

	@Inject(filter = "servlet-filter-name=Launch Preview Filter")
	private Filter _filter;

	@DeleteAfterTestRun
	private Group _group;

	private JournalArticle _journalArticle;

	@Inject
	private JournalArticleLocalService _journalArticleLocalService;

	private ObjectEntry _launchSetObjectEntry;

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