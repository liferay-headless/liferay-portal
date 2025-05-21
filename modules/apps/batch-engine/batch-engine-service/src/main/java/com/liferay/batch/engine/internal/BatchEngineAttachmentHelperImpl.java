/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal;

import com.liferay.batch.engine.BatchEngineAttachmentHelper;
import com.liferay.exportimport.kernel.lar.ExportImportPathUtil;
import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.exportimport.kernel.lar.PortletDataHandler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.model.PortletPreferences;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.PortletLocalService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Vendel Toreki
 */
@Component(service = BatchEngineAttachmentHelper.class)
public class BatchEngineAttachmentHelperImpl
	implements BatchEngineAttachmentHelper {

	@Override
	public void exportAttachments(
			PortletDataContext portletDataContext, String portletId)
		throws Exception {

		if (_log.isInfoEnabled()) {
			_log.info("Exporting attachments for portlet " + portletId);
		}

		Group companyGroup = _groupLocalService.getCompanyGroup(
			portletDataContext.getCompanyId());

		long globalGroupId = companyGroup.getGroupId();

		long originalGroupId = portletDataContext.getGroupId();
		long originalPlid = portletDataContext.getPlid();
		String originalPortletId = portletDataContext.getPortletId();
		long originalScopeGroupId = portletDataContext.getScopeGroupId();
		String originalScopeLayoutUuid =
			portletDataContext.getScopeLayoutUuid();
		String originalScopeType = portletDataContext.getScopeType();
		boolean originalValidateExistingDataHandler =
			portletDataContext.isValidateExistingDataHandler();

		portletDataContext.setGroupId(globalGroupId);
		portletDataContext.setPlid(0);
		portletDataContext.setPortletId(portletId);
		portletDataContext.setScopeGroupId(globalGroupId);
		portletDataContext.setScopeLayoutUuid("");
		portletDataContext.setScopeType("");
		portletDataContext.setValidateExistingDataHandler(false);

		try {
			String path = ExportImportPathUtil.getPortletDataPath(
				portletDataContext);

			if (portletDataContext.hasPrimaryKey(String.class, path)) {
				return;
			}

			Portlet dlPortlet = _portletLocalService.getPortletById(
				"com_liferay_document_library_web_portlet_DLAdminPortlet");

			PortletDataHandler portletDataHandler =
				dlPortlet.getPortletDataHandlerInstance();

			javax.portlet.PortletPreferences jxPortletPreferences =
				PortletPreferencesFactoryUtil.getStrictPortletSetup(
					portletDataContext.getCompanyId(),
					portletDataContext.getGroupId(), portletId);

			String data = portletDataHandler.exportData(
				portletDataContext, portletId, jxPortletPreferences);

			if (data != null) {
				portletDataContext.addZipEntry(path, data);
			}
		}
		finally {
			portletDataContext.clearScopedPrimaryKeys();

			portletDataContext.setGroupId(originalGroupId);
			portletDataContext.setPlid(originalPlid);
			portletDataContext.setPortletId(originalPortletId);
			portletDataContext.setScopeGroupId(originalScopeGroupId);
			portletDataContext.setScopeLayoutUuid(originalScopeLayoutUuid);
			portletDataContext.setScopeType(originalScopeType);
			portletDataContext.setValidateExistingDataHandler(
				originalValidateExistingDataHandler);
		}

		if (_log.isInfoEnabled()) {
			_log.info(
				"Exporting attachments finished for portlet " + portletId);
		}
	}

	@Override
	public void importAttachments(
			PortletDataContext portletDataContext, String portletId)
		throws Exception {

		if (_log.isInfoEnabled()) {
			_log.info("Importing attachments for portlet " + portletId);
		}

		Group companyGroup = _groupLocalService.getCompanyGroup(
			portletDataContext.getCompanyId());

		long globalGroupId = companyGroup.getGroupId();

		long originalGroupId = portletDataContext.getGroupId();
		long originalPlid = portletDataContext.getPlid();
		String originalPortletId = portletDataContext.getPortletId();
		long originalScopeGroupId = portletDataContext.getScopeGroupId();
		String originalScopeLayoutUuid =
			portletDataContext.getScopeLayoutUuid();
		String originalScopeType = portletDataContext.getScopeType();
		boolean originalValidateExistingDataHandler =
			portletDataContext.isValidateExistingDataHandler();

		portletDataContext.setGroupId(globalGroupId);
		portletDataContext.setPlid(0);
		portletDataContext.setPortletId(portletId);
		portletDataContext.setScopeGroupId(globalGroupId);
		portletDataContext.setScopeLayoutUuid("");
		portletDataContext.setScopeType("");
		portletDataContext.setValidateExistingDataHandler(false);

		try {
			String path = ExportImportPathUtil.getPortletDataPath(
				portletDataContext);

			if (portletDataContext.hasPrimaryKey(String.class, path)) {
				return;
			}

			String data = portletDataContext.getZipEntryAsString(path);

			if (data == null) {
				return;
			}

			Portlet dlPortlet = _portletLocalService.getPortletById(
				"com_liferay_document_library_web_portlet_DLAdminPortlet");

			PortletDataHandler portletDataHandler =
				dlPortlet.getPortletDataHandlerInstance();

			javax.portlet.PortletPreferences jxPortletPreferences =
				PortletPreferencesFactoryUtil.getStrictPortletSetup(
					portletDataContext.getCompanyId(),
					portletDataContext.getGroupId(), portletId);

			portletDataHandler.importData(
				portletDataContext, portletId, jxPortletPreferences, data);
		}
		finally {
			portletDataContext.clearScopedPrimaryKeys();

			portletDataContext.setGroupId(originalGroupId);
			portletDataContext.setPlid(originalPlid);
			portletDataContext.setPortletId(originalPortletId);
			portletDataContext.setScopeGroupId(originalScopeGroupId);
			portletDataContext.setScopeLayoutUuid(originalScopeLayoutUuid);
			portletDataContext.setScopeType(originalScopeType);
			portletDataContext.setValidateExistingDataHandler(
				originalValidateExistingDataHandler);
		}

		if (_log.isInfoEnabled()) {
			_log.info(
				"Importing attachments finished for portlet " + portletId);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BatchEngineAttachmentHelperImpl.class);

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private PortletLocalService _portletLocalService;

}