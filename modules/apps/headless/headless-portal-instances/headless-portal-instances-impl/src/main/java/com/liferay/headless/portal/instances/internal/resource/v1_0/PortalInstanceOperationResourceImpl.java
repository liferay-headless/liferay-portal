/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.portal.instances.internal.resource.v1_0;

import com.liferay.headless.portal.instances.dto.v1_0.PortalInstanceOperation;
import com.liferay.headless.portal.instances.internal.dto.v1_0.util.PortalInstanceOperationUtil;
import com.liferay.headless.portal.instances.resource.v1_0.PortalInstanceOperationResource;
import com.liferay.portal.instances.background.task.constants.PortalInstanceBackgroundTaskExecutorNames;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.util.StringUtil;

import jakarta.ws.rs.NotFoundException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Luis Ortiz
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/portal-instance-operation.properties",
	scope = ServiceScope.PROTOTYPE,
	service = PortalInstanceOperationResource.class
)
public class PortalInstanceOperationResourceImpl
	extends BasePortalInstanceOperationResourceImpl {

	@Override
	public PortalInstanceOperation getPortalInstanceOperation(
			Long backgroundTaskId)
		throws Exception {

		_checkPermission();

		BackgroundTask backgroundTask =
			_backgroundTaskManager.fetchBackgroundTask(backgroundTaskId);

		if (backgroundTask == null) {
			throw new NotFoundException(
				"No portal instance operation exists with background task ID " +
					backgroundTaskId);
		}

		return PortalInstanceOperationUtil.toPortalInstanceOperation(
			backgroundTask, _jsonFactory, _language,
			contextAcceptLanguage.getPreferredLocale(),
			_getOperationType(backgroundTask));
	}

	private void _checkPermission() throws Exception {
		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		if (!permissionChecker.isOmniadmin()) {
			throw new PrincipalException.MustBeOmniadmin(permissionChecker);
		}
	}

	private PortalInstanceOperation.OperationType _getOperationType(
		BackgroundTask backgroundTask) {

		if (StringUtil.equals(
				backgroundTask.getTaskExecutorClassName(),
				PortalInstanceBackgroundTaskExecutorNames.
					ADD_PORTAL_INSTANCE_BACKGROUND_TASK_EXECUTOR)) {

			return PortalInstanceOperation.OperationType.ADD;
		}

		throw new NotFoundException(
			"Background task " + backgroundTask.getBackgroundTaskId() +
				" is not a portal instance operation");
	}

	@Reference
	private BackgroundTaskManager _backgroundTaskManager;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Language _language;

}