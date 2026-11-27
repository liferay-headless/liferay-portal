/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.jaxrs.exception.mapper.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.portal.kernel.exception.NoSuchModelException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.HTTPTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.vulcan.problem.Problem;
import com.liferay.portal.vulcan.problem.ProblemMapper;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

/**
 * @author Luis Ortiz
 */
@RunWith(Arquillian.class)
public class ExceptionMapperTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() {
		Bundle bundle = FrameworkUtil.getBundle(ExceptionMapperTest.class);

		BundleContext bundleContext = bundle.getBundleContext();

		_serviceRegistrations = Arrays.asList(
			bundleContext.registerService(
				Application.class, new ExceptionMapperTest.TestApplication(),
				HashMapDictionaryBuilder.<String, Object>put(
					"liferay.auth.verifier", true
				).put(
					"liferay.jackson", false
				).put(
					"liferay.oauth2", false
				).put(
					"osgi.jaxrs.application.base", "/test-vulcan"
				).put(
					"osgi.jaxrs.extension.select",
					"(osgi.jaxrs.name=Liferay.Vulcan)"
				).build()),
			bundleContext.registerService(
				ProblemMapper.class,
				new ExceptionMapperTest.TestExceptionProblemMapper(), null));
	}

	@After
	public void tearDown() {
		_serviceRegistrations.forEach(ServiceRegistration::unregister);
	}

	@Test
	public void testNoSuchModelExceptionAndPrincipalExceptionReturnNotFound()
		throws Exception {

		Assert.assertEquals(
			404,
			HTTPTestUtil.invokeToHttpCode(
				null, "/test-vulcan/testNoSuchModelException",
				Http.Method.GET));
		Assert.assertEquals(
			404,
			HTTPTestUtil.invokeToHttpCode(
				null, "/test-vulcan/testPrincipalException1", Http.Method.GET));

		JSONObject expectedJSONObject = JSONUtil.put("status", "NOT_FOUND");

		Assert.assertEquals(
			expectedJSONObject.toString(),
			HTTPTestUtil.invokeToJSONObject(
				null, "/test-vulcan/testPrincipalException1", Http.Method.GET
			).toString());
		Assert.assertEquals(
			expectedJSONObject.toString(),
			HTTPTestUtil.invokeToJSONObject(
				null, "/test-vulcan/testNoSuchModelException", Http.Method.GET
			).toString());
	}

	@Test
	@TestInfo("LPD-63186")
	public void testPrincipalExceptionReturnForbidden() throws Exception {
		Assert.assertEquals(
			403,
			HTTPTestUtil.invokeToHttpCode(
				null, "/test-vulcan/testPrincipalException2",
				Http.Method.POST));
		Assert.assertEquals(
			LanguageUtil.get(LocaleUtil.ENGLISH, "forbidden"),
			HTTPTestUtil.invokeToJSONObject(
				null, "/test-vulcan/testPrincipalException2",
				HashMapBuilder.put(
					"Accept-Language",
					() -> {
						Locale defaultLocale = LocaleUtil.getDefault();

						return defaultLocale.toLanguageTag();
					}
				).build(),
				Http.Method.POST
			).getString(
				"title"
			));
		Assert.assertEquals(
			LanguageUtil.get(LocaleUtil.ITALIAN, "forbidden"),
			HTTPTestUtil.invokeToJSONObject(
				null, "/test-vulcan/testPrincipalException2",
				HashMapBuilder.put(
					"Accept-Language", LocaleUtil.ITALIAN.toLanguageTag()
				).build(),
				Http.Method.POST
			).getString(
				"title"
			));
		Assert.assertEquals(
			LanguageUtil.get(LocaleUtil.JAPAN, "forbidden"),
			HTTPTestUtil.invokeToJSONObject(
				null, "/test-vulcan/testPrincipalException2",
				HashMapBuilder.put(
					"Accept-Language", LocaleUtil.JAPAN.toLanguageTag()
				).build(),
				Http.Method.POST
			).getString(
				"title"
			));
	}

	@Test
	@TestInfo("LPD-107137")
	public void testPrincipalExceptionWithViewActionReturnNotFound()
		throws Exception {

		Assert.assertEquals(
			404,
			HTTPTestUtil.invokeToHttpCode(
				null, "/test-vulcan/testPrincipalException4",
				Http.Method.POST));
		Assert.assertEquals(
			JSONUtil.put(
				"status", "NOT_FOUND"
			).toString(),
			HTTPTestUtil.invokeToJSONObject(
				null, "/test-vulcan/testPrincipalException4", Http.Method.POST
			).toString());
	}

	@Test
	@TestInfo("LPD-107137")
	public void testPrincipalExceptionWithViewDeniedReturnNotFound()
		throws Exception {

		_group = GroupTestUtil.addGroup();

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(_group.getGroupId());

		Folder folder1 = DLAppLocalServiceUtil.addFolder(
			null, TestPropsValues.getUserId(), _group.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			serviceContext);

		serviceContext.setAddGroupPermissions(false);
		serviceContext.setAddGuestPermissions(false);

		Folder folder2 = DLAppLocalServiceUtil.addFolder(
			null, TestPropsValues.getUserId(), _group.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			serviceContext);

		String password = RandomTestUtil.randomString();

		_user = UserTestUtil.addUser(
			CompanyLocalServiceUtil.getCompany(_group.getCompanyId()),
			password);

		HTTPTestUtil.customize(
		).withCredentials(
			_user.getEmailAddress(), password
		).apply(
			() -> {
				Assert.assertEquals(
					403,
					HTTPTestUtil.invokeToHttpCode(
						null,
						"/test-vulcan/testPrincipalException5/" +
							folder1.getFolderId(),
						Http.Method.POST));
				Assert.assertEquals(
					404,
					HTTPTestUtil.invokeToHttpCode(
						null,
						"/test-vulcan/testPrincipalException5/" +
							folder2.getFolderId(),
						Http.Method.POST));
			}
		);
	}

	@Test
	@TestInfo("LPD-107137")
	public void testPrincipalExceptionWithoutViewActionReturnForbidden()
		throws Exception {

		Assert.assertEquals(
			403,
			HTTPTestUtil.invokeToHttpCode(
				null, "/test-vulcan/testPrincipalException3",
				Http.Method.POST));
	}

	@Test
	public void testProblemMapperReturnBadRequestProblem() throws Exception {
		Assert.assertEquals(
			400,
			HTTPTestUtil.invokeToHttpCode(
				null, "/test-vulcan/testTestException1", Http.Method.GET));

		JSONObject expectedJSONObject = JSONUtil.put(
			"detail", _DETAIL
		).put(
			"status", "BAD_REQUEST"
		).put(
			"title", _TITLE
		).put(
			"type", _TYPE
		);

		JSONAssert.assertEquals(
			expectedJSONObject.toString(),
			HTTPTestUtil.invokeToString(
				null, "/test-vulcan/testTestException1", Http.Method.GET
			).toString(),
			JSONCompareMode.LENIENT);

		JSONAssert.assertEquals(
			expectedJSONObject.toString(),
			HTTPTestUtil.invokeToString(
				null, "/test-vulcan/testTestException2", Http.Method.GET
			).toString(),
			JSONCompareMode.LENIENT);
	}

	public static class TestApplication extends Application {

		@Override
		public Set<Object> getSingletons() {
			return Collections.singleton(this);
		}

		@GET
		@Path("/testPrincipalException1")
		@Produces("application/json")
		public String principalException1() throws PrincipalException {
			throw new PrincipalException();
		}

		@Path("/testPrincipalException2")
		@POST
		@Produces("application/json")
		public void principalException2() throws PrincipalException {
			throw new PrincipalException();
		}

		@Path("/testPrincipalException3")
		@POST
		@Produces("application/json")
		public void principalException3() throws PrincipalException {
			throw new PrincipalException.MustHavePermission(
				0, Group.class.getName(), RandomTestUtil.randomLong(),
				ActionKeys.UPDATE);
		}

		@Path("/testPrincipalException4")
		@POST
		@Produces("application/json")
		public void principalException4() throws PrincipalException {
			throw new PrincipalException.MustHavePermission(
				0, Group.class.getName(), RandomTestUtil.randomLong(),
				ActionKeys.VIEW);
		}

		@Path("/testPrincipalException5/{folderId}")
		@POST
		@Produces("application/json")
		public void principalException5(@PathParam("folderId") long folderId)
			throws PrincipalException {

			throw new PrincipalException.MustHavePermission(
				0, DLFolder.class.getName(), folderId, ActionKeys.UPDATE);
		}

		@GET
		@Path("/testNoSuchModelException")
		@Produces("application/json")
		public String testNoSuchModelException() throws NoSuchModelException {
			throw new NoSuchModelException();
		}

		@GET
		@Path("/testTestException1")
		@Produces("application/json")
		public String testTestException1() throws TestException {
			throw new TestException(RandomTestUtil.randomString());
		}

		@GET
		@Path("/testTestException2")
		@Produces("application/json")
		public String testTestException2() throws Exception {
			throw new Exception(
				new TestException(RandomTestUtil.randomString()));
		}

	}

	private static final String _DETAIL = RandomTestUtil.randomString();

	private static final String _TITLE = RandomTestUtil.randomString();

	private static final String _TYPE = RandomTestUtil.randomString();

	@DeleteAfterTestRun
	private Group _group;

	private List<ServiceRegistration<?>> _serviceRegistrations;

	@DeleteAfterTestRun
	private User _user;

	private static class TestException extends Exception {

		public TestException(String s) {
			super(s);
		}

	}

	private static class TestExceptionProblemMapper
		implements ProblemMapper<TestException> {

		@Override
		public Problem getProblem(TestException testException) {
			return new Problem() {

				@Override
				public String getDetail(Locale locale) {
					return _DETAIL;
				}

				@Override
				public Status getStatus() {
					return Status.BAD_REQUEST;
				}

				@Override
				public String getTitle(Locale locale) {
					return _TITLE;
				}

				@Override
				public String getType() {
					return _TYPE;
				}

			};
		}

	}

}