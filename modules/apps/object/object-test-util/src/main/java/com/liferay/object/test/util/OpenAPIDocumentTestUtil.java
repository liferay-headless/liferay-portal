/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.test.util;

import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.vulcan.application.HeadlessApplicationProvider;

import java.util.List;

/**
 * @author Petteri Karttunen
 */
public class OpenAPIDocumentTestUtil {

	public static String getContent(
			String basePath,
			HeadlessApplicationProvider headlessApplicationProvider)
		throws Exception {

		for (HeadlessApplicationProvider.Application application :
				headlessApplicationProvider.getApplications(
					TestPropsValues.getCompanyId())) {

			if (!basePath.equals(application.getBasePath())) {
				continue;
			}

			List<HeadlessApplicationProvider.OpenAPIDocument> openAPIDocuments =
				application.getOpenAPIDocuments();

			HeadlessApplicationProvider.OpenAPIDocument openAPIDocument =
				openAPIDocuments.get(0);

			return openAPIDocument.getContentString(
				"http://localhost/o",
				HeadlessApplicationProvider.OpenAPIDocument.Type.JSON);
		}

		return null;
	}

}