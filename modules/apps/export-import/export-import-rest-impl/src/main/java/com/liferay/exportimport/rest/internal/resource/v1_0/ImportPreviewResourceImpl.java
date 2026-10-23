/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.rest.internal.resource.v1_0;

import com.liferay.exportimport.kernel.configuration.ExportImportConfigurationSettingsMapFactoryUtil;
import com.liferay.exportimport.kernel.configuration.constants.ExportImportConfigurationConstants;
import com.liferay.exportimport.kernel.lar.ExportImportGroup;
import com.liferay.exportimport.kernel.lar.ExportImportHelper;
import com.liferay.exportimport.kernel.lar.ManifestSummary;
import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.exportimport.kernel.lar.PortletDataContextFactory;
import com.liferay.exportimport.kernel.lar.PortletDataHandler;
import com.liferay.exportimport.kernel.lar.UserIdStrategy;
import com.liferay.exportimport.kernel.model.ExportImportConfiguration;
import com.liferay.exportimport.kernel.service.ExportImportConfigurationLocalService;
import com.liferay.exportimport.kernel.service.ExportImportLocalService;
import com.liferay.exportimport.kernel.staging.Staging;
import com.liferay.exportimport.rest.dto.v1_0.ImportPreview;
import com.liferay.exportimport.rest.dto.v1_0.PreviewPortletDataHandler;
import com.liferay.exportimport.rest.dto.v1_0.PreviewSite;
import com.liferay.exportimport.rest.internal.util.GroupUtil;
import com.liferay.exportimport.rest.internal.util.ImportTempFileEntryUtil;
import com.liferay.exportimport.rest.internal.util.PermissionUtil;
import com.liferay.exportimport.rest.internal.util.PreviewPortletDataHandlerUtil;
import com.liferay.exportimport.rest.resource.v1_0.ImportPreviewResource;
import com.liferay.layout.admin.constants.LayoutAdminPortletKeys;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.PortletLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.upload.UploadServletRequest;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.zip.ZipReader;
import com.liferay.portal.kernel.zip.ZipReaderFactory;
import com.liferay.portal.vulcan.multipart.BinaryFile;
import com.liferay.portal.vulcan.multipart.MultipartBody;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestWrapper;

import jakarta.ws.rs.BadRequestException;

import java.io.File;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Daniel Raposo
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/import-preview.properties",
	scope = ServiceScope.PROTOTYPE, service = ImportPreviewResource.class
)
public class ImportPreviewResourceImpl extends BaseImportPreviewResourceImpl {

	@Override
	public ImportPreview postAssetLibraryImportPreview(
			String assetLibraryExternalReferenceCode, Long plid,
			String portletId, MultipartBody multipartBody)
		throws Exception {

		return _getImportPreview(
			GroupUtil.getAssetLibraryGroup(
				contextCompany.getCompanyId(),
				assetLibraryExternalReferenceCode),
			multipartBody, GetterUtil.getLong(plid), portletId);
	}

	@Override
	public ImportPreview postImportPreview(
			Long plid, String portletId, MultipartBody multipartBody)
		throws Exception {

		return _getImportPreview(
			GroupUtil.getCompanyGroup(contextCompany.getCompanyId()),
			multipartBody, GetterUtil.getLong(plid), portletId);
	}

	@Override
	public ImportPreview postSiteImportPreview(
			String siteExternalReferenceCode, Long plid, String portletId,
			MultipartBody multipartBody)
		throws Exception {

		return _getImportPreview(
			GroupUtil.getSiteGroup(
				contextCompany.getCompanyId(), siteExternalReferenceCode),
			multipartBody, GetterUtil.getLong(plid), portletId);
	}

	private ExportImportConfiguration _addExportImportConfiguration(
			long groupId, long plid, String portletId)
		throws Exception {

		if (Validator.isBlank(portletId)) {
			return _exportImportConfigurationLocalService.
				addExportImportConfiguration(
					contextUser.getUserId(), groupId, null, null,
					ExportImportConfigurationConstants.TYPE_IMPORT_LAYOUT,
					ExportImportConfigurationSettingsMapFactoryUtil.
						buildImportLayoutSettingsMap(
							contextUser.getUserId(), groupId, false, null,
							new HashMap<>(),
							contextAcceptLanguage.getPreferredLocale(),
							contextUser.getTimeZone()),
					new ServiceContext());
		}

		return _exportImportConfigurationLocalService.
			addExportImportConfiguration(
				contextUser.getUserId(), groupId, null, null,
				ExportImportConfigurationConstants.TYPE_IMPORT_PORTLET,
				ExportImportConfigurationSettingsMapFactoryUtil.
					buildImportPortletSettingsMap(
						contextUser.getUserId(), plid, groupId, portletId,
						new HashMap<>(),
						contextAcceptLanguage.getPreferredLocale(),
						contextUser.getTimeZone()),
				new ServiceContext());
	}

