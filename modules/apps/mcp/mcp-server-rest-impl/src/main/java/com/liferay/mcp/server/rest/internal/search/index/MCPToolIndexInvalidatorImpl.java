/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index;

import com.liferay.mcp.server.rest.internal.util.OpenAPIBriefUtil;
import com.liferay.mcp.server.rest.internal.util.SearchToolUtil;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.cluster.Clusterable;
import com.liferay.portal.kernel.module.framework.service.IdentifiableOSGiService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = AopService.class)
public class MCPToolIndexInvalidatorImpl
	implements AopService, IdentifiableOSGiService, MCPToolIndexInvalidator {

	@Override
	public String getOSGiServiceIdentifier() {
		return MCPToolIndexInvalidatorImpl.class.getName();
	}

	@Clusterable
	@Override
	public void invalidate(long companyId, String restContextPath) {
		OpenAPIBriefUtil.clearOpenAPIJSONObjectCache(
			companyId, restContextPath);

		if (!SearchToolUtil.isToolSearchSupported()) {
			return;
		}

		_mcpToolIndexWriter.invalidate(
			companyId,
			OpenAPIBriefUtil.getToolSetName(companyId, restContextPath));
	}

	@Reference
	private MCPToolIndexWriter _mcpToolIndexWriter;

}