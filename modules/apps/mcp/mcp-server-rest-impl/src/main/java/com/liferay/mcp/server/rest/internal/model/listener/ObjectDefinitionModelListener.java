/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.model.listener;

import com.liferay.mcp.server.rest.internal.cache.MCPServerCacheManager;
import com.liferay.mcp.server.rest.internal.search.index.MCPToolIndexInvalidator;
import com.liferay.mcp.server.rest.internal.util.ObjectRESTPathUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alejandro Tardín
 */
@Component(service = ModelListener.class)
public class ObjectDefinitionModelListener
	extends BaseModelListener<ObjectDefinition> {

	@Override
	public void onAfterCreate(ObjectDefinition objectDefinition) {
		_invalidateToolSet(objectDefinition);
	}

	@Override
	public void onAfterRemove(ObjectDefinition objectDefinition) {
		_invalidateToolSet(objectDefinition);
	}

	@Override
	public void onAfterUpdate(
		ObjectDefinition originalObjectDefinition,
		ObjectDefinition objectDefinition) {

		_invalidateToolSet(objectDefinition);
	}

	private void _invalidateToolSet(ObjectDefinition objectDefinition) {
		if (!objectDefinition.isApproved()) {
			return;
		}

		_mcpServerCacheManager.clearOpenAPIJSONObjectCache(
			objectDefinition.getCompanyId());

		_mcpToolIndexInvalidator.invalidate(
			objectDefinition.getCompanyId(),
			ObjectRESTPathUtil.getRESTContextPath(objectDefinition));
	}

	@Reference
	private MCPServerCacheManager _mcpServerCacheManager;

	@Reference
	private MCPToolIndexInvalidator _mcpToolIndexInvalidator;

}