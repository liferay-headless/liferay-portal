/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.internal.model.listener;

import com.liferay.launch.internal.constants.LaunchDestinationNames;
import com.liferay.launch.internal.set.scheduler.util.LaunchSetSchedulerUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.scheduler.SchedulerEngineHelper;
import com.liferay.portal.kernel.scheduler.SchedulerException;
import com.liferay.portal.kernel.scheduler.StorageType;

import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jose Luis Navarro
 */
@Component(service = ModelListener.class)
public class LaunchSetObjectEntryModelListener
	extends BaseModelListener<ObjectEntry> {

	@Override
	public void onBeforeRemove(ObjectEntry objectEntry)
		throws ModelListenerException {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.fetchObjectDefinition(
				objectEntry.getObjectDefinitionId());

		if ((objectDefinition == null) ||
			!Objects.equals(
				objectDefinition.getExternalReferenceCode(), "L_LAUNCH_SET")) {

			return;
		}

		try {
			_schedulerEngineHelper.delete(
				LaunchSetSchedulerUtil.getJobName(objectEntry),
				LaunchDestinationNames.LAUNCH_SET_SCHEDULED_PUBLISH,
				StorageType.PERSISTED);
		}
		catch (SchedulerException schedulerException) {
			throw new ModelListenerException(schedulerException);
		}
	}

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private SchedulerEngineHelper _schedulerEngineHelper;

}