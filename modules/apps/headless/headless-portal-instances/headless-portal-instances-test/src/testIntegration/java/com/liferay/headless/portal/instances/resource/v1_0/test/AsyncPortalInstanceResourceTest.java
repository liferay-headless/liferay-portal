/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.portal.instances.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.counter.kernel.service.CounterLocalService;
import com.liferay.headless.portal.instances.client.dto.v1_0.Admin;
import com.liferay.headless.portal.instances.client.dto.v1_0.PortalInstance;
import com.liferay.headless.portal.instances.client.dto.v1_0.PortalInstanceOperation;
import com.liferay.headless.portal.instances.client.http.HttpInvoker;
import com.liferay.headless.portal.instances.client.problem.Problem;
import com.liferay.headless.portal.instances.client.resource.v1_0.AsyncPortalInstanceResource;
import com.liferay.headless.portal.instances.client.serdes.v1_0.PortalInstanceOperationSerDes;
import com.liferay.headless.portal.instances.resource.v1_0.test.util.PortalInstanceOperationTestUtil;
import com.liferay.portal.background.task.model.BackgroundTask;
import com.liferay.portal.background.task.service.BackgroundTaskLocalService;
import com.liferay.portal.instances.background.task.constants.PortalInstanceBackgroundTaskConstants;
import com.liferay.portal.instances.background.task.constants.PortalInstanceBackgroundTaskExecutorNames;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.Inject;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Luis Ortiz
 */
