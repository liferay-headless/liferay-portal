/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.feature.flag;

import com.liferay.portal.kernel.feature.flag.FeatureFlagListener;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.vulcan.internal.cache.OpenAPIDocumentCache;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(property = "feature.flag.key=*", service = FeatureFlagListener.class)
public class OpenAPIDocumentCacheFeatureFlagListener
	implements FeatureFlagListener {

	@Override
	public void onValue(
		long companyId, String featureFlagKey, boolean enabled) {

		if (companyId == CompanyConstants.SYSTEM) {
			_openAPIDocumentCache.clear();

			return;
		}

		_openAPIDocumentCache.clear(companyId);
	}

	@Reference
	private OpenAPIDocumentCache _openAPIDocumentCache;

}