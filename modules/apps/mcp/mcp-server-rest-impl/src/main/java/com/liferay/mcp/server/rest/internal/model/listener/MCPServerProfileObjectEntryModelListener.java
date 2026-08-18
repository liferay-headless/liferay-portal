/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.model.listener;

import com.liferay.mcp.server.rest.internal.constants.MCPServerConstants;
import com.liferay.mcp.server.rest.internal.servlet.MCPServerServlet;
import com.liferay.object.constants.ObjectEntryFolderConstants;
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
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alejandro Tardín
 */
@Component(service = RelevantObjectEntryModelListener.class)
public class MCPServerProfileObjectEntryModelListener
	extends BaseModelListener<ObjectEntry>
	implements RelevantObjectEntryModelListener {

	@Override
	public String getObjectDefinitionExternalReferenceCode() {
		return MCPServerConstants.EXTERNAL_REFERENCE_CODE_MCP_SERVER_PROFILE;
	}

	@Override
	public void onAfterCreate(ObjectEntry objectEntry)
		throws ModelListenerException {

		_invalidateServlet(
			objectEntry, MapUtil.getString(objectEntry.getValues(), "name"));

		_addMCPServerProfileDataMasks(objectEntry);
	}

	@Override
	public void onAfterRemove(ObjectEntry objectEntry)
		throws ModelListenerException {

		_invalidateServlet(
			objectEntry, MapUtil.getString(objectEntry.getValues(), "name"));
	}

	@Override
	public void onAfterUpdate(
			ObjectEntry originalObjectEntry, ObjectEntry objectEntry)
		throws ModelListenerException {

		_invalidateServlet(
			objectEntry,
			MapUtil.getString(originalObjectEntry.getValues(), "name"));
	}

	@Override
	public void onBeforeRemove(ObjectEntry objectEntry)
		throws ModelListenerException {

		_deleteMCPServerProfileDataMaskObjectEntries(objectEntry);
		_deleteMCPServerRestrictedFieldObjectEntries(objectEntry);
	}

	private void _addMCPServerProfileDataMasks(
		ObjectEntry mcpServerProfileObjectEntry) {

		long companyId = mcpServerProfileObjectEntry.getCompanyId();

		ObjectDefinition dataMaskObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					MCPServerConstants.EXTERNAL_REFERENCE_CODE_DATA_MASK,
					companyId);

		ObjectDefinition mcpServerProfileDataMaskObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					MCPServerConstants.
						EXTERNAL_REFERENCE_CODE_MCP_SERVER_PROFILE_DATA_MASK,
					companyId);

		if ((dataMaskObjectDefinition == null) ||
			(mcpServerProfileDataMaskObjectDefinition == null)) {

			return;
		}

		int executionOrder = 1;

		for (ObjectEntry dataMaskObjectEntry :
				_objectEntryLocalService.getObjectEntries(
					0, dataMaskObjectDefinition.getObjectDefinitionId(),
					QueryUtil.ALL_POS, QueryUtil.ALL_POS)) {

			Map<String, Serializable> values = dataMaskObjectEntry.getValues();

			if (!Objects.equals(values.get("maskType"), "system")) {
				continue;
			}

			try {
				_objectEntryLocalService.addObjectEntry(
					0, mcpServerProfileObjectEntry.getUserId(),
					mcpServerProfileDataMaskObjectDefinition.
						getObjectDefinitionId(),
					ObjectEntryFolderConstants.
						PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
					null,
					HashMapBuilder.<String, Serializable>put(
						"dataMaskExternalReferenceCode",
						dataMaskObjectEntry.getExternalReferenceCode()
					).put(
						"executionOrder", executionOrder
					).put(
						"mcpServerProfileExternalReferenceCode",
						mcpServerProfileObjectEntry.getExternalReferenceCode()
					).build(),
					new ServiceContext());

				executionOrder++;
			}
			catch (PortalException portalException) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						StringBundler.concat(
							"Unable to attach system mask \"",
							dataMaskObjectEntry.getExternalReferenceCode(),
							"\" to profile \"",
							mcpServerProfileObjectEntry.
								getExternalReferenceCode(),
							"\""),
						portalException);
				}
			}
		}
	}

	private void _deleteMCPServerProfileDataMaskObjectEntries(
		ObjectEntry mcpServerProfileObjectEntry) {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					MCPServerConstants.
						EXTERNAL_REFERENCE_CODE_MCP_SERVER_PROFILE_DATA_MASK,
					mcpServerProfileObjectEntry.getCompanyId());

		if (objectDefinition == null) {
			return;
		}

		String externalReferenceCode =
			mcpServerProfileObjectEntry.getExternalReferenceCode();

		for (ObjectEntry mcpServerProfileDataMaskObjectEntry :
				_objectEntryLocalService.getObjectEntries(
					0, objectDefinition.getObjectDefinitionId(),
					QueryUtil.ALL_POS, QueryUtil.ALL_POS)) {

			Map<String, Serializable> values =
				mcpServerProfileDataMaskObjectEntry.getValues();

			if (!Objects.equals(
					values.get("mcpServerProfileExternalReferenceCode"),
					externalReferenceCode)) {

				continue;
			}

			try {
				Map<String, Serializable> newValues =
					HashMapBuilder.<String, Serializable>putAll(
						values
					).put(
						"deleteReason", "MCP server profile was deleted."
					).build();

				_objectEntryLocalService.updateObjectEntry(
					mcpServerProfileDataMaskObjectEntry.getUserId(),
					mcpServerProfileDataMaskObjectEntry.getObjectEntryId(),
					mcpServerProfileDataMaskObjectEntry.
						getObjectEntryFolderId(),
					newValues, new ServiceContext());

				mcpServerProfileDataMaskObjectEntry.setValues(newValues);

				_objectEntryLocalService.deleteObjectEntry(
					mcpServerProfileDataMaskObjectEntry);
			}
			catch (PortalException portalException) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						StringBundler.concat(
							"Unable to delete object entry ",
							mcpServerProfileDataMaskObjectEntry.
								getExternalReferenceCode(),
							" for profile ", externalReferenceCode),
						portalException);
				}
			}
		}
	}

	private void _deleteMCPServerRestrictedFieldObjectEntries(
		ObjectEntry mcpServerProfileObjectEntry) {

		for (ObjectEntry mcpServerProfileToolObjectEntry :
				_getRelatedObjectEntries(
					mcpServerProfileObjectEntry, "mcpServerProfileToTools")) {

			for (ObjectEntry mcpServerRestrictedFieldObjectEntry :
					_getRelatedObjectEntries(
						mcpServerProfileToolObjectEntry,
						"mcpServerToolToRestrictedFields")) {

				try {
					Map<String, Serializable> newValues =
						HashMapBuilder.<String, Serializable>putAll(
							mcpServerRestrictedFieldObjectEntry.getValues()
						).put(
							"deleteReason", "MCP server profile was deleted."
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
								" for profile ",
								mcpServerProfileObjectEntry.
									getExternalReferenceCode()),
							portalException);
					}
				}
			}
		}
	}

	private List<ObjectEntry> _getRelatedObjectEntries(
		ObjectEntry objectEntry, String objectRelationshipName) {

		try {
			ObjectRelationship objectRelationship =
				_objectRelationshipLocalService.getObjectRelationship(
					objectEntry.getObjectDefinitionId(),
					objectRelationshipName);

			return _objectEntryLocalService.getOneToManyObjectEntries(
				0, objectRelationship.getObjectRelationshipId(), null, false,
				objectEntry.getObjectEntryId(), true, null, QueryUtil.ALL_POS,
				QueryUtil.ALL_POS, null);
		}
		catch (PortalException portalException) {
			throw new RuntimeException(portalException);
		}
	}

	private void _invalidateServlet(
		ObjectEntry mcpServerProfileObjectEntry, String profileName) {

		MCPServerServlet mcpServerServlet = (MCPServerServlet)_servlet;

		mcpServerServlet.invalidate(
			mcpServerProfileObjectEntry.getCompanyId(), profileName);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		MCPServerProfileObjectEntryModelListener.class);

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