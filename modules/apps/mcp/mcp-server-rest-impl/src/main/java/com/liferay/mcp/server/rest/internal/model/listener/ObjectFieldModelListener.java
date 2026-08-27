/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.model.listener;

import com.liferay.mcp.server.rest.internal.search.index.util.MCPToolIndexWriterUtil;
import com.liferay.mcp.server.rest.internal.util.ObjectRESTPathUtil;
import com.liferay.mcp.server.rest.internal.util.OpenAPIBriefUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alejandro Tardín
 */
@Component(service = ModelListener.class)
public class ObjectFieldModelListener extends BaseModelListener<ObjectField> {

	@Override
	public void onAfterCreate(ObjectField objectField) {
		_invalidate(objectField);
	}

	@Override
	public void onAfterRemove(ObjectField objectField) {
		_invalidate(objectField);
	}

	@Override
	public void onAfterUpdate(
		ObjectField originalObjectField, ObjectField objectField) {

		_invalidate(objectField);
	}

	private void _invalidate(ObjectField objectField) {
		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.fetchObjectDefinition(
				objectField.getObjectDefinitionId());

		if (objectDefinition == null) {
			MCPToolIndexWriterUtil.invalidate(objectField.getCompanyId());

			return;
		}

		if (!objectDefinition.isApproved()) {
			return;
		}

		String restContextPath = ObjectRESTPathUtil.getRESTContextPath(
			objectDefinition);

		OpenAPIBriefUtil.clearOpenAPIJSONObjectCache(
			objectField.getCompanyId(), restContextPath);

		MCPToolIndexWriterUtil.invalidate(
			objectField.getCompanyId(),
			OpenAPIBriefUtil.getToolSetName(restContextPath));
	}

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

}