/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.model.listener;

import com.liferay.mcp.server.rest.internal.search.index.util.MCPToolIndexWriterUtil;
import com.liferay.mcp.server.rest.internal.util.ObjectRESTPathUtil;
import com.liferay.mcp.server.rest.internal.util.OpenAPIBriefUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;

import org.osgi.service.component.annotations.Component;

/**
 * @author Alejandro Tardín
 */
@Component(service = ModelListener.class)
public class ObjectDefinitionModelListener
	extends BaseModelListener<ObjectDefinition> {

	@Override
	public void onAfterRemove(ObjectDefinition objectDefinition) {
		if (!objectDefinition.isApproved()) {
			return;
		}

		_invalidate(objectDefinition);
	}

	@Override
	public void onAfterUpdate(
		ObjectDefinition originalObjectDefinition,
		ObjectDefinition objectDefinition) {

		if (!originalObjectDefinition.isApproved() &&
			!objectDefinition.isApproved()) {

			return;
		}

		_invalidate(objectDefinition);
	}

	private void _invalidate(ObjectDefinition objectDefinition) {
		String restContextPath = ObjectRESTPathUtil.getRESTContextPath(
			objectDefinition);

		OpenAPIBriefUtil.clearOpenAPIJSONObjectCache(
			objectDefinition.getCompanyId(), restContextPath);

		MCPToolIndexWriterUtil.invalidate(
			objectDefinition.getCompanyId(),
			OpenAPIBriefUtil.getToolSetName(restContextPath));
	}

}