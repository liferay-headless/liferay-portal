/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.portal.instances.internal.dto.v1_0.util;

import com.liferay.headless.portal.instances.dto.v1_0.PortalInstanceOperation;
import com.liferay.portal.instances.background.task.PortalInstanceOperationType;
import com.liferay.portal.instances.background.task.constants.PortalInstanceBackgroundTaskConstants;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.Serializable;

import java.util.Locale;
import java.util.Map;

/**
 * @author Luis Ortiz
 */
public class PortalInstanceOperationUtil {

	public static PortalInstanceOperation toPortalInstanceOperation(
		BackgroundTask backgroundTask, JSONFactory jsonFactory,
		Language language, Locale locale,
		PortalInstanceOperationType portalInstanceOperationType) {

		PortalInstanceOperation portalInstanceOperation =
			new PortalInstanceOperation();

		JSONObject statusMessageJSONObject = jsonFactory.safeCreateJSONObject(
			backgroundTask.getStatusMessage(), true);

		Map<String, Serializable> taskContextMap =
			backgroundTask.getTaskContextMap();

		portalInstanceOperation.setBackgroundTaskId(
			backgroundTask::getBackgroundTaskId);
		portalInstanceOperation.setCompanyId(
			() -> _getCompanyId(statusMessageJSONObject));
		portalInstanceOperation.setCompletionDate(
			backgroundTask::getCompletionDate);
		portalInstanceOperation.setCreateDate(backgroundTask::getCreateDate);
		portalInstanceOperation.setErrorMessage(
			() -> _getErrorMessage(language, locale, taskContextMap));
		portalInstanceOperation.setOperationType(
			() -> PortalInstanceOperation.OperationType.create(
				StringUtil.toUpperCase(
					portalInstanceOperationType.getValue())));
		portalInstanceOperation.setPortalInstanceId(
			() -> GetterUtil.getString(
				taskContextMap.get(
					PortalInstanceBackgroundTaskConstants.WEB_ID),
				null));
		portalInstanceOperation.setStatus(
			() -> _getStatus(backgroundTask.getStatus()));

		return portalInstanceOperation;
	}

	private static Long _getCompanyId(JSONObject statusMessageJSONObject) {
		if (statusMessageJSONObject == null) {
			return null;
		}

		long companyId = statusMessageJSONObject.getLong(
			PortalInstanceBackgroundTaskConstants.COMPANY_ID);

		if (companyId <= 0) {
			return null;
		}

		return companyId;
	}

	private static String _getErrorMessage(
		Language language, Locale locale,
		Map<String, Serializable> taskContextMap) {

		String errorMessageKey = GetterUtil.getString(
			taskContextMap.get(
				PortalInstanceBackgroundTaskConstants.ERROR_MESSAGE_KEY),
			null);

		if (Validator.isNull(errorMessageKey)) {
			return null;
		}

		return language.get(locale, errorMessageKey);
	}

	private static PortalInstanceOperation.Status _getStatus(int status) {
		if (status == BackgroundTaskConstants.STATUS_CANCELLED) {
			return PortalInstanceOperation.Status.CANCELLED;
		}
		else if (status ==
					BackgroundTaskConstants.STATUS_COMPLETED_WITH_ERRORS) {

			return PortalInstanceOperation.Status.COMPLETED_WITH_ERRORS;
		}
		else if (status == BackgroundTaskConstants.STATUS_FAILED) {
			return PortalInstanceOperation.Status.FAILED;
		}
		else if (status == BackgroundTaskConstants.STATUS_IN_PROGRESS) {
			return PortalInstanceOperation.Status.IN_PROGRESS;
		}
		else if (status == BackgroundTaskConstants.STATUS_NEW) {
			return PortalInstanceOperation.Status.NEW;
		}
		else if (status == BackgroundTaskConstants.STATUS_QUEUED) {
			return PortalInstanceOperation.Status.QUEUED;
		}
		else if (status == BackgroundTaskConstants.STATUS_SUCCESSFUL) {
			return PortalInstanceOperation.Status.SUCCESSFUL;
		}

		return null;
	}

}