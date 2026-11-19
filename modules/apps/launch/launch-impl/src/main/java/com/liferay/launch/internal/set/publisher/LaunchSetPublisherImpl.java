/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.internal.set.publisher;

import com.liferay.launch.entry.manager.LaunchEntryManager;
import com.liferay.launch.entry.manager.LaunchEntryManagerRegistry;
import com.liferay.launch.set.publisher.LaunchSetPublisher;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowHandlerRegistryUtil;

import java.io.Serializable;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alejandro Tardín
 * @author Jose Luis Navarro
 */
@Component(service = LaunchSetPublisher.class)
public class LaunchSetPublisherImpl implements LaunchSetPublisher {

	@Override
	public void publish(long launchSetId) throws PortalException {
		ObjectEntry launchSetObjectEntry = _getLaunchSetObjectEntry(
			launchSetId);

		ObjectRelationship objectRelationship =
			_objectRelationshipLocalService.getObjectRelationship(
				launchSetObjectEntry.getObjectDefinitionId(),
				"launchSetToLaunchEntries");

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		for (ObjectEntry launchEntryObjectEntry :
				_objectEntryLocalService.getOneToManyObjectEntries(
					launchSetObjectEntry.getGroupId(),
					objectRelationship.getObjectRelationshipId(), null, false,
					launchSetObjectEntry.getObjectEntryId(), true, null,
					QueryUtil.ALL_POS, QueryUtil.ALL_POS, null)) {

			Map<String, Serializable> values =
				launchEntryObjectEntry.getValues();

			String className = MapUtil.getString(values, "className");

			LaunchEntryManager launchEntryManager =
				_launchEntryManagerRegistry.getLaunchEntryManager(className);

			if (launchEntryManager == null) {
				throw new PortalException(
					"No launch entry manager exists for " + className);
			}

			LaunchEntryManager.Version version =
				launchEntryManager.publishDraft(
					MapUtil.getLong(values, "classPK"),
					MapUtil.getLong(values, "draftId"));

			values.put("draftId", 0L);
			values.put("versionKey", version.getVersionKey());

			_objectEntryLocalService.updateObjectEntry(
				serviceContext.getUserId(),
				launchEntryObjectEntry.getObjectEntryId(),
				launchEntryObjectEntry.getObjectEntryFolderId(), values,
				serviceContext);
		}
	}

	@Override
	public void requestPublish(long launchSetId, long userId)
		throws PortalException {

		ObjectEntry launchSetObjectEntry = _getLaunchSetObjectEntry(
			launchSetId);

		if (launchSetObjectEntry.getStatus() ==
				WorkflowConstants.STATUS_APPROVED) {

			throw new PortalException(
				"Launch set " + launchSetId + " was already published");
		}

		if (launchSetObjectEntry.getStatus() ==
				WorkflowConstants.STATUS_PENDING) {

			throw new PortalException(
				"Launch set " + launchSetId + " is pending approval");
		}

		Date displayDate = launchSetObjectEntry.getDisplayDate();

		if (launchSetObjectEntry.getStatus() ==
				WorkflowConstants.STATUS_SCHEDULED) {

			Date date = new Date();

			if ((displayDate != null) && displayDate.after(date)) {
				launchSetObjectEntry.setDisplayDate(date);
			}

			_objectEntryLocalService.updateStatus(
				userId, launchSetObjectEntry, WorkflowConstants.STATUS_APPROVED,
				_getServiceContext(launchSetObjectEntry, userId));

			return;
		}

		if ((displayDate != null) &&
			(launchSetObjectEntry.getStatus() ==
				WorkflowConstants.STATUS_DRAFT)) {

			launchSetObjectEntry = _updateDisplayDate(
				null, launchSetObjectEntry);
		}

		_startWorkflowInstance(launchSetObjectEntry, userId);
	}

	@Override
	public void schedulePublish(Date date, long launchSetId, long userId)
		throws PortalException {

		ObjectEntry launchSetObjectEntry = _getLaunchSetObjectEntry(
			launchSetId);

		if (launchSetObjectEntry.getStatus() !=
				WorkflowConstants.STATUS_DRAFT) {

			throw new PortalException(
				"Launch set " + launchSetId + " must be open to be scheduled");
		}

		if ((date == null) || !date.after(new Date())) {
			throw new PortalException(
				"Launch set " + launchSetId +
					" must be scheduled for a future date");
		}

		_startWorkflowInstance(
			_updateDisplayDate(date, launchSetObjectEntry), userId);
	}

	@Override
	public void unschedulePublish(long launchSetId) throws PortalException {
		ObjectEntry launchSetObjectEntry = _getLaunchSetObjectEntry(
			launchSetId);

		if (launchSetObjectEntry.getStatus() ==
				WorkflowConstants.STATUS_PENDING) {

			throw new PortalException(
				"Launch set " + launchSetId + " is pending approval");
		}

		if (launchSetObjectEntry.getStatus() !=
				WorkflowConstants.STATUS_SCHEDULED) {

			return;
		}

		long userId = PrincipalThreadLocal.getUserId();

		_updateDisplayDate(
			null,
			_objectEntryLocalService.updateStatus(
				userId, launchSetId, WorkflowConstants.STATUS_DRAFT,
				_getServiceContext(launchSetObjectEntry, userId)));
	}

	private ObjectEntry _getLaunchSetObjectEntry(long launchSetId)
		throws PortalException {

		ObjectEntry objectEntry = _objectEntryLocalService.getObjectEntry(
			launchSetId);

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.getObjectDefinition(
				objectEntry.getObjectDefinitionId());

		if (!Objects.equals(
				objectDefinition.getExternalReferenceCode(), "L_LAUNCH_SET")) {

			throw new PortalException(
				"Object entry " + launchSetId + " is not a launch set");
		}

		return objectEntry;
	}

	private ServiceContext _getServiceContext(
		ObjectEntry launchSetObjectEntry, long userId) {

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		if (serviceContext == null) {
			serviceContext = new ServiceContext();
		}
		else {
			serviceContext = (ServiceContext)serviceContext.clone();
		}

		serviceContext.setCompanyId(launchSetObjectEntry.getCompanyId());
		serviceContext.setUserId(userId);

		return serviceContext;
	}

	private void _startWorkflowInstance(
			ObjectEntry launchSetObjectEntry, long userId)
		throws PortalException {

		ServiceContext serviceContext = _getServiceContext(
			launchSetObjectEntry, userId);

		serviceContext.setAttribute(
			"workflowContext",
			HashMapBuilder.<String, Serializable>put(
				WorkflowConstants.CONTEXT_WAIT_FOR_COMPLETION,
				Boolean.TRUE.toString()
			).build());
		serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);

		WorkflowHandlerRegistryUtil.startWorkflowInstance(
			launchSetObjectEntry.getCompanyId(),
			launchSetObjectEntry.getNonzeroGroupId(), userId,
			launchSetObjectEntry.getModelClassName(),
			launchSetObjectEntry.getObjectEntryId(), launchSetObjectEntry,
			serviceContext);
	}

	private ObjectEntry _updateDisplayDate(
		Date displayDate, ObjectEntry launchSetObjectEntry) {

		launchSetObjectEntry.setDisplayDate(displayDate);

		return _objectEntryLocalService.updateObjectEntry(launchSetObjectEntry);
	}

	@Reference
	private LaunchEntryManagerRegistry _launchEntryManagerRegistry;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

}