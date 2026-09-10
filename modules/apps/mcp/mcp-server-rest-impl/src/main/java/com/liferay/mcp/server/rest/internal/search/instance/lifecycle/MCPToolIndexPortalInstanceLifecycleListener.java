/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.instance.lifecycle;

import com.liferay.mcp.server.rest.internal.search.index.MCPToolIndexCreator;
import com.liferay.mcp.server.rest.internal.search.index.MCPToolIndexWriter;
import com.liferay.portal.instance.lifecycle.BasePortalInstanceLifecycleListener;
import com.liferay.portal.instance.lifecycle.PortalInstanceLifecycleListener;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.search.capabilities.SearchCapabilities;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = PortalInstanceLifecycleListener.class)
public class MCPToolIndexPortalInstanceLifecycleListener
	extends BasePortalInstanceLifecycleListener {

	@Override
	public void portalInstanceRegistered(Company company) throws Exception {
		if (!_searchCapabilities.isMCPToolSearchSupported()) {
			return;
		}

		_mcpToolIndexCreator.createIfNotExists(company.getCompanyId());
	}

	@Override
	public void portalInstanceUnregistered(Company company) throws Exception {
		if (!_searchCapabilities.isMCPToolSearchSupported()) {
			return;
		}

		_mcpToolIndexCreator.deleteIfExists(company.getCompanyId());

		// Remove rather than invalidate. Invalidating creates the state it
		// marks, so a torn down instance would leave one behind for a company
		// that no longer exists.

		_mcpToolIndexWriter.removeIndexState(company.getCompanyId());
	}

	@Reference
	private MCPToolIndexCreator _mcpToolIndexCreator;

	@Reference
	private MCPToolIndexWriter _mcpToolIndexWriter;

	@Reference
	private SearchCapabilities _searchCapabilities;

}