/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.model.listener;

import com.liferay.mcp.server.rest.internal.util.ToolSetUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
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
	public void onAfterCreate(ObjectDefinition objectDefinition) {
		_clearCaches(objectDefinition);
	}

	@Override
	public void onAfterRemove(ObjectDefinition objectDefinition) {
		_clearCaches(objectDefinition);
	}

	@Override
	public void onAfterUpdate(
		ObjectDefinition originalObjectDefinition,
		ObjectDefinition objectDefinition) {

		_clearCaches(objectDefinition);
	}

	private void _clearCaches(ObjectDefinition objectDefinition) {

		// Never let a cache failure break the object operation that triggered
		// it, since these run inside the caller's transaction

		try {
			_doClearCaches(objectDefinition);
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(exception);
			}
		}
	}

	private void _doClearCaches(ObjectDefinition objectDefinition) {

		// Only this object definition's tool set changed, so drop its count
		// alone and let it refill from its own document. An unresolved name
		// means nothing is cached under it, so skipping beats wiping the map.

		String toolSetName = ToolSetUtil.getToolSetName(
			objectDefinition.getRESTContextPath());

		if (toolSetName != null) {
			ToolSetUtil.clearNumberOfTools(
				objectDefinition.getCompanyId(), toolSetName);
		}

		ToolSetUtil.clearOpenAPIJSONObjectCache(
			objectDefinition.getCompanyId());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ObjectDefinitionModelListener.class);

}