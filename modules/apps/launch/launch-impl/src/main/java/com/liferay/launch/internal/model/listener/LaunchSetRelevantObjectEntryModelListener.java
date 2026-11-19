/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.internal.model.listener;

import com.liferay.launch.set.publisher.LaunchSetPublisher;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.listener.RelevantObjectEntryModelListener;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactory;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.TransactionConfig;
import com.liferay.portal.kernel.transaction.TransactionInvokerUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alejandro Tardín
 */
@Component(service = RelevantObjectEntryModelListener.class)
public class LaunchSetRelevantObjectEntryModelListener
	extends BaseModelListener<ObjectEntry>
	implements RelevantObjectEntryModelListener {

	@Override
	public String getObjectDefinitionExternalReferenceCode() {
		return "L_LAUNCH_SET";
	}

	@Override
	public void onBeforeUpdate(
			ObjectEntry originalObjectEntry, ObjectEntry objectEntry)
		throws ModelListenerException {

		if ((originalObjectEntry.getStatus() ==
				WorkflowConstants.STATUS_APPROVED) ||
			(objectEntry.getStatus() != WorkflowConstants.STATUS_APPROVED)) {

			return;
		}

		String name = PrincipalThreadLocal.getName();
		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		try {
			if (serviceContext == null) {
				serviceContext = new ServiceContext();

				Group group = _groupLocalService.getGroup(
					objectEntry.getCompanyId(), GroupConstants.GUEST);

				serviceContext.setScopeGroupId(group.getGroupId());

				long userId = objectEntry.getStatusByUserId();

				serviceContext.setUserId(userId);

				PermissionThreadLocal.setPermissionChecker(
					_permissionCheckerFactory.create(
						_userLocalService.getUser(userId)));
				PrincipalThreadLocal.setName(userId);
			}
			else {
				serviceContext = (ServiceContext)serviceContext.clone();
			}

			serviceContext.setCompanyId(objectEntry.getCompanyId());
			serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);

			ServiceContextThreadLocal.pushServiceContext(serviceContext);

			try {
				TransactionInvokerUtil.invoke(
					_transactionConfig,
					() -> {
						_launchSetPublisher.publish(
							objectEntry.getObjectEntryId());

						return null;
					});
			}
			finally {
				ServiceContextThreadLocal.popServiceContext();
			}
		}
		catch (Throwable throwable) {
			throw new ModelListenerException(throwable.getMessage(), throwable);
		}
		finally {
			PermissionThreadLocal.setPermissionChecker(permissionChecker);
			PrincipalThreadLocal.setName(name);
		}
	}

	private static final TransactionConfig _transactionConfig;

	static {
		TransactionConfig.Builder builder = new TransactionConfig.Builder();

		builder.setPropagation(Propagation.REQUIRES_NEW);
		builder.setRollbackForClasses(Exception.class);

		_transactionConfig = builder.build();
	}

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private LaunchSetPublisher _launchSetPublisher;

	@Reference
	private PermissionCheckerFactory _permissionCheckerFactory;

	@Reference
	private UserLocalService _userLocalService;

}