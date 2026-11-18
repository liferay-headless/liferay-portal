/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.internal.servlet.filter.test;

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
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
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

		_launchEntryObjectEntry = _addObjectEntry(
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
	public void testProcessFilter() throws Exception {

		// Signed out

		MockFilterChain mockFilterChain = new MockFilterChain();
		MockHttpServletResponse mockHttpServletResponse =
			new MockHttpServletResponse();

		_processFilter(
			mockFilterChain,
			_getMockHttpServletRequest(
				_launchSetObjectEntry.getObjectEntryId(), null),
			mockHttpServletResponse);

		Assert.assertNull(mockFilterChain.getRequest());

		String redirectedURL = mockHttpServletResponse.getRedirectedUrl();

		Assert.assertEquals(
			_portal.getPathMain() + "/portal/login",
			HttpComponentsUtil.getPath(redirectedURL));
		Assert.assertEquals(
			"http://localhost/web/guest/home?p_l_mode=preview&" +
				"previewLaunchSetId=" +
					_launchSetObjectEntry.getObjectEntryId(),
			HttpComponentsUtil.decodeURL(
				HttpComponentsUtil.getParameter(
					redirectedURL, "redirect", false)));

		// Cannot view the launch

		_setViewPermission(
			JournalArticle.class.getName(),
			_journalArticle.getResourcePrimKey());

		_assertForbidden(_launchSetObjectEntry.getObjectEntryId(), _user);

		// Cannot view the launch, forwarded to the status page

		MockHttpServletRequest mockHttpServletRequest =
			_getMockHttpServletRequest(
				_launchSetObjectEntry.getObjectEntryId(), _user);

		mockHttpServletRequest.setAttribute(
			WebKeys.PORTAL_STATUS_EXCEPTION,
			new PrincipalException(RandomTestUtil.randomString()));

		_assertProcessed(mockHttpServletRequest);

		// Can view the launch but not one of its articles

		_removeViewPermissions(
			JournalArticle.class.getName(),
			_journalArticle.getResourcePrimKey());
		_setViewPermission(
			_getClassName("LaunchSet"),
			_launchSetObjectEntry.getObjectEntryId());

		_assertProcessed(
			_getMockHttpServletRequest(
				_launchSetObjectEntry.getObjectEntryId(), _user));

		// No launch matches the ID

		_assertForbidden(RandomTestUtil.randomLong(), _user);

		// The ID matches an object entry that is not a launch

		_setViewPermission(
			_getClassName("LaunchEntry"),
			_launchEntryObjectEntry.getObjectEntryId());

		_assertForbidden(_launchEntryObjectEntry.getObjectEntryId(), _user);
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

		MockHttpServletRequest mockHttpServletRequest =
			_getMockHttpServletRequest(previewLaunchSetId, user);

		_processFilter(
			mockFilterChain, mockHttpServletRequest, mockHttpServletResponse);

		Assert.assertNull(mockFilterChain.getRequest());
		Assert.assertEquals(
			HttpServletResponse.SC_FORBIDDEN,
			mockHttpServletResponse.getStatus());

		Exception exception = (Exception)mockHttpServletRequest.getAttribute(
			WebKeys.PORTAL_STATUS_EXCEPTION);

		Assert.assertTrue(exception instanceof PrincipalException);
	}

	private void _assertProcessed(MockHttpServletRequest mockHttpServletRequest)
		throws Exception {

		MockFilterChain mockFilterChain = new MockFilterChain();
		MockHttpServletResponse mockHttpServletResponse =
			new MockHttpServletResponse();

		_processFilter(
			mockFilterChain, mockHttpServletRequest, mockHttpServletResponse);

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

	private MockHttpServletRequest _getMockHttpServletRequest(
		long previewLaunchSetId, User user) {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setParameter("p_l_mode", Constants.PREVIEW);
		mockHttpServletRequest.setParameter(
			"previewLaunchSetId", String.valueOf(previewLaunchSetId));
		mockHttpServletRequest.setQueryString(
			"p_l_mode=preview&previewLaunchSetId=" + previewLaunchSetId);
		mockHttpServletRequest.setRequestURI("/web/guest/home");

		if (user != null) {
			mockHttpServletRequest.setAttribute(WebKeys.USER, user);
		}

		return mockHttpServletRequest;
	}

	private void _processFilter(
			FilterChain filterChain,
			MockHttpServletRequest mockHttpServletRequest,
			MockHttpServletResponse mockHttpServletResponse)
		throws Exception {

		ReflectionTestUtil.invoke(
			_filter, "processFilter",
			new Class<?>[] {
				HttpServletRequest.class, HttpServletResponse.class,
				FilterChain.class
			},
			mockHttpServletRequest, mockHttpServletResponse, filterChain);
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
	private ObjectEntry _launchEntryObjectEntry;
	private ObjectEntry _launchSetObjectEntry;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	@Inject
	private Portal _portal;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	@DeleteAfterTestRun
	private Role _role;

	@Inject
	private RoleLocalService _roleLocalService;

	@DeleteAfterTestRun
	private User _user;

}