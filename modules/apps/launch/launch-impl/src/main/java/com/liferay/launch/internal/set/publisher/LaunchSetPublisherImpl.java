/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.internal.set.publisher;

import com.liferay.launch.entry.manager.LaunchEntryManager;
import com.liferay.launch.entry.manager.LaunchEntryManagerRegistry;
import com.liferay.launch.internal.constants.LaunchDestinationNames;
import com.liferay.launch.set.publisher.LaunchSetPublisher;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.scheduler.SchedulerEngineHelper;
import com.liferay.portal.kernel.scheduler.StorageType;
import com.liferay.portal.kernel.scheduler.TriggerFactory;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.TransactionConfig;
import com.liferay.portal.kernel.transaction.TransactionInvokerUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.io.Serializable;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alejandro Tardín
 * @author Jose Luis Navarro
 */
@Component(service = LaunchSetPublisher.class)
public class LaunchSetPublisherImpl implements LaunchSetPublisher {

	@Override
	public void failPublish(long launchSetId, long userId, String errorMessage)
		throws PortalException {

		ObjectEntry launchSetObjectEntry =
			_objectEntryLocalService.fetchObjectEntry(launchSetId);

		if ((launchSetObjectEntry == null) ||
			launchSetObjectEntry.isApproved()) {

			return;
		}

		_deleteLaunchSetErrorObjectEntries(launchSetObjectEntry);

		_addLaunchSetErrorObjectEntry(
			null, launchSetObjectEntry, errorMessage, "critical", userId);

		_updateStatusToDraft(launchSetObjectEntry, userId);
	}

	@Override
	public void publish(long launchSetId) throws PortalException {
		ObjectEntry launchSetObjectEntry =
			_objectEntryLocalService.getObjectEntry(launchSetId);

		if (launchSetObjectEntry.getStatus() ==
				WorkflowConstants.STATUS_APPROVED) {

			throw new PortalException(
				"Launch set " + launchSetId + " was already published");
		}

		_deleteLaunchSetErrorObjectEntries(launchSetObjectEntry);

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		int failedCount = 0;

		for (ObjectEntry launchEntryObjectEntry :
				_getOneToManyObjectEntries(
					launchSetObjectEntry, "launchSetToLaunchEntries")) {

			Map<String, Serializable> values =
				launchEntryObjectEntry.getValues();

			if ((MapUtil.getLong(values, "draftId") == 0) &&
				Validator.isNotNull(MapUtil.getString(values, "versionKey"))) {

				continue;
			}

			try {
				TransactionInvokerUtil.invoke(
					_transactionConfig,
					() -> {
						_publishLaunchEntryObjectEntry(
							launchEntryObjectEntry, serviceContext);

						return null;
					});
			}
			catch (Throwable throwable) {
				if (_log.isDebugEnabled()) {
					_log.debug(
						"Unable to publish launch entry " +
							launchEntryObjectEntry.getObjectEntryId(),
						throwable);
				}

				failedCount++;

				_addLaunchSetErrorObjectEntry(
					launchEntryObjectEntry, launchSetObjectEntry,
					GetterUtil.getString(
						throwable.getMessage(), String.valueOf(throwable)),
					"error", serviceContext.getUserId());
			}
		}

		if (failedCount > 0) {
			_updateStatusToDraft(
				launchSetObjectEntry, serviceContext.getUserId());

			throw new PortalException(
				StringBundler.concat(
					"Unable to publish ", failedCount,
					" launch entries of launch set ", launchSetId));
		}

		_objectEntryLocalService.updateStatus(
			serviceContext.getUserId(), launchSetId,
			WorkflowConstants.STATUS_APPROVED, serviceContext);
	}

