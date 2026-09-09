/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.portal.instances.internal.resource.v1_0;

import com.liferay.headless.portal.instances.dto.v1_0.Admin;
import com.liferay.headless.portal.instances.dto.v1_0.PortalInstance;
import com.liferay.headless.portal.instances.dto.v1_0.PortalInstanceOperation;
import com.liferay.headless.portal.instances.internal.dto.v1_0.util.PortalInstanceOperationUtil;
import com.liferay.headless.portal.instances.resource.v1_0.AsyncPortalInstanceResource;
import com.liferay.portal.instances.background.task.PortalInstanceOperationType;
import com.liferay.portal.instances.background.task.constants.PortalInstanceBackgroundTaskConstants;
import com.liferay.portal.instances.background.task.constants.PortalInstanceBackgroundTaskExecutorNames;
import com.liferay.portal.instances.exception.PortalInstanceAlreadyBeingAddedException;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.encryptor.Encryptor;
import com.liferay.portal.kernel.encryptor.EncryptorException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.exception.UserEmailAddressException;
import com.liferay.portal.kernel.exception.UserScreenNameException;
import com.liferay.portal.kernel.instance.PortalInstancePool;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.security.auth.EmailAddressValidator;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.auth.EmailAddressValidatorFactory;
import com.liferay.portal.vulcan.status.Status;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

import java.io.Serializable;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Luis Ortiz
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/async-portal-instance.properties",
	scope = ServiceScope.PROTOTYPE, service = AsyncPortalInstanceResource.class
)
public class AsyncPortalInstanceResourceImpl
	extends BaseAsyncPortalInstanceResourceImpl {

	@Override
	@Status(Response.Status.ACCEPTED)
	public PortalInstanceOperation postAsyncPortalInstance(
			PortalInstance portalInstance)
		throws Exception {

		_checkPermission();

		Admin admin = portalInstance.getAdmin();

		if (admin != null) {
			_validateAdmin(admin);
		}

		return PortalInstanceOperationUtil.toPortalInstanceOperation(
			_addPortalInstance(admin, portalInstance), _jsonFactory, _language,
			contextAcceptLanguage.getPreferredLocale(),
			PortalInstanceOperationType.ADD);
	}

	private BackgroundTask _addPortalInstance(
			Admin admin, PortalInstance portalInstance)
		throws Exception {

		String webId = portalInstance.getPortalInstanceId();
		String virtualHostname = StringUtil.toLowerCase(
			StringUtil.trim(portalInstance.getVirtualHost()));
		String mx = portalInstance.getDomain();
		int maxUsers = GetterUtil.getInteger(portalInstance.getMaxUsers());

		_companyLocalService.validateCompany(
			webId, virtualHostname, mx, maxUsers);

		String name = "addPortalInstance-" + webId;

		int count = _backgroundTaskManager.getBackgroundTasksCount(
			BackgroundTaskConstants.GROUP_ID_DEFAULT, name,
			PortalInstanceBackgroundTaskExecutorNames.
				ADD_PORTAL_INSTANCE_BACKGROUND_TASK_EXECUTOR,
			false);

		if (count > 0) {
			throw new ClientErrorException(
				"Portal instance " + webId + " is already being added",
				Response.Status.CONFLICT,
				new PortalInstanceAlreadyBeingAddedException(
					"Portal instance " + webId + " is already being added"));
		}

		String defaultAdminEmailAddress = null;
		String defaultAdminFirstName = null;
		String defaultAdminLastName = null;
		String defaultAdminMiddleName = null;
		String defaultAdminPassword = null;
		String defaultAdminScreenName = null;

		if (admin != null) {
			defaultAdminEmailAddress = admin.getEmailAddress();
			defaultAdminFirstName = admin.getGivenName();
			defaultAdminLastName = admin.getFamilyName();
			defaultAdminMiddleName = admin.getMiddleName();
			defaultAdminPassword = _encryptDefaultAdminPassword(
				admin.getPassword());
			defaultAdminScreenName = admin.getScreenName();
		}

		return _backgroundTaskManager.addBackgroundTask(
			contextUser.getUserId(), BackgroundTaskConstants.GROUP_ID_DEFAULT,
			name,
			PortalInstanceBackgroundTaskExecutorNames.
				ADD_PORTAL_INSTANCE_BACKGROUND_TASK_EXECUTOR,
			HashMapBuilder.<String, Serializable>put(
				PortalInstanceBackgroundTaskConstants.ACTIVE,
				GetterUtil.getBoolean(portalInstance.getActive(), true)
			).put(
				PortalInstanceBackgroundTaskConstants.
					DEFAULT_ADMIN_EMAIL_ADDRESS,
				defaultAdminEmailAddress
			).put(
				PortalInstanceBackgroundTaskConstants.DEFAULT_ADMIN_FIRST_NAME,
				defaultAdminFirstName
			).put(
				PortalInstanceBackgroundTaskConstants.DEFAULT_ADMIN_LAST_NAME,
				defaultAdminLastName
			).put(
				PortalInstanceBackgroundTaskConstants.DEFAULT_ADMIN_MIDDLE_NAME,
				defaultAdminMiddleName
			).put(
				PortalInstanceBackgroundTaskConstants.DEFAULT_ADMIN_PASSWORD,
				defaultAdminPassword
			).put(
				PortalInstanceBackgroundTaskConstants.DEFAULT_ADMIN_SCREEN_NAME,
				defaultAdminScreenName
			).put(
				PortalInstanceBackgroundTaskConstants.MAX_USERS, maxUsers
			).put(
				PortalInstanceBackgroundTaskConstants.MX, mx
			).put(
				PortalInstanceBackgroundTaskConstants.SITE_INITIALIZER_KEY,
				portalInstance.getSiteInitializerKey()
			).put(
				PortalInstanceBackgroundTaskConstants.VIRTUAL_HOSTNAME,
				virtualHostname
			).put(
				PortalInstanceBackgroundTaskConstants.WEB_ID, webId
			).build(),
			new ServiceContext());
	}

	private void _checkPermission() throws Exception {
		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		if (!permissionChecker.isOmniadmin()) {
			throw new PrincipalException.MustBeOmniadmin(permissionChecker);
		}
	}

	private String _encryptDefaultAdminPassword(String defaultAdminPassword)
		throws Exception {

		if (Validator.isNull(defaultAdminPassword)) {
			return null;
		}

		Company company = _companyLocalService.getCompany(
			PortalInstancePool.getDefaultCompanyId());

		try {
			return _encryptor.encrypt(
				company.getKeyObj(), defaultAdminPassword);
		}
		catch (EncryptorException encryptorException) {
			throw new SystemException(encryptorException);
		}
	}

	private void _validateAdmin(Admin admin) throws Exception {
		if (Validator.isNull(admin.getEmailAddress()) ||
			Validator.isNull(admin.getFamilyName()) ||
			Validator.isNull(admin.getGivenName())) {

			throw new UserScreenNameException.MustNotBeNull();
		}

		EmailAddressValidator emailAddressValidator =
			EmailAddressValidatorFactory.getInstance();

		if (!emailAddressValidator.validate(0, admin.getEmailAddress())) {
			throw new UserEmailAddressException.MustValidate(
				admin.getEmailAddress(), emailAddressValidator);
		}
	}

	@Reference
	private BackgroundTaskManager _backgroundTaskManager;

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private Encryptor _encryptor;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Language _language;

}