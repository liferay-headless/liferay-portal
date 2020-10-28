/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 * <p>
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.headless.admin.content.internal.resource.v1_0;

import com.liferay.headless.admin.content.resource.v1_0.StructuredContentResource;
import com.liferay.headless.delivery.dto.v1_0.StructuredContent;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleService;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.util.ContentLanguageUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import javax.validation.constraints.NotNull;
import java.util.Collections;

/**
 * @author Javier Gamarra
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/structured-content.properties",
	scope = ServiceScope.PROTOTYPE, service = StructuredContentResource.class
)
public class StructuredContentResourceImpl
	extends BaseStructuredContentResourceImpl {

	@Override
	public StructuredContent getStructuredContent(
		@NotNull Long structuredContentId) throws Exception {

		JournalArticle journalArticle = _journalArticleService.getLatestArticle(
			structuredContentId);

		JournalArticle latestArticle =
			_journalArticleService.getLatestArticle(
				journalArticle.getGroupId(),
				journalArticle.getArticleId(),
				WorkflowConstants.STATUS_ANY);

		ContentLanguageUtil.addContentLanguageHeader(
			latestArticle.getAvailableLanguageIds(),
			latestArticle.getDefaultLanguageId(), contextHttpServletResponse,
			contextAcceptLanguage.getPreferredLocale());

		DTOConverter<JournalArticle, StructuredContent> dtoConverter =
			(DTOConverter<JournalArticle, StructuredContent>) _dtoConverterRegistry.getDTOConverter(
				JournalArticle.class.getName());

		if (dtoConverter == null) {
			return null;
		}

		return dtoConverter.toDTO(
			new DefaultDTOConverterContext(
				contextAcceptLanguage.isAcceptAllLanguages(),
				Collections.emptyMap(), _dtoConverterRegistry,
				contextHttpServletRequest, latestArticle.getResourcePrimKey(),
				contextAcceptLanguage.getPreferredLocale(),
				contextUriInfo,
				contextUser), latestArticle);
	}

	@Reference
	private DTOConverterRegistry _dtoConverterRegistry;
	@Reference
	private JournalArticleService _journalArticleService;
}