	@Override
	public void schedulePublish(Date date, long launchSetId, long userId)
		throws PortalException {

		ObjectEntry launchSetObjectEntry =
			_objectEntryLocalService.getObjectEntry(launchSetId);

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

		ServiceContext serviceContext = _createServiceContext(
			launchSetObjectEntry, userId);

		_updateScheduledDate(
			date, launchSetObjectEntry, serviceContext, userId);

		_objectEntryLocalService.updateStatus(
			userId, launchSetId, WorkflowConstants.STATUS_SCHEDULED,
			serviceContext);

		Message message = new Message();

		message.put("companyId", launchSetObjectEntry.getCompanyId());
		message.put("launchSetId", launchSetId);
		message.put("userId", userId);

		_schedulerEngineHelper.schedule(
			_triggerFactory.createTrigger(
				_getJobName(launchSetObjectEntry),
				LaunchDestinationNames.LAUNCH_SET_SCHEDULED_PUBLISH, date, null,
				0, null),
			StorageType.PERSISTED, String.valueOf(launchSetId),
			LaunchDestinationNames.LAUNCH_SET_SCHEDULED_PUBLISH, message);
	}

	@Override
	public void unschedulePublish(long launchSetId) throws PortalException {
		ObjectEntry launchSetObjectEntry =
			_objectEntryLocalService.fetchObjectEntry(launchSetId);

		if (launchSetObjectEntry == null) {
			return;
		}

		_schedulerEngineHelper.delete(
			_getJobName(launchSetObjectEntry),
			LaunchDestinationNames.LAUNCH_SET_SCHEDULED_PUBLISH,
			StorageType.PERSISTED);

		if (launchSetObjectEntry.getStatus() !=
				WorkflowConstants.STATUS_SCHEDULED) {

			return;
		}

		long userId = PrincipalThreadLocal.getUserId();

		ServiceContext serviceContext = _createServiceContext(
			launchSetObjectEntry, userId);

		_objectEntryLocalService.updateStatus(
			userId, launchSetId, WorkflowConstants.STATUS_DRAFT,
			serviceContext);

		_updateScheduledDate(
			null, launchSetObjectEntry, serviceContext, userId);
	}

	private void _addLaunchSetErrorObjectEntry(
			ObjectEntry launchEntryObjectEntry,
			ObjectEntry launchSetObjectEntry, String message, String severity,
			long userId)
		throws PortalException {

		long launchEntryId = 0;
		String launchEntryTitle = StringPool.BLANK;

		if (launchEntryObjectEntry != null) {
			launchEntryId = launchEntryObjectEntry.getObjectEntryId();
			launchEntryTitle = _getLaunchEntryTitle(launchEntryObjectEntry);
		}

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.getObjectDefinition(
				launchSetObjectEntry.getCompanyId(), "LaunchSetError");

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setCompanyId(launchSetObjectEntry.getCompanyId());
		serviceContext.setUserId(userId);

		_objectEntryLocalService.addObjectEntry(
			0, userId, objectDefinition.getObjectDefinitionId(), 0, null,
			HashMapBuilder.<String, Serializable>put(
				"launchEntryId", launchEntryId
			).put(
				"launchEntryTitle", launchEntryTitle
			).put(
				"message", message
			).put(
				"r_launchSetToLaunchSetErrors_c_launchSetId",
				launchSetObjectEntry.getObjectEntryId()
			).put(
				"severity", severity
			).build(),
			serviceContext);
	}

	private ServiceContext _createServiceContext(
		ObjectEntry launchSetObjectEntry, long userId) {

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setCompanyId(launchSetObjectEntry.getCompanyId());
		serviceContext.setUserId(userId);
		serviceContext.setWorkflowAction(WorkflowConstants.ACTION_SAVE_DRAFT);

		return serviceContext;
	}

	private void _deleteLaunchSetErrorObjectEntries(
			ObjectEntry launchSetObjectEntry)
		throws PortalException {

		for (ObjectEntry launchSetErrorObjectEntry :
				_getOneToManyObjectEntries(
					launchSetObjectEntry, "launchSetToLaunchSetErrors")) {

			_objectEntryLocalService.deleteObjectEntry(
				launchSetErrorObjectEntry);
		}
	}

