/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.rest.internal.resource.v1_0;

import com.liferay.launch.constants.LaunchStatusConstants;
import com.liferay.launch.rest.dto.v1_0.LaunchPublishStatus;
import com.liferay.launch.rest.resource.v1_0.LaunchPublishStatusResource;
import com.liferay.launch.set.publisher.LaunchSetPublisher;
import com.liferay.object.exception.NoSuchObjectEntryException;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryService;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Alberto Moreno
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/launch-publish-status.properties",
	scope = ServiceScope.PROTOTYPE, service = LaunchPublishStatusResource.class
)
public class LaunchPublishStatusResourceImpl
	extends BaseLaunchPublishStatusResourceImpl {

	@Override
	public LaunchPublishStatus getLaunchPublishStatus(Long launchId)
		throws Exception {

		ObjectEntry objectEntry = _objectEntryService.getObjectEntry(launchId);

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.getObjectDefinition(
				objectEntry.getObjectDefinitionId());

		if (!Objects.equals(
				objectDefinition.getExternalReferenceCode(), "L_LAUNCH_SET")) {

			throw new NoSuchObjectEntryException(
				"No launch set exists with ID " + launchId);
		}

		BackgroundTask backgroundTask =
			_launchSetPublisher.fetchPublishBackgroundTask(launchId);

		String statusLabel = _getStatusLabel(backgroundTask, objectEntry);

		return new LaunchPublishStatus() {
			{
				setErrorMessage(
					() -> {
						if (!Objects.equals(
								statusLabel,
								LaunchStatusConstants.LABEL_FAILED)) {

							return null;
						}

						return backgroundTask.getStatusMessage();
					});
				setLabel(() -> statusLabel);
				setLabel_i18n(
					() -> _language.get(
						contextAcceptLanguage.getPreferredLocale(),
						statusLabel));
			}
		};
	}

	private String _getStatusLabel(
		BackgroundTask backgroundTask, ObjectEntry objectEntry) {

		if ((backgroundTask != null) && !backgroundTask.isCompleted()) {
			return LaunchStatusConstants.LABEL_PUBLISHING;
		}

		if (objectEntry.getStatus() == WorkflowConstants.STATUS_SCHEDULED) {
			return LaunchStatusConstants.LABEL_SCHEDULED;
		}

		if (objectEntry.getStatus() == WorkflowConstants.STATUS_APPROVED) {
			return LaunchStatusConstants.LABEL_PUBLISHED;
		}

		if ((backgroundTask != null) &&
			(backgroundTask.getStatus() ==
				BackgroundTaskConstants.STATUS_FAILED)) {

			return LaunchStatusConstants.LABEL_FAILED;
		}

		return LaunchStatusConstants.LABEL_IN_PROGRESS;
	}

	@Reference
	private Language _language;

	@Reference
	private LaunchSetPublisher _launchSetPublisher;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryService _objectEntryService;

}