@RunWith(Arquillian.class)
public class AsyncPortalInstanceResourceTest
	extends BaseAsyncPortalInstanceResourceTestCase {

	@Override
	@Test
	public void testPostAsyncPortalInstance() throws Exception {
		_testPostAsyncPortalInstance();
		_testPostAsyncPortalInstanceWhenAddIsAlreadyRunning();
		_testPostAsyncPortalInstanceWithDuplicatePortalInstanceId();
		_testPostAsyncPortalInstanceWithInvalidAdminEmailAddress();
		_testPostAsyncPortalInstanceWithoutAdmin();
		_testPostAsyncPortalInstanceWithoutOmniadminPermission();
	}

	private Admin _randomAdminWithInvalidEmailAddress() {
		Admin admin = new Admin();

		admin.setEmailAddress(RandomTestUtil.randomString());
		admin.setFamilyName(RandomTestUtil.randomString());
		admin.setGivenName(RandomTestUtil.randomString());

		return admin;
	}

	private PortalInstance _randomPortalInstance() {
		String portalInstanceId = StringUtil.toLowerCase(
			RandomTestUtil.randomString());

		PortalInstance portalInstance = new PortalInstance();

		portalInstance.setActive(true);
		portalInstance.setDomain(portalInstanceId + ".com");
		portalInstance.setMaxUsers(_MAX_USERS);
		portalInstance.setPortalInstanceId(portalInstanceId);
		portalInstance.setVirtualHost(portalInstanceId + ".com");

		return portalInstance;
	}

	private void _testPostAsyncPortalInstance() throws Exception {
		PortalInstance portalInstance = _randomPortalInstance();

		String screenName = StringUtil.toLowerCase(
			RandomTestUtil.randomString());

		Admin admin = new Admin();

		admin.setEmailAddress(screenName + "@liferay.com");
		admin.setFamilyName(RandomTestUtil.randomString());
		admin.setGivenName(RandomTestUtil.randomString());
		admin.setMiddleName(RandomTestUtil.randomString());
		admin.setPassword(RandomTestUtil.randomString());
		admin.setScreenName(screenName);

		portalInstance.setAdmin(admin);

		HttpInvoker.HttpResponse httpResponse =
			asyncPortalInstanceResource.postAsyncPortalInstanceHttpResponse(
				portalInstance);

		assertHttpResponseStatusCode(202, httpResponse);

		PortalInstanceOperation portalInstanceOperation =
			PortalInstanceOperationSerDes.toDTO(httpResponse.getContent());

		Assert.assertNotNull(portalInstanceOperation.getBackgroundTaskId());
		Assert.assertEquals(
			PortalInstanceOperation.OperationType.ADD,
			portalInstanceOperation.getOperationType());

		portalInstanceOperation =
			PortalInstanceOperationTestUtil.waitForCompletion(
				portalInstanceOperation.getBackgroundTaskId(),
				PortalInstanceOperationTestUtil.
					getPortalInstanceOperationResource(testCompany));

		Assert.assertEquals(
			PortalInstanceOperation.Status.SUCCESSFUL,
			portalInstanceOperation.getStatus());
		Assert.assertNull(portalInstanceOperation.getErrorMessage());
		Assert.assertNotNull(portalInstanceOperation.getCompletionDate());

		_company = _companyLocalService.getCompanyByWebId(
			portalInstanceOperation.getPortalInstanceId());

		Assert.assertEquals(
			Long.valueOf(_company.getCompanyId()),
			portalInstanceOperation.getCompanyId());
		Assert.assertEquals(_MAX_USERS, _company.getMaxUsers());
		Assert.assertTrue(_company.isActive());

		User user = _userLocalService.getUserByScreenName(
			_company.getCompanyId(), screenName);

		Assert.assertEquals(admin.getEmailAddress(), user.getEmailAddress());
		Assert.assertEquals(admin.getGivenName(), user.getFirstName());
		Assert.assertEquals(admin.getMiddleName(), user.getMiddleName());
		Assert.assertEquals(admin.getFamilyName(), user.getLastName());
	}

	private void _testPostAsyncPortalInstanceWhenAddIsAlreadyRunning()
		throws Exception {

		PortalInstance portalInstance = _randomPortalInstance();

		String portalInstanceId = portalInstance.getPortalInstanceId();

		BackgroundTask backgroundTask =
			_backgroundTaskLocalService.createBackgroundTask(
				_counterLocalService.increment());

		backgroundTask.setGroupId(BackgroundTaskConstants.GROUP_ID_DEFAULT);
		backgroundTask.setCompanyId(TestPropsValues.getCompanyId());
		backgroundTask.setUserId(TestPropsValues.getUserId());
		backgroundTask.setName("addPortalInstance-" + portalInstanceId);
		backgroundTask.setTaskExecutorClassName(
			PortalInstanceBackgroundTaskExecutorNames.
				ADD_PORTAL_INSTANCE_BACKGROUND_TASK_EXECUTOR);
		backgroundTask.setTaskContextMap(
			HashMapBuilder.<String, Serializable>put(
				PortalInstanceBackgroundTaskConstants.WEB_ID, portalInstanceId
			).build());
		backgroundTask.setCompleted(false);
		backgroundTask.setStatus(BackgroundTaskConstants.STATUS_IN_PROGRESS);

		backgroundTask = _backgroundTaskLocalService.updateBackgroundTask(
			backgroundTask);

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				_CLASS_NAME_WEB_APPLICATION_EXCEPTION_MAPPER,
				LoggerTestUtil.ERROR)) {

			asyncPortalInstanceResource.postAsyncPortalInstance(portalInstance);

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("CONFLICT", problem.getStatus());
			Assert.assertEquals(
				"Portal instance " + portalInstanceId +
					" is already being added",
				problem.getTitle());
		}
		finally {
			_backgroundTaskLocalService.deleteBackgroundTask(backgroundTask);
		}
	}

	private void _testPostAsyncPortalInstanceWithDuplicatePortalInstanceId()
		throws Exception {

		PortalInstance portalInstance = _randomPortalInstance();

		portalInstance.setPortalInstanceId(testCompany.getWebId());

		try {
			asyncPortalInstanceResource.postAsyncPortalInstance(portalInstance);

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("BAD_REQUEST", problem.getStatus());
		}
	}

	private void _testPostAsyncPortalInstanceWithInvalidAdminEmailAddress()
		throws Exception {

		PortalInstance portalInstance = _randomPortalInstance();

		portalInstance.setAdmin(_randomAdminWithInvalidEmailAddress());

		try {
			asyncPortalInstanceResource.postAsyncPortalInstance(portalInstance);

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("BAD_REQUEST", problem.getStatus());
		}
	}

	private void _testPostAsyncPortalInstanceWithoutAdmin() throws Exception {
		PortalInstance portalInstance = _randomPortalInstance();

		PortalInstanceOperation portalInstanceOperation =
			asyncPortalInstanceResource.postAsyncPortalInstance(portalInstance);

		portalInstanceOperation =
			PortalInstanceOperationTestUtil.waitForCompletion(
				portalInstanceOperation.getBackgroundTaskId(),
				PortalInstanceOperationTestUtil.
					getPortalInstanceOperationResource(testCompany));

		Assert.assertEquals(
			PortalInstanceOperation.Status.SUCCESSFUL,
			portalInstanceOperation.getStatus());

		Company company = _companyLocalService.getCompanyByWebId(
			portalInstance.getPortalInstanceId());

		try {
			_userLocalService.getUserByScreenName(
				company.getCompanyId(), PropsValues.DEFAULT_ADMIN_SCREEN_NAME);
		}
		finally {
			_companyLocalService.deleteCompany(company);
		}
	}

	private void _testPostAsyncPortalInstanceWithoutOmniadminPermission()
		throws Exception {

		User user = UserTestUtil.addUser(testCompany, "test");

		AsyncPortalInstanceResource userPortalInstanceResource =
			AsyncPortalInstanceResource.builder(
			).authentication(
				user.getEmailAddress(), "test"
			).endpoint(
				testCompany.getVirtualHostname(),
				PortalUtil.getPortalServerPort(false), "http"
			).locale(
				LocaleUtil.getDefault()
			).build();

		PortalInstance portalInstance = _randomPortalInstance();

		portalInstance.setAdmin(_randomAdminWithInvalidEmailAddress());

		try {
			userPortalInstanceResource.postAsyncPortalInstance(portalInstance);

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("FORBIDDEN", problem.getStatus());
		}
	}

	private static final String _CLASS_NAME_WEB_APPLICATION_EXCEPTION_MAPPER =
		"com.liferay.portal.vulcan.internal.jaxrs.exception.mapper." +
			"WebApplicationExceptionMapper";

	private static final int _MAX_USERS = 42;

	@Inject
	private BackgroundTaskLocalService _backgroundTaskLocalService;

	@DeleteAfterTestRun
	private Company _company;

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private CounterLocalService _counterLocalService;

	@Inject
	private UserLocalService _userLocalService;

}