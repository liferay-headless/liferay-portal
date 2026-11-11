/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.rest.internal.resource.v1_0;

import com.liferay.launch.rest.resource.v1_0.LaunchResource;
import com.liferay.launch.set.publisher.LaunchSetPublishException;
import com.liferay.launch.set.publisher.LaunchSetPublisher;
import com.liferay.object.exception.NoSuchObjectEntryException;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import jakarta.ws.rs.core.Response;

import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Alberto Moreno
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/launch.properties",
	scope = ServiceScope.PROTOTYPE, service = LaunchResource.class
)
public class LaunchResourceImpl extends BaseLaunchResourceImpl {

	@Override
	public Response postLaunchPublish(Long launchId) throws Exception {
		ObjectEntry objectEntry = _objectEntryLocalService.getObjectEntry(
			launchId);

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.getObjectDefinition(
				objectEntry.getObjectDefinitionId());

		if (!Objects.equals(
				objectDefinition.getExternalReferenceCode(), "L_LAUNCH_SET")) {

			throw new NoSuchObjectEntryException(
				"No launch set exists with ID " + launchId);
		}

		if (objectEntry.getStatus() == WorkflowConstants.STATUS_SCHEDULED) {
			throw new LaunchSetPublishException(
				"Launch set " + launchId + " is scheduled to be published");
		}

		_launchSetPublisher.publish(launchId, contextUser.getUserId());

		return Response.accepted(
		).build();
	}

	@Reference
	private LaunchSetPublisher _launchSetPublisher;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

}