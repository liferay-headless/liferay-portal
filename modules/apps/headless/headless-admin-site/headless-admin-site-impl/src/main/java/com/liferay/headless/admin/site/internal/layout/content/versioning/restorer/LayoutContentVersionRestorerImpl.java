/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.layout.content.versioning.restorer;

import com.liferay.client.extension.type.manager.CETManager;
import com.liferay.fragment.processor.FragmentEntryProcessorRegistry;
import com.liferay.headless.admin.site.dto.v1_0.ContentPageSpecification;
import com.liferay.headless.admin.site.dto.v1_0.PageExperience;
import com.liferay.headless.admin.site.internal.resource.v1_0.util.LayoutUtil;
import com.liferay.info.item.InfoItemServiceRegistry;
import com.liferay.layout.content.model.LayoutContentVersion;
import com.liferay.layout.content.restorer.LayoutContentVersionRestorer;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.segments.model.SegmentsExperience;
import com.liferay.segments.service.SegmentsExperienceLocalService;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alejandro Tardín
 */
@Component(service = LayoutContentVersionRestorer.class)
public class LayoutContentVersionRestorerImpl
	implements LayoutContentVersionRestorer {

	@Override
	public Layout restoreLayoutContentVersion(
			Layout draftLayout, LayoutContentVersion layoutContentVersion,
			ServiceContext serviceContext)
		throws Exception {

		ContentPageSpecification contentPageSpecification =
			ContentPageSpecification.unsafeToDTO(
				layoutContentVersion.getData());

		_setPageExperienceExternalReferenceCodes(
			contentPageSpecification, draftLayout);

		return LayoutUtil.updateLayout(
			_cetManager, _fragmentEntryProcessorRegistry,
			_infoItemServiceRegistry, draftLayout,
			layoutContentVersion.getNameMap(), draftLayout.getTitleMap(),
			draftLayout.getDescriptionMap(), draftLayout.getKeywordsMap(),
			draftLayout.getRobotsMap(), draftLayout.getFriendlyURLMap(),
			contentPageSpecification, draftLayout.getStatus(), serviceContext);
	}

	/**
	 * Points the version's experiences at the ones the page being restored
	 * into owns, matching them by key. A version can be restored into a page
	 * other than the one it was taken from, and an experience external
	 * reference code belongs to a single page, so carrying the version's own
	 * codes over would report the page's default experience as a foreign one.
	 */
	private void _setPageExperienceExternalReferenceCodes(
		ContentPageSpecification contentPageSpecification, Layout draftLayout) {

		PageExperience[] pageExperiences =
			contentPageSpecification.getPageExperiences();

		if (pageExperiences == null) {
			return;
		}

		Map<String, String> externalReferenceCodes = new HashMap<>();

		for (SegmentsExperience segmentsExperience :
				_segmentsExperienceLocalService.getSegmentsExperiences(
					draftLayout.getGroupId(), draftLayout.getPlid())) {

			externalReferenceCodes.put(
				segmentsExperience.getSegmentsExperienceKey(),
				segmentsExperience.getExternalReferenceCode());
		}

		for (PageExperience pageExperience : pageExperiences) {
			pageExperience.setExternalReferenceCode(
				() -> externalReferenceCodes.get(pageExperience.getKey()));
		}
	}

	@Reference
	private CETManager _cetManager;

	@Reference
	private FragmentEntryProcessorRegistry _fragmentEntryProcessorRegistry;

	@Reference
	private InfoItemServiceRegistry _infoItemServiceRegistry;

	@Reference
	private SegmentsExperienceLocalService _segmentsExperienceLocalService;

}