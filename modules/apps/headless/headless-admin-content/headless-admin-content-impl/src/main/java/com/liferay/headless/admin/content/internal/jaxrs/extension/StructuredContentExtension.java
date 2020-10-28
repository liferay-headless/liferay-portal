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

package com.liferay.headless.admin.content.internal.jaxrs.extension;

import com.liferay.headless.delivery.dto.v1_0.StructuredContent;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.portal.vulcan.jaxrs.context.ExtensionContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.ext.ContextResolver;

/**
 * @author Luis Miguel Barcos
 */

@Component(
	property = {
		"osgi.jaxrs.application.select=(osgi.jaxrs.name=Liferay.Headless.Admin.Content)",
		"osgi.jaxrs.extension=true",
		"osgi.jaxrs.name=Liferay.Headless.Admin.Content.VersionInformation"
	},
	service = ContextResolver.class
)
public class StructuredContentExtension
	implements ContextResolver<ExtensionContext> {

	@Override
	public ExtensionContext getContext(Class<?> type) {
		if (type.isAssignableFrom(StructuredContent.class)) {
			return new StructuredContentExtensionContext(
				_journalArticleLocalService);
		}
		return null;
	}

	@Reference
	private JournalArticleLocalService _journalArticleLocalService;
}