	private List<ExportImportGroup> _getExportImportGroups(
			PortletDataContext portletDataContext)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled(
				contextCompany.getCompanyId(), "LPD-85946")) {

			return Collections.emptyList();
		}

		return _exportImportHelper.getExportImportGroups(portletDataContext);
	}

	private ImportPreview _getImportPreview(
			BinaryFile binaryFile, File file, long groupId, long plid,
			String portletId)
		throws Exception {

		_validateImportFile(file, groupId, plid, portletId);

		try (ZipReader zipReader = _zipReaderFactory.getZipReader(file)) {
			PortletDataContext portletDataContext =
				_portletDataContextFactory.createImportPortletDataContext(
					contextCompany.getCompanyId(), groupId, new HashMap<>(),
					_exportImportHelper.getUserIdStrategy(
						contextUser.getUserId(),
						UserIdStrategy.CURRENT_USER_ID),
					zipReader);

			ManifestSummary manifestSummary =
				_exportImportHelper.getManifestSummary(portletDataContext);

			List<ExportImportGroup> exportImportGroups = _getExportImportGroups(
				portletDataContext);

			Locale locale = contextAcceptLanguage.getPreferredLocale();

			Map<String, List<PreviewPortletDataHandler>>
				previewPortletDataHandlersMap =
					_getPreviewPortletDataHandlersMap(
						locale, manifestSummary, portletId);

			FileEntry fileEntry = ImportTempFileEntryUtil.addTempFileEntry(
				groupId, contextUser.getUserId(), binaryFile.getFileName(),
				file, binaryFile.getContentType());

			return new ImportPreview() {
				{
					setAdditionCount(
						() -> PreviewPortletDataHandlerUtil.getAdditionCount(
							previewPortletDataHandlersMap));
					setAuthor(fileEntry::getUserName);
					setDeletionCount(
						() -> PreviewPortletDataHandlerUtil.getDeletionCount(
							previewPortletDataHandlersMap));
					setExportDate(manifestSummary::getExportDate);
					setFileName(
						() -> ImportTempFileEntryUtil.getFileName(fileEntry));
					setFileSize(fileEntry::getSize);
					setPreviewPortletDataHandlerSections(
						() ->
							PreviewPortletDataHandlerUtil.
								toPreviewPortletDataHandlerSections(
									locale, previewPortletDataHandlersMap));
					setPreviewSites(() -> _toPreviewSites(exportImportGroups));
				}
			};
		}
	}

	private ImportPreview _getImportPreview(
			Group group, MultipartBody multipartBody, long plid,
			String portletId)
		throws Exception {

		long groupId = group.getGroupId();

		PermissionUtil.checkImportPermission(
			contextCompany.getCompanyId(), groupId,
			PermissionUtil.getGroupActionId(portletId));

		BinaryFile binaryFile = multipartBody.getBinaryFile("file");

		File uploadedFile = _getUploadedFile();

		File file = uploadedFile;

		if (file == null) {
			file = FileUtil.createTempFile(binaryFile.getInputStream());
		}

		try {
			return _getImportPreview(
				binaryFile, file, groupId, plid, portletId);
		}
		finally {
			if (uploadedFile == null) {
				FileUtil.delete(file);
			}
		}
	}

	private String _getMessage(JSONObject jsonObject) {
		JSONArray messageListItemsJSONArray = jsonObject.getJSONArray(
			"messageListItems");

		if ((messageListItemsJSONArray == null) ||
			(messageListItemsJSONArray.length() == 0)) {

			return jsonObject.getString("message");
		}

		StringBundler sb = new StringBundler(
			(messageListItemsJSONArray.length() * 7) + 1);

		sb.append(jsonObject.getString("message"));

		for (int i = 0; i < messageListItemsJSONArray.length(); i++) {
			JSONObject messageListItemJSONObject =
				messageListItemsJSONArray.getJSONObject(i);

			sb.append(StringPool.NEW_LINE);
			sb.append(messageListItemJSONObject.getString("type"));
			sb.append(StringPool.SPACE);
			sb.append(messageListItemJSONObject.getString("name"));
			sb.append(StringPool.COLON);
			sb.append(StringPool.SPACE);
			sb.append(messageListItemJSONObject.getString("info"));
		}

		return sb.toString();
	}

	private Map<String, List<PreviewPortletDataHandler>>
			_getPreviewPortletDataHandlersMap(
				Locale locale, ManifestSummary manifestSummary,
				String portletId)
		throws Exception {

		Map<String, List<PreviewPortletDataHandler>>
			previewPortletDataHandlersMap = new LinkedHashMap<>();

		boolean portletScoped = !Validator.isBlank(portletId);

		List<Portlet> portlets = manifestSummary.getDataPortlets();

		if (portletScoped) {
			portlets = ListUtil.fromArray(
				_portletLocalService.getPortletById(
					contextCompany.getCompanyId(), portletId));
		}

		for (Portlet portlet : portlets) {
			PortletDataHandler portletDataHandler =
				portlet.getPortletDataHandlerInstance();

			if ((portletDataHandler == null) ||
				(portletDataHandler.isHidden() &&
				 !LayoutAdminPortletKeys.LAYOUT_SET_LAYOUTS.equals(
					 portlet.getPortletId()))) {

				continue;
			}

			PreviewPortletDataHandlerUtil.addPreviewPortletDataHandler(
				contextCompany.getCompanyId(), locale, manifestSummary, portlet,
				portletDataHandler,
				PortletDataHandler::getImportPortletDataHandlerControls,
				portletScoped, previewPortletDataHandlersMap);
		}

		if (portletScoped) {
			Portlet portlet = _portletLocalService.getPortletById(
				contextCompany.getCompanyId(), portletId);

			PortletDataHandler portletDataHandler =
				portlet.getPortletDataHandlerInstance();

			if (portletDataHandler != null) {
				PreviewPortletDataHandlerUtil.
					addConfigurationPreviewPortletDataHandler(
						locale, portlet,
						portletDataHandler.
							getImportConfigurationPortletDataHandlerControls(
								portlet, manifestSummary),
						previewPortletDataHandlersMap);
			}
		}

		return previewPortletDataHandlersMap;
	}

	private File _getUploadedFile() {
		ServletRequest servletRequest = contextHttpServletRequest;

		while (servletRequest instanceof ServletRequestWrapper) {
			if (servletRequest instanceof UploadServletRequest) {
				break;
			}

			ServletRequestWrapper servletRequestWrapper =
				(ServletRequestWrapper)servletRequest;

			servletRequest = servletRequestWrapper.getRequest();
		}

		if (!(servletRequest instanceof UploadServletRequest)) {
			return null;
		}

		UploadServletRequest uploadServletRequest =
			(UploadServletRequest)servletRequest;

		return uploadServletRequest.getFile("file", true);
	}

	private PreviewSite[] _toPreviewSites(
		List<ExportImportGroup> exportImportGroups) {

		return transformToArray(
			exportImportGroups,
			exportImportGroup -> new PreviewSite() {
				{
					setChildSitesCount(exportImportGroup::getChildGroupsCount);
					setDescriptiveName(exportImportGroup::getDescriptiveName);
					setExistsInInstance(
						() -> {
							Group group =
								groupLocalService.
									fetchGroupByExternalReferenceCode(
										exportImportGroup.
											getExternalReferenceCode(),
										contextCompany.getCompanyId());

							return group != null;
						});
					setExternalReferenceCode(
						exportImportGroup::getExternalReferenceCode);
					setPath(exportImportGroup::getPath);
				}
			},
			PreviewSite.class);
	}

	private void _validateImportFile(
			File file, long groupId, long plid, String portletId)
		throws Exception {

		ExportImportConfiguration exportImportConfiguration =
			_addExportImportConfiguration(groupId, plid, portletId);

		try {
			if (Validator.isBlank(portletId)) {
				_exportImportLocalService.validateImportLayoutsFile(
					exportImportConfiguration, file);
			}
			else {
				_exportImportLocalService.validateImportPortletInfo(
					exportImportConfiguration, file);
			}
		}
		catch (PortalException portalException) {
			JSONObject jsonObject = _staging.getExceptionMessagesJSONObject(
				contextAcceptLanguage.getPreferredLocale(), portalException,
				exportImportConfiguration);

			throw new BadRequestException(_getMessage(jsonObject));
		}
		finally {
			_exportImportConfigurationLocalService.
				deleteExportImportConfiguration(exportImportConfiguration);
		}
	}

	@Reference
	private ExportImportConfigurationLocalService
		_exportImportConfigurationLocalService;

	@Reference
	private ExportImportHelper _exportImportHelper;

	@Reference
	private ExportImportLocalService _exportImportLocalService;

	@Reference
	private PortletDataContextFactory _portletDataContextFactory;

	@Reference
	private PortletLocalService _portletLocalService;

	@Reference
	private Staging _staging;

	@Reference
	private ZipReaderFactory _zipReaderFactory;

}