/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.rest.internal.resource.v1_0;

import com.liferay.exportimport.constants.ExportImportConstants;
import com.liferay.exportimport.kernel.lar.ExportImportHelper;
import com.liferay.exportimport.kernel.lar.PortletDataContextFactory;
import com.liferay.exportimport.kernel.lar.PortletDataHandlerKeys;
import com.liferay.exportimport.lar.DeletionSystemEventExporter;
import com.liferay.exportimport.portlet.data.handler.provider.PortletDataHandlerProvider;
import com.liferay.exportimport.rest.dto.v1_0.ExportPreview;
import com.liferay.exportimport.rest.dto.v1_0.PreviewPortletDataHandler;
import com.liferay.exportimport.rest.internal.util.GroupUtil;
import com.liferay.exportimport.rest.internal.util.ParameterMapUtil;
import com.liferay.exportimport.rest.internal.util.PermissionUtil;
import com.liferay.exportimport.rest.internal.util.PreviewPortletDataHandlerUtil;
import com.liferay.exportimport.rest.resource.v1_0.ExportPreviewResource;
import com.liferay.exportimport.staticsite.constants.StaticSiteExportConstants;
import com.liferay.layout.admin.constants.LayoutAdminPortletKeys;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.service.PortletLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Daniel Raposo
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/export-preview.properties",
	scope = ServiceScope.PROTOTYPE, service = ExportPreviewResource.class
)
public class ExportPreviewResourceImpl extends BaseExportPreviewResourceImpl {

	@Override
	public ExportPreview getAssetLibraryExportPreview(
			String assetLibraryExternalReferenceCode, String dateRangeType,
			Date endDate, String outputFormat, Long plid, String portletId,
			Date startDate)
		throws Exception {

		return _getExportPreview(
			dateRangeType, endDate,
			GroupUtil.getAssetLibraryGroup(
				contextCompany.getCompanyId(),
				assetLibraryExternalReferenceCode),
			outputFormat, GetterUtil.getLong(plid), portletId, startDate);
	}

	@Override
	public ExportPreview getExportPreview(
			String dateRangeType, Date endDate, String outputFormat, Long plid,
			String portletId, Date startDate)
		throws Exception {

		return _getExportPreview(
			dateRangeType, endDate,
			GroupUtil.getCompanyGroup(contextCompany.getCompanyId()),
			outputFormat, GetterUtil.getLong(plid), portletId, startDate);
	}

	@Override
	public ExportPreview getSiteExportPreview(
			String siteExternalReferenceCode, String dateRangeType,
			Date endDate, String outputFormat, Long plid, String portletId,
			Date startDate)
		throws Exception {

		return _getExportPreview(
			dateRangeType, endDate,
			GroupUtil.getSiteGroup(
				contextCompany.getCompanyId(), siteExternalReferenceCode),
			outputFormat, GetterUtil.getLong(plid), portletId, startDate);
	}

	/**
	 * Narrows the preview to the one selection a static site export can act
	 * on, which is which pages to render. Everything else the preview offers
	 * describes models to serialize, and a static site export serializes
	 * rendered pages instead.
	 */
	private Map<String, List<PreviewPortletDataHandler>>
		_filterStaticSitePreviewPortletDataHandlersMap(
			Map<String, List<PreviewPortletDataHandler>>
				previewPortletDataHandlersMap) {

		List<PreviewPortletDataHandler> previewPortletDataHandlers =
			previewPortletDataHandlersMap.get(
				ExportImportConstants.SECTION_KEY_SITE_BUILDER);

		if (previewPortletDataHandlers == null) {
			return new LinkedHashMap<>();
		}

		List<PreviewPortletDataHandler> filteredPreviewPortletDataHandlers =
			new ArrayList<>();

		for (PreviewPortletDataHandler previewPortletDataHandler :
				previewPortletDataHandlers) {

			if (Objects.equals(
					previewPortletDataHandler.getName(),
					_LAYOUT_SET_LAYOUTS_PORTLET_DATA_NAME)) {

				filteredPreviewPortletDataHandlers.add(
					previewPortletDataHandler);
			}
		}

		if (filteredPreviewPortletDataHandlers.isEmpty()) {
			return new LinkedHashMap<>();
		}

		return HashMapBuilder.<String, List<PreviewPortletDataHandler>>put(
			ExportImportConstants.SECTION_KEY_SITE_BUILDER,
			filteredPreviewPortletDataHandlers
		).build();
	}

	private ExportPreview _getExportPreview(
			String dateRangeType, Date endDate, Group group,
			String outputFormat, long plid, String portletId, Date startDate)
		throws Exception {

		long groupId = group.getGroupId();

		PermissionUtil.checkExportPermission(
			contextCompany.getCompanyId(), groupId);

		boolean portletScoped = !Validator.isBlank(portletId);

		List<Portlet> portlets = null;

		if (portletScoped) {
			portlets = ListUtil.fromArray(
				_portletLocalService.getPortletById(
					contextCompany.getCompanyId(), portletId));
		}
		else {
			portlets = _exportImportHelper.getExportablePortlets(
				contextCompany.getCompanyId(), false, groupId);
		}

		Locale locale = contextAcceptLanguage.getPreferredLocale();

		Map<String, String[]> parameterMap =
			ParameterMapUtil.putDateRangeParameters(
				dateRangeType, startDate, endDate, new LinkedHashMap<>(),
				contextUser);

		Map<String, List<PreviewPortletDataHandler>>
			allPreviewPortletDataHandlersMap =
				PreviewPortletDataHandlerUtil.getPreviewPortletDataHandlersMap(
					_deletionSystemEventExporter, group, locale, parameterMap,
					plid, _portletDataContextFactory,
					_portletDataHandlerProvider, portlets, portletScoped,
					contextUser.getTimeZone());

		Map<String, List<PreviewPortletDataHandler>>
			previewPortletDataHandlersMap = allPreviewPortletDataHandlersMap;

		if (Objects.equals(
				outputFormat,
				StaticSiteExportConstants.OUTPUT_FORMAT_STATIC_HTML)) {

			previewPortletDataHandlersMap =
				_filterStaticSitePreviewPortletDataHandlersMap(
					allPreviewPortletDataHandlersMap);
		}

		Map<String, List<PreviewPortletDataHandler>>
			finalPreviewPortletDataHandlersMap = previewPortletDataHandlersMap;

		return new ExportPreview() {
			{
				setAdditionCount(
					() -> PreviewPortletDataHandlerUtil.getAdditionCount(
						finalPreviewPortletDataHandlersMap));
				setDeletionCount(
					() -> PreviewPortletDataHandlerUtil.getDeletionCount(
						finalPreviewPortletDataHandlersMap));
				setPreviewPortletDataHandlerSections(
					() ->
						PreviewPortletDataHandlerUtil.
							toPreviewPortletDataHandlerSections(
								locale, finalPreviewPortletDataHandlersMap));
			}
		};
	}

	private static final String _LAYOUT_SET_LAYOUTS_PORTLET_DATA_NAME =
		PortletDataHandlerKeys.PORTLET_DATA + "_" +
			LayoutAdminPortletKeys.LAYOUT_SET_LAYOUTS;

	@Reference
	private DeletionSystemEventExporter _deletionSystemEventExporter;

	@Reference
	private ExportImportHelper _exportImportHelper;

	@Reference
	private PortletDataContextFactory _portletDataContextFactory;

	@Reference
	private PortletDataHandlerProvider _portletDataHandlerProvider;

	@Reference
	private PortletLocalService _portletLocalService;

}