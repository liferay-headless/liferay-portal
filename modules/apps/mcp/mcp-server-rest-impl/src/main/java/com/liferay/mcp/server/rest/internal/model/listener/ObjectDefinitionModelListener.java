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
		try {
			String toolSetName = ToolSetUtil.getToolSetName(
				objectDefinition.getRESTContextPath());

			if (toolSetName != null) {
				ToolSetUtil.clearNumberOfTools(
					objectDefinition.getCompanyId(), toolSetName);
			}

			ToolSetUtil.clearOpenAPIJSONObjectCache(
				objectDefinition.getCompanyId());
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(exception);
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ObjectDefinitionModelListener.class);

}