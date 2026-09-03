/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.staticsite.internal;

import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.exportimport.staticsite.StaticSiteExporter;
import com.liferay.exportimport.staticsite.internal.asset.StaticSiteAssetHarvester;
import com.liferay.exportimport.staticsite.internal.render.LayoutHTMLRenderer;
import com.liferay.exportimport.staticsite.internal.util.StaticSitePathUtil;
import com.liferay.layout.util.LayoutServiceContextHelper;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.Portal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jsoup.nodes.Document;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Writes the layouts selected by an export process into the export's zip as a
 * tree of static HTML files, together with the assets they reference.
 *
 * <p>
 * The tree is shaped so that it works as a document root with no web server
 * configuration at all: every page is an <code>index.html</code> inside a
 * directory named after its friendly URL, and every reference between the files
 * is relative, so the archive also works unzipped into a subdirectory.
 * </p>
 *
 * @author Alejandro Tardín
 */
@Component(service = StaticSiteExporter.class)
public class StaticSiteExporterImpl implements StaticSiteExporter {

	@Override
	public void export(PortletDataContext portletDataContext) throws Exception {
		Group group = _groupLocalService.getGroup(
			portletDataContext.getGroupId());

		Locale locale = _portal.getSiteDefaultLocale(group.getGroupId());

		LayoutHTMLRenderer layoutHTMLRenderer = new LayoutHTMLRenderer(
			_layoutServiceContextHelper, _portal);

		List<String> pagePaths = new ArrayList<>();
		List<String> skippedLayoutNames = new ArrayList<>();

		List<Layout> layouts = new ArrayList<>();

		for (long layoutId : portletDataContext.getLayoutIds()) {
			Layout layout = _layoutLocalService.fetchLayout(
				portletDataContext.getGroupId(),
				portletDataContext.isPrivateLayout(), layoutId);

			if (layout == null) {
				continue;
			}

			if (layout.isSystem() || !layout.isPublished()) {
				skippedLayoutNames.add(
					layout.getName(locale) + " (unpublished or system page)");

				continue;
			}

			layouts.add(layout);
		}

		// The first selected page answers for the document root, so it is
		// written there once instead of a second time under its own name, and
		// every link to it has to point at the root as well

		Map<String, String> pagePathsByFriendlyURLPath = new LinkedHashMap<>();

		for (int i = 0; i < layouts.size(); i++) {
			String friendlyURLPath = _getFriendlyURLPath(
				layouts.get(i), locale);

			if (i == 0) {
				pagePathsByFriendlyURLPath.put(
					friendlyURLPath, StaticSitePathUtil.INDEX_FILE_NAME);
			}
			else {
				pagePathsByFriendlyURLPath.put(
					friendlyURLPath,
					StaticSitePathUtil.getPagePath(friendlyURLPath));
			}
		}

		StaticSiteAssetHarvester staticSiteAssetHarvester =
			new StaticSiteAssetHarvester(
				_getPortalURL(group),
				_getSiteFriendlyURLPath(
					group, portletDataContext.isPrivateLayout()),
				pagePathsByFriendlyURLPath);

		for (Layout layout : layouts) {
			Document document = layoutHTMLRenderer.render(layout, locale);

			String pagePath = pagePathsByFriendlyURLPath.get(
				_getFriendlyURLPath(layout, locale));

			staticSiteAssetHarvester.rewrite(document, pagePath);

			portletDataContext.addZipEntry("/" + pagePath, document.html());

			pagePaths.add(pagePath);

			if (_log.isInfoEnabled()) {
				_log.info("Rendered " + pagePath);
			}
		}

		int harvestedCount = _harvestAssets(
			portletDataContext, staticSiteAssetHarvester, group);

		portletDataContext.addZipEntry(
			"/MANIFEST.txt",
			_getManifest(
				pagePaths, skippedLayoutNames, harvestedCount,
				staticSiteAssetHarvester));
	}

