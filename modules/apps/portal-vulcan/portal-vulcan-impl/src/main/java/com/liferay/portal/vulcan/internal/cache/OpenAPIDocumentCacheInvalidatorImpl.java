/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.cache;

import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.cluster.Clusterable;
import com.liferay.portal.kernel.module.framework.service.IdentifiableOSGiService;
import com.liferay.portal.vulcan.application.OpenAPIDocumentCacheInvalidator;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = AopService.class)
public class OpenAPIDocumentCacheInvalidatorImpl
	implements AopService, IdentifiableOSGiService,
			   OpenAPIDocumentCacheInvalidator {

	@Override
	public String getOSGiServiceIdentifier() {
		return OpenAPIDocumentCacheInvalidator.class.getName();
	}

	@Clusterable
	@Override
	public void invalidate(long companyId) {
		_openAPIDocumentCache.clear(companyId);
	}

	@Clusterable
	@Override
	public void invalidate(long companyId, String restContextPath) {
		_openAPIDocumentCache.clear(companyId, restContextPath);
	}

	@Reference
	private OpenAPIDocumentCache _openAPIDocumentCache;

}