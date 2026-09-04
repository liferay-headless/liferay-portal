/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.model.listener;

import com.liferay.mcp.server.rest.internal.util.ToolSetUtil;
import com.liferay.object.model.ObjectAction;
import com.liferay.object.model.ObjectDefinition;
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
public class ObjectActionModelListener extends BaseModelListener<ObjectAction> {

	@Override
	public void onAfterCreate(ObjectAction objectAction) {
		_clearCaches(objectAction);
	}

	@Override
	public void onAfterRemove(ObjectAction objectAction) {
		_clearCaches(objectAction);
	}

	@Override
	public void onAfterUpdate(
		ObjectAction originalObjectAction, ObjectAction objectAction) {

		_clearCaches(objectAction);
	}

	private void _clearCaches(ObjectAction objectAction) {

		// Never let a cache failure break the object operation that triggered
		// it, since these run inside the caller's transaction

		try {
			_doClearCaches(objectAction);
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(exception);
			}
		}
	}

	private void _doClearCaches(ObjectAction objectAction) {

		// A standalone action adds paths to its object definition's document,
		// so both the document and the count have to go. Clearing the count
		// alone would leave the summaries endpoint stale instead.

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.fetchObjectDefinition(
				objectAction.getObjectDefinitionId());

		if (objectDefinition == null) {
			return;
		}

		String toolSetName = ToolSetUtil.getToolSetName(
			objectDefinition.getRESTContextPath());

		if (toolSetName != null) {
			ToolSetUtil.clearNumberOfTools(
				objectAction.getCompanyId(), toolSetName);
		}

		ToolSetUtil.clearOpenAPIJSONObjectCache(objectAction.getCompanyId());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ObjectActionModelListener.class);

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

}