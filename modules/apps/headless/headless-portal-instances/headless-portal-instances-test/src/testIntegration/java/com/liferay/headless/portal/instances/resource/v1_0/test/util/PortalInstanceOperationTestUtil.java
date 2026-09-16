/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.portal.instances.resource.v1_0.test.util;

import com.liferay.headless.portal.instances.client.dto.v1_0.PortalInstanceOperation;
import com.liferay.headless.portal.instances.client.resource.v1_0.PortalInstanceOperationResource;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PropsValues;

/**
 * @author Luis Ortiz
 */
public class PortalInstanceOperationTestUtil {

	public static PortalInstanceOperationResource
			getPortalInstanceOperationResource(Company company)
		throws Exception {

		User user = UserTestUtil.getAdminUser(company.getCompanyId());

		return PortalInstanceOperationResource.builder(
		).authentication(
			user.getEmailAddress(), PropsValues.DEFAULT_ADMIN_PASSWORD
		).endpoint(
			company.getVirtualHostname(), PortalUtil.getPortalServerPort(false),
			"http"
		).locale(
			LocaleUtil.getDefault()
		).build();
	}

	public static BackgroundTask waitForCompletion(
			long backgroundTaskId, BackgroundTaskManager backgroundTaskManager)
		throws Exception {

		long endTime = System.currentTimeMillis() + _TIMEOUT;

		while (System.currentTimeMillis() < endTime) {
			BackgroundTask backgroundTask =
				backgroundTaskManager.fetchBackgroundTask(backgroundTaskId);

			if ((backgroundTask != null) && backgroundTask.isCompleted()) {
				return backgroundTask;
			}

			Thread.sleep(_POLL_INTERVAL);
		}

		throw new AssertionError(
			"Background task " + backgroundTaskId + " did not complete");
	}

	public static PortalInstanceOperation waitForCompletion(
			long backgroundTaskId,
			PortalInstanceOperationResource portalInstanceOperationResource)
		throws Exception {

		long endTime = System.currentTimeMillis() + _TIMEOUT;

		while (System.currentTimeMillis() < endTime) {
			PortalInstanceOperation portalInstanceOperation =
				portalInstanceOperationResource.getPortalInstanceOperation(
					backgroundTaskId);

			if (portalInstanceOperation.getCompletionDate() != null) {
				return portalInstanceOperation;
			}

			Thread.sleep(_POLL_INTERVAL);
		}

		throw new AssertionError(
			"Portal instance operation " + backgroundTaskId +
				" did not complete");
	}

	private static final long _POLL_INTERVAL = 1000;

	private static final long _TIMEOUT = 600000;

}