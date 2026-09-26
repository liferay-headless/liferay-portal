/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.web.internal.portlet.action;

import com.liferay.launch.entry.type.LaunchEntryType;
import com.liferay.launch.entry.type.registry.LaunchEntryTypeRegistry;
import com.liferay.launch.web.internal.constants.LaunchPortletKeys;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import jakarta.portlet.ResourceRequest;
import jakarta.portlet.ResourceResponse;

import jakarta.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alejandro Tardín
 */
@Component(
	property = {
		"jakarta.portlet.name=" + LaunchPortletKeys.LAUNCH,
		"mvc.command.name=/launch/add_launch_version"
	},
	service = MVCResourceCommand.class
)
public class AddLaunchVersionMVCResourceCommand extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		long launchSetId = ParamUtil.getLong(resourceRequest, "launchSetId");

		ObjectEntry launchSetObjectEntry =
			_objectEntryLocalService.getObjectEntry(launchSetId);

		if (launchSetObjectEntry.getStatus() ==
				WorkflowConstants.STATUS_APPROVED) {

			throw new PortalException(
				"Launch set " + launchSetId + " was already published");
		}

		String className = ParamUtil.getString(resourceRequest, "className");

		LaunchEntryType launchEntryType =
			_launchEntryTypeRegistry.getLaunchEntryType(className);

		if (launchEntryType == null) {
			resourceResponse.setProperty(
				ResourceResponse.HTTP_STATUS_CODE,
				String.valueOf(HttpServletResponse.SC_NOT_FOUND));

			return;
		}

		long classPK = ParamUtil.getLong(resourceRequest, "classPK");
		String baseClassVersion = ParamUtil.getString(
			resourceRequest, "baseClassVersion");

		LaunchEntryType.Version version = launchEntryType.addVersion(
			classPK, baseClassVersion);

		JSONPortletResponseUtil.writeJSON(
			resourceRequest, resourceResponse,
			JSONUtil.put(
				"baseClassVersion", baseClassVersion
			).put(
				"className", className
			).put(
				"classPK", classPK
			).put(
				"classVersion", version.getClassVersion()
			));
	}

	@Reference
	private LaunchEntryTypeRegistry _launchEntryTypeRegistry;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

}