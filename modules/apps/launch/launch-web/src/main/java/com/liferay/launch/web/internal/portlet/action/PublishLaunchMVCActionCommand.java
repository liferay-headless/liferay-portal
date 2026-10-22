/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.web.internal.portlet.action;

import com.liferay.launch.set.publisher.LaunchSetPublisher;
import com.liferay.launch.web.internal.constants.LaunchPortletKeys;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseTransactionalMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;

import jakarta.portlet.ActionRequest;
import jakarta.portlet.ActionResponse;
import jakarta.portlet.PortletException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alejandro Tardín
 * @author Jose Luis Navarro
 */
@Component(
	property = {
		"jakarta.portlet.name=" + LaunchPortletKeys.LAUNCH,
		"mvc.command.name=/launch/publish_launch"
	},
	service = MVCActionCommand.class
)
public class PublishLaunchMVCActionCommand
	extends BaseTransactionalMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws PortletException {

		try {
			super.doProcessAction(actionRequest, actionResponse);
		}
		catch (PortletException portletException) {
			Throwable throwable = portletException.getCause();

			if (throwable == null) {
				throwable = portletException;
			}

			try {
				_launchSetPublisher.failPublish(
					ParamUtil.getLong(actionRequest, "launchSetId"),
					PrincipalThreadLocal.getUserId(),
					GetterUtil.getString(
						throwable.getMessage(), String.valueOf(throwable)));
			}
			catch (Exception exception) {
				portletException.addSuppressed(exception);
			}

			throw portletException;
		}
	}

	@Override
	protected void doTransactionalCommand(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		_launchSetPublisher.publish(
			ParamUtil.getLong(actionRequest, "launchSetId"));

		sendRedirect(actionRequest, actionResponse);
	}

	@Reference
	private LaunchSetPublisher _launchSetPublisher;

}