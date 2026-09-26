/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.internal.model.listener;

import com.liferay.launch.internal.entry.type.util.LaunchPreviewLayoutUtil;
import com.liferay.layout.content.model.LayoutContentVersion;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.service.LayoutLocalService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Drops the layout a version was materialized into for previewing once the
 * version itself is gone, so that the preview cache never outlives what it
 * holds a copy of.
 *
 * @author Alejandro Tardín
 */
@Component(
	property = "model.class.name=com.liferay.layout.content.model.LayoutContentVersion",
	service = ModelListener.class
)
public class LayoutContentVersionModelListener
	extends BaseModelListener<LayoutContentVersion> {

	@Override
	public void onBeforeRemove(LayoutContentVersion layoutContentVersion)
		throws ModelListenerException {

		Layout draftLayout = _layoutLocalService.fetchLayout(
			layoutContentVersion.getPlid());

		if (draftLayout == null) {
			return;
		}

		Layout layout = _layoutLocalService.fetchLayout(
			draftLayout.getClassPK());

		if (layout == null) {
			return;
		}

		Layout previewLayout =
			_layoutLocalService.fetchLayoutByExternalReferenceCode(
				LaunchPreviewLayoutUtil.getExternalReferenceCode(
					layout, layoutContentVersion),
				layout.getGroupId());

		if (previewLayout == null) {
			return;
		}

		try {
			_layoutLocalService.deleteLayout(previewLayout);
		}
		catch (PortalException portalException) {
			throw new ModelListenerException(portalException);
		}
	}

	@Reference
	private LayoutLocalService _layoutLocalService;

}