	private String _getFriendlyURLPath(Layout layout, Locale locale)
		throws Exception {

		// A layout's friendly URL is only its own segment. Nesting on disk has
		// to be rebuilt by walking up, or every child collapses into a sibling
		// of its parent

		StringBundler sb = new StringBundler();

		List<Layout> ancestorLayouts = layout.getAncestors();

		for (int i = ancestorLayouts.size() - 1; i >= 0; i--) {
			Layout ancestorLayout = ancestorLayouts.get(i);

			sb.append(ancestorLayout.getFriendlyURL(locale));
		}

		sb.append(layout.getFriendlyURL(locale));

		return sb.toString();
	}

	private String _getManifest(
		List<String> pagePaths, List<String> skippedLayoutNames,
		int harvestedCount, StaticSiteAssetHarvester staticSiteAssetHarvester) {

		StringBundler sb = new StringBundler();

		sb.append("Static site export\n\n");
		sb.append("Unzip this archive into a directory Apache serves. No web ");
		sb.append("server configuration is required.\n\n");
		sb.append("Pages (");
		sb.append(pagePaths.size());
		sb.append("):\n");

		for (String pagePath : pagePaths) {
			sb.append("\t");
			sb.append(pagePath);
			sb.append("\n");
		}

		sb.append("\nAssets harvested: ");
		sb.append(harvestedCount);
		sb.append(" of ");
		sb.append(
			staticSiteAssetHarvester.getAssetPaths(
			).size());
		sb.append("\n");

		if (!skippedLayoutNames.isEmpty()) {
			sb.append("\nPages skipped:\n");

			for (String skippedLayoutName : skippedLayoutNames) {
				sb.append("\t");
				sb.append(skippedLayoutName);
				sb.append("\n");
			}
		}

		if (!staticSiteAssetHarvester.getDroppedURLs(
			).isEmpty()) {

			sb.append("\nReferences the portal served dynamically. These ");
			sb.append("were neutralized and no longer work:\n");

			for (String droppedURL :
					staticSiteAssetHarvester.getDroppedURLs()) {

				sb.append("\t");
				sb.append(droppedURL);
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	private String _getPortalURL(Group group) throws Exception {
		Company company = _companyLocalService.getCompany(group.getCompanyId());

		boolean secure = false;

		return _portal.getPortalURL(
			company.getVirtualHostname(), _portal.getPortalServerPort(secure),
			secure);
	}

	private String _getSiteFriendlyURLPath(Group group, boolean privateLayout) {
		if (privateLayout) {
			return _portal.getPathFriendlyURLPrivateGroup() +
				group.getFriendlyURL();
		}

		return _portal.getPathFriendlyURLPublic() + group.getFriendlyURL();
	}

	private int _harvestAssets(
			PortletDataContext portletDataContext,
			StaticSiteAssetHarvester staticSiteAssetHarvester, Group group)
		throws Exception {

		Company company = _companyLocalService.getCompany(group.getCompanyId());

		String portalURL = _portal.getPortalURL(
			company.getVirtualHostname(), _portal.getPortalServerPort(false),
			false);

		int harvestedCount = 0;

		for (Map.Entry<String, String> entry :
				staticSiteAssetHarvester.getAssetPaths(
				).entrySet()) {

			String url = entry.getKey();

			if (!url.startsWith("http")) {
				url = portalURL + url;
			}

			try {

				// Fetching over HTTP proves the pipeline. Resolving these in
				// process, through the servlet context of the bundle that owns
				// each resource, is the production path

				byte[] bytes = _http.URLtoByteArray(url);

				if ((bytes != null) && (bytes.length > 0)) {
					portletDataContext.addZipEntry(
						"/" + entry.getValue(), bytes);

					harvestedCount++;
				}
			}
			catch (Exception exception) {
				if (_log.isWarnEnabled()) {
					_log.warn("Unable to harvest " + url, exception);
				}
			}
		}

		return harvestedCount;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		StaticSiteExporterImpl.class);

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private Http _http;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private LayoutServiceContextHelper _layoutServiceContextHelper;

	@Reference
	private Portal _portal;

}