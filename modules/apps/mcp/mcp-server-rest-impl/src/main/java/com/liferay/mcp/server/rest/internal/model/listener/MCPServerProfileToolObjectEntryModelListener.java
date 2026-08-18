/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.model.listener;

import com.liferay.mcp.server.rest.internal.constants.MCPServerConstants;
import com.liferay.mcp.server.rest.internal.servlet.MCPServerServlet;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.model.listener.RelevantObjectEntryModelListener;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.MapUtil;

import jakarta.servlet.Servlet;

import java.io.Serializable;

import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alberto Javier Moreno Lage
 */
@Component(service = RelevantObjectEntryModelListener.class)
public class MCPServerProfileToolObjectEntryModelListener
	extends BaseModelListener<ObjectEntry>
	implements RelevantObjectEntryModelListener {

	@Override
	public String getObjectDefinitionExternalReferenceCode() {
		return MCPServerConstants.
			EXTERNAL_REFERENCE_CODE_MCP_SERVER_PROFILE_TOOL;
	}

	@Override
	public void onAfterCreate(ObjectEntry objectEntry)
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

		_invalidateServlet(objectEntry);

		_deleteMCPServerRestrictedFieldObjectEntries(objectEntry);
	}

	private void _deleteMCPServerRestrictedFieldObjectEntries(
		ObjectEntry mcpServerProfileToolObjectEntry) {

		for (ObjectEntry mcpServerRestrictedFieldObjectEntry :
				_getMCPServerRestrictedFieldObjectEntries(
					mcpServerProfileToolObjectEntry)) {

			try {
				Map<String, Serializable> newValues =
					HashMapBuilder.<String, Serializable>putAll(
						mcpServerRestrictedFieldObjectEntry.getValues()
					).put(
						"deleteReason", "MCP server profile tool was deleted."
					).build();

				_objectEntryLocalService.updateObjectEntry(
					mcpServerRestrictedFieldObjectEntry.getUserId(),
					mcpServerRestrictedFieldObjectEntry.getObjectEntryId(),
					mcpServerRestrictedFieldObjectEntry.
						getObjectEntryFolderId(),
					newValues, new ServiceContext());

				mcpServerRestrictedFieldObjectEntry.setValues(newValues);

				_objectEntryLocalService.deleteObjectEntry(
					mcpServerRestrictedFieldObjectEntry);
			}
			catch (PortalException portalException) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						StringBundler.concat(
							"Unable to delete object entry ",
							mcpServerRestrictedFieldObjectEntry.
								getExternalReferenceCode(),
							" for profile tool ",
							mcpServerProfileToolObjectEntry.
								getExternalReferenceCode()),
						portalException);
				}
			}
		}
	}

	private List<ObjectEntry> _getMCPServerRestrictedFieldObjectEntries(
		ObjectEntry mcpServerProfileToolObjectEntry) {

		try {
			ObjectRelationship objectRelationship =
				_objectRelationshipLocalService.getObjectRelationship(
					mcpServerProfileToolObjectEntry.getObjectDefinitionId(),
					"mcpServerToolToRestrictedFields");

			return _objectEntryLocalService.getOneToManyObjectEntries(
				0, objectRelationship.getObjectRelationshipId(), null, false,
				mcpServerProfileToolObjectEntry.getObjectEntryId(), true, null,
				QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
		}
		catch (PortalException portalException) {
			throw new RuntimeException(portalException);
		}
	}

	private void _invalidateServlet(
		ObjectEntry mcpServerProfileToolObjectEntry) {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					MCPServerConstants.
						EXTERNAL_REFERENCE_CODE_MCP_SERVER_PROFILE,
					mcpServerProfileToolObjectEntry.getCompanyId());

		if (objectDefinition == null) {
			return;
		}

		ObjectEntry mcpServerProfileObjectEntry =
			_objectEntryLocalService.fetchObjectEntry(
				MapUtil.getString(
					mcpServerProfileToolObjectEntry.getValues(),
					"r_mcpServerProfileToTools_l_mcpServerProfileERC"),
				0, objectDefinition.getObjectDefinitionId());

		if (mcpServerProfileObjectEntry == null) {
			return;
		}

		MCPServerServlet mcpServerServlet = (MCPServerServlet)_servlet;

		mcpServerServlet.invalidate(
			mcpServerProfileToolObjectEntry.getCompanyId(),
			MapUtil.getString(mcpServerProfileObjectEntry.getValues(), "name"));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		MCPServerProfileToolObjectEntryModelListener.class);

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

	@Reference(
		target = "(osgi.http.whiteboard.servlet.name=com.liferay.mcp.server.rest.internal.servlet.MCPServerServlet)"
	)
	private Servlet _servlet;

}