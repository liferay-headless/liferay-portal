/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.model.listener;

import com.liferay.mcp.server.rest.internal.constants.MCPServerConstants;
import com.liferay.mcp.server.rest.internal.servlet.MCPServerServlet;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.listener.RelevantObjectEntryModelListener;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.util.PortalInstances;

import jakarta.servlet.Servlet;

import jakarta.validation.ValidationException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alberto Javier Moreno Lage
 */
@Component(service = RelevantObjectEntryModelListener.class)
public class MCPServerRestrictedFieldObjectEntryModelListener
	extends BaseModelListener<ObjectEntry>
	implements RelevantObjectEntryModelListener {

	@Override
	public String getObjectDefinitionExternalReferenceCode() {
		return MCPServerConstants.
			EXTERNAL_REFERENCE_CODE_MCP_SERVER_RESTRICTED_FIELD;
	}

	@Override
	public void onAfterCreate(ObjectEntry objectEntry)
		throws ModelListenerException {

		_invalidateServlet(objectEntry);
	}

	@Override
	public void onAfterRemove(ObjectEntry objectEntry)
		throws ModelListenerException {

		_invalidateServlet(objectEntry);
	}

	@Override
	public void onAfterUpdate(
			ObjectEntry originalObjectEntry, ObjectEntry objectEntry)
		throws ModelListenerException {

		_invalidateServlet(objectEntry);
	}

	@Override
	public void onBeforeRemove(ObjectEntry objectEntry)
		throws ModelListenerException {

		if (PortalInstances.isCurrentCompanyInDeletionProcess()) {
			return;
		}

		if (Validator.isNull(
				MapUtil.getString(objectEntry.getValues(), "deleteReason"))) {

			throw new ModelListenerException(
				new ValidationException(
					"Unable to remove a restricted field without a delete " +
						"reason"));
		}
	}

	private ObjectEntry _fetchObjectEntry(
		long companyId, String externalReferenceCode,
		String objectDefinitionExternalReferenceCode) {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					objectDefinitionExternalReferenceCode, companyId);

		if (objectDefinition == null) {
			return null;
		}

		return _objectEntryLocalService.fetchObjectEntry(
			externalReferenceCode, 0, objectDefinition.getObjectDefinitionId());
	}

	private void _invalidateServlet(
		ObjectEntry mcpServerRestrictedFieldObjectEntry) {

		long companyId = mcpServerRestrictedFieldObjectEntry.getCompanyId();

		ObjectEntry mcpServerProfileToolObjectEntry = _fetchObjectEntry(
			companyId,
			MapUtil.getString(
				mcpServerRestrictedFieldObjectEntry.getValues(),
				"r_mcpServerToolToRestrictedFields_l_mcpServerProfileToolERC"),
			MCPServerConstants.EXTERNAL_REFERENCE_CODE_MCP_SERVER_PROFILE_TOOL);

		if (mcpServerProfileToolObjectEntry == null) {
			return;
		}

		ObjectEntry mcpServerProfileObjectEntry = _fetchObjectEntry(
			companyId,
			MapUtil.getString(
				mcpServerProfileToolObjectEntry.getValues(),
				"r_mcpServerProfileToTools_l_mcpServerProfileERC"),
			MCPServerConstants.EXTERNAL_REFERENCE_CODE_MCP_SERVER_PROFILE);

		if (mcpServerProfileObjectEntry == null) {
			return;
		}

		MCPServerServlet mcpServerServlet = (MCPServerServlet)_servlet;

		mcpServerServlet.invalidate(
			companyId,
			MapUtil.getString(mcpServerProfileObjectEntry.getValues(), "name"));
	}

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference(
		target = "(osgi.http.whiteboard.servlet.name=com.liferay.mcp.server.rest.internal.servlet.MCPServerServlet)"
	)
	private Servlet _servlet;

}