/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.web.internal.portlet.action;

import com.liferay.launch.entry.type.LaunchEntryType;
import com.liferay.launch.entry.type.registry.LaunchEntryTypeRegistry;
import com.liferay.launch.web.internal.constants.LaunchPortletKeys;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.portlet.ResourceRequest;
import jakarta.portlet.ResourceResponse;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author David Truong
 */
@Component(
	property = {
		"jakarta.portlet.name=" + LaunchPortletKeys.LAUNCH,
		"mvc.command.name=/launch/get_launch_entry_summary"
	},
	service = MVCResourceCommand.class
)
public class GetLaunchEntrySummaryMVCResourceCommand
	extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

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
		String classVersion = ParamUtil.getString(
			resourceRequest, "classVersion");

		LaunchEntryType.Version version = launchEntryType.fetchVersion(
			classPK, classVersion);

		if (version == null) {
			resourceResponse.setProperty(
				ResourceResponse.HTTP_STATUS_CODE,
				String.valueOf(HttpServletResponse.SC_NOT_FOUND));

			return;
		}

		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		Locale locale = themeDisplay.getLocale();

		boolean published = ParamUtil.getBoolean(resourceRequest, "published");

		try {
			Group group = _groupLocalService.getGroup(version.getGroupId());

			String editURL = null;

			if (!published) {
				editURL = version.getEditURL(
					ParamUtil.getString(resourceRequest, "redirect"),
					RequestBackedPortletURLFactoryUtil.create(resourceRequest));
			}

			JSONPortletResponseUtil.writeJSON(
				resourceRequest, resourceResponse,
				JSONUtil.put(
					"author", version.getUserName()
				).put(
					"editURL", editURL
				).put(
					"groupId", version.getGroupId()
				).put(
					"modified", version.getModifiedDate()
				).put(
					"space", group.getDescriptiveName(locale)
				).put(
					"status", version.getStatus()
				).put(
					"title", version.getTitle(locale)
				).put(
					"type", version.getTypeName(locale)
				).put(
					"version", version.getLabel(locale)
				));
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					StringBundler.concat(
						"Unable to resolve launch entry summary for class ",
						className, " ", classPK),
					portalException);
			}

			resourceResponse.setProperty(
				ResourceResponse.HTTP_STATUS_CODE,
				String.valueOf(HttpServletResponse.SC_NOT_FOUND));
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		GetLaunchEntrySummaryMVCResourceCommand.class);

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private LaunchEntryTypeRegistry _launchEntryTypeRegistry;

}