/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.model.listener;

import com.liferay.mcp.server.rest.internal.util.ToolSetUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jose Luis Navarro
 */
@Component(service = ModelListener.class)
public class ObjectRelationshipModelListener
	extends BaseModelListener<ObjectRelationship> {

	@Override
	public void onAfterCreate(ObjectRelationship objectRelationship) {
		_clearCaches(objectRelationship);
	}

	@Override
	public void onAfterRemove(ObjectRelationship objectRelationship) {
		_clearCaches(objectRelationship);
	}

	@Override
	public void onAfterUpdate(
		ObjectRelationship originalObjectRelationship,
		ObjectRelationship objectRelationship) {

		_clearCaches(objectRelationship);
	}

	private void _clearCaches(ObjectRelationship objectRelationship) {

		// Never let a cache failure break the object operation that triggered
		// it, since these run inside the caller's transaction

		try {
			_doClearCaches(objectRelationship);
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(exception);
			}
		}
	}

	private void _clearNumberOfTools(long companyId, long objectDefinitionId) {

		// Each side is resolved on its own so that a definition already gone
		// on remove does not stop the surviving side from being invalidated

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.fetchObjectDefinition(
				objectDefinitionId);

		if (objectDefinition == null) {
			return;
		}

		String toolSetName = ToolSetUtil.getToolSetName(
			objectDefinition.getRESTContextPath());

		if (toolSetName != null) {
			ToolSetUtil.clearNumberOfTools(companyId, toolSetName);
		}
	}

	private void _doClearCaches(ObjectRelationship objectRelationship) {

		// A relationship adds paths to one side and can affect the other, so
		// drop the counts of both object definitions and nothing else

		_clearNumberOfTools(
			objectRelationship.getCompanyId(),
			objectRelationship.getObjectDefinitionId1());
		_clearNumberOfTools(
			objectRelationship.getCompanyId(),
			objectRelationship.getObjectDefinitionId2());

		ToolSetUtil.clearOpenAPIJSONObjectCache(
			objectRelationship.getCompanyId());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ObjectRelationshipModelListener.class);

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

}