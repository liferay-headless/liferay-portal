/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.headless.delivery.internal.dto.v1_0.util;

import com.liferay.headless.delivery.dto.v1_0.SeoSettings;
import com.liferay.layout.seo.model.LayoutSEOEntry;
import com.liferay.layout.seo.service.LayoutSEOEntryLocalService;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import com.liferay.portal.vulcan.util.LocalizedMapUtil;

/**
 * @author Javier de Arcos
 * @author Jürgen Kappler
 */
public class SeoSettingsUtil {

	public static SeoSettings getSeoSettings(
		DTOConverterContext dtoConverterContext,
		LayoutSEOEntryLocalService layoutSEOEntryLocalService, Layout layout) {

		LayoutSEOEntry layoutSeoEntry =
			layoutSEOEntryLocalService.fetchLayoutSEOEntry(
				layout.getGroupId(), layout.isPrivateLayout(),
				layout.getLayoutId());

		SeoSettings seoSettings = new SeoSettings() {
			{
				robots = layout.getRobots(dtoConverterContext.getLocale());
				robots_i18n = LocalizedMapUtil.getI18nMap(
					dtoConverterContext.isAcceptAllLanguages(),
					layout.getRobotsMap());
				seoKeywords = layout.getKeywords(
					dtoConverterContext.getLocale());
				seoKeywords_i18n = LocalizedMapUtil.getI18nMap(
					dtoConverterContext.isAcceptAllLanguages(),
					layout.getKeywordsMap());
			}
		};

		if ((layoutSeoEntry != null) &&
			layoutSeoEntry.isCanonicalURLEnabled()) {

			seoSettings.setCustomCanonicalURL(
				layoutSeoEntry.getCanonicalURL(
					dtoConverterContext.getLocale()));

			// TODO: CanonicalURL is localized?
			// seoSettings.setCustomCanonicalURL_i18n(LocalizedMapUtil.getI18nMap(dtoConverterContext.isAcceptAllLanguages(), layoutSeoEntry.getCanonicalURLMap()));

		}

		return seoSettings;
	}

}