	private String _getJobName(ObjectEntry launchSetObjectEntry) {
		return StringBundler.concat(
			launchSetObjectEntry.getObjectEntryId(), StringPool.AT,
			launchSetObjectEntry.getCompanyId());
	}

	private String _getLaunchEntryTitle(ObjectEntry launchEntryObjectEntry) {
		Map<String, Serializable> values = launchEntryObjectEntry.getValues();

		LaunchEntryManager launchEntryManager =
			_launchEntryManagerRegistry.getLaunchEntryManager(
				MapUtil.getString(values, "className"));

		if (launchEntryManager == null) {
			return StringPool.BLANK;
		}

		LaunchEntryManager.Item item = launchEntryManager.fetchItem(
			MapUtil.getLong(values, "classPK"));

		if (item == null) {
			return StringPool.BLANK;
		}

		return item.getTitle(LocaleUtil.getSiteDefault());
	}

	private List<ObjectEntry> _getOneToManyObjectEntries(
			ObjectEntry launchSetObjectEntry, String objectRelationshipName)
		throws PortalException {

		ObjectRelationship objectRelationship =
			_objectRelationshipLocalService.getObjectRelationship(
				launchSetObjectEntry.getObjectDefinitionId(),
				objectRelationshipName);

		return _objectEntryLocalService.getOneToManyObjectEntries(
			launchSetObjectEntry.getGroupId(),
			objectRelationship.getObjectRelationshipId(), null, false,
			launchSetObjectEntry.getObjectEntryId(), true, null,
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	private void _publishLaunchEntryObjectEntry(
			ObjectEntry launchEntryObjectEntry, ServiceContext serviceContext)
		throws PortalException {

		Map<String, Serializable> values = launchEntryObjectEntry.getValues();

		String className = MapUtil.getString(values, "className");

		LaunchEntryManager launchEntryManager =
			_launchEntryManagerRegistry.getLaunchEntryManager(className);

		if (launchEntryManager == null) {
			throw new PortalException(
				"No launch entry manager exists for " + className);
		}

		LaunchEntryManager.Version version = launchEntryManager.publishDraft(
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

	private void _updateScheduledDate(
			Date date, ObjectEntry launchSetObjectEntry,
			ServiceContext serviceContext, long userId)
		throws PortalException {

		Map<String, Serializable> values = launchSetObjectEntry.getValues();

		values.put("scheduledDate", date);

		_objectEntryLocalService.updateObjectEntry(
			userId, launchSetObjectEntry.getObjectEntryId(),
			launchSetObjectEntry.getObjectEntryFolderId(), values,
			serviceContext);
	}

	private void _updateStatusToDraft(
			ObjectEntry launchSetObjectEntry, long userId)
		throws PortalException {

		ObjectEntry currentLaunchSetObjectEntry =
			_objectEntryLocalService.getObjectEntry(
				launchSetObjectEntry.getObjectEntryId());

		if (currentLaunchSetObjectEntry.isDraft()) {
			return;
		}

		_objectEntryLocalService.updateStatus(
			userId, launchSetObjectEntry.getObjectEntryId(),
			WorkflowConstants.STATUS_DRAFT,
			_createServiceContext(launchSetObjectEntry, userId));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		LaunchSetPublisherImpl.class);

	private static final TransactionConfig _transactionConfig;

	static {
		TransactionConfig.Builder builder = new TransactionConfig.Builder();

		builder.setPropagation(Propagation.REQUIRES_NEW);
		builder.setRollbackForClasses(Exception.class);

		_transactionConfig = builder.build();
	}

	@Reference
	private LaunchEntryManagerRegistry _launchEntryManagerRegistry;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

	@Reference
	private SchedulerEngineHelper _schedulerEngineHelper;

	@Reference
	private TriggerFactory _triggerFactory;

}