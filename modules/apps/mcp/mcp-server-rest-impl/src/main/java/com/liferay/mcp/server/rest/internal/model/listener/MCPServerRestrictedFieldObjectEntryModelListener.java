/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.model.listener;

import com.liferay.mcp.server.rest.internal.constants.MCPServerConstants;
import com.liferay.mcp.server.rest.internal.servlet.MCPServerServlet;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.model.listener.RelevantObjectEntryModelListener;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.util.PortalInstances;

import jakarta.servlet.Servlet;

import jakarta.validation.ValidationException;

import java.io.Serializable;

import java.util.List;
import java.util.Objects;

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

		_deleteDescendantMCPServerRestrictedFieldObjectEntries(objectEntry);

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

		if (_isFieldNameModified(originalObjectEntry, objectEntry)) {
			_deleteDescendantMCPServerRestrictedFieldObjectEntries(objectEntry);
		}

		_invalidateServlet(objectEntry);
	}

	@Override
	public void onBeforeCreate(ObjectEntry objectEntry)
		throws ModelListenerException {

		_validateFieldName(objectEntry);
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

	@Override
	public void onBeforeUpdate(
			ObjectEntry originalObjectEntry, ObjectEntry objectEntry)
		throws ModelListenerException {

		if (_isFieldNameModified(originalObjectEntry, objectEntry)) {
			_validateFieldName(objectEntry);
		}
	}

	private void _deleteDescendantMCPServerRestrictedFieldObjectEntries(
			ObjectEntry mcpServerRestrictedFieldObjectEntry)
		throws ModelListenerException {

		String fieldName = MapUtil.getString(
			mcpServerRestrictedFieldObjectEntry.getValues(), "fieldName");

		for (ObjectEntry objectEntry :
				_getMCPServerRestrictedFieldObjectEntries(
					mcpServerRestrictedFieldObjectEntry)) {

			if (_isAncestorFieldName(
					fieldName,
					MapUtil.getString(objectEntry.getValues(), "fieldName"))) {

				_deleteMCPServerRestrictedFieldObjectEntry(
					fieldName, objectEntry);
			}
		}
	}

	private void _deleteMCPServerRestrictedFieldObjectEntry(
			String ancestorFieldName,
			ObjectEntry mcpServerRestrictedFieldObjectEntry)
		throws ModelListenerException {

		mcpServerRestrictedFieldObjectEntry.setValues(
			HashMapBuilder.<String, Serializable>putAll(
				mcpServerRestrictedFieldObjectEntry.getValues()
			).put(
				"deleteReason",
				"Restricted field \"" + ancestorFieldName + "\" was added."
			).build());

		try {
			_objectEntryLocalService.deleteObjectEntry(
				mcpServerRestrictedFieldObjectEntry);
		}
		catch (PortalException portalException) {
			throw new ModelListenerException(portalException);
		}
	}

	private ObjectEntry _fetchMCPServerProfileToolObjectEntry(
		ObjectEntry mcpServerRestrictedFieldObjectEntry) {

		return _objectEntryLocalService.fetchObjectEntry(
			MapUtil.getLong(
				mcpServerRestrictedFieldObjectEntry.getValues(),
				"r_mcpServerToolToRestrictedFields_l_mcpServerProfileToolId"));
	}

	private List<ObjectEntry> _getMCPServerRestrictedFieldObjectEntries(
			ObjectEntry mcpServerRestrictedFieldObjectEntry)
		throws ModelListenerException {

		ObjectEntry mcpServerProfileToolObjectEntry =
			_fetchMCPServerProfileToolObjectEntry(
				mcpServerRestrictedFieldObjectEntry);

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
			throw new ModelListenerException(portalException);
		}
	}

	private void _invalidateServlet(
		ObjectEntry mcpServerRestrictedFieldObjectEntry) {

		ObjectEntry mcpServerProfileToolObjectEntry =
			_fetchMCPServerProfileToolObjectEntry(
				mcpServerRestrictedFieldObjectEntry);

		if (mcpServerProfileToolObjectEntry == null) {
			return;
		}

		ObjectEntry mcpServerProfileObjectEntry =
			_objectEntryLocalService.fetchObjectEntry(
				MapUtil.getLong(
					mcpServerProfileToolObjectEntry.getValues(),
					"r_mcpServerProfileToTools_l_mcpServerProfileId"));

		if (mcpServerProfileObjectEntry == null) {
			return;
		}

		MCPServerServlet mcpServerServlet = (MCPServerServlet)_servlet;

		mcpServerServlet.invalidate(
			mcpServerRestrictedFieldObjectEntry.getCompanyId(),
			MapUtil.getString(mcpServerProfileObjectEntry.getValues(), "name"));
	}

	private boolean _isAncestorFieldName(
		String ancestorFieldName, String descendantFieldName) {

		return descendantFieldName.startsWith(
			ancestorFieldName + StringPool.PERIOD);
	}

	private boolean _isFieldNameModified(
		ObjectEntry originalObjectEntry, ObjectEntry objectEntry) {

		return !Objects.equals(
			MapUtil.getString(originalObjectEntry.getValues(), "fieldName"),
			MapUtil.getString(objectEntry.getValues(), "fieldName"));
	}

	private void _validateFieldName(
			ObjectEntry mcpServerRestrictedFieldObjectEntry)
		throws ModelListenerException {

		String fieldName = MapUtil.getString(
			mcpServerRestrictedFieldObjectEntry.getValues(), "fieldName");

		if (fieldName.contains(StringPool.COMMA)) {
			throw new ModelListenerException(
				new ValidationException(
					"Unable to restrict more than one field at a time"));
		}

		for (ObjectEntry objectEntry :
				_getMCPServerRestrictedFieldObjectEntries(
					mcpServerRestrictedFieldObjectEntry)) {

			if (objectEntry.getObjectEntryId() ==
					mcpServerRestrictedFieldObjectEntry.getObjectEntryId()) {

				continue;
			}

			String ancestorFieldName = MapUtil.getString(
				objectEntry.getValues(), "fieldName");

			if (_isAncestorFieldName(ancestorFieldName, fieldName)) {
				throw new ModelListenerException(
					new ValidationException(
						StringBundler.concat(
							"Unable to restrict field \"", fieldName,
							"\" because restricted field \"", ancestorFieldName,
							"\" already hides it")));
			}
		}
	}

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

	@Reference(
		target = "(osgi.http.whiteboard.servlet.name=com.liferay.mcp.server.rest.internal.servlet.MCPServerServlet)"
	)
	private Servlet _servlet;

}