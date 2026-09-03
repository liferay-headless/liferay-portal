/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.staticsite.internal.render;

import com.liferay.layout.util.LayoutServiceContextHelper;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutSet;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.servlet.DummyHttpServletResponse;
import com.liferay.portal.kernel.servlet.DynamicServletRequest;
import com.liferay.portal.kernel.servlet.ServletContextPool;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.theme.ThemeUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.RenderLayoutContentThreadLocal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.webserver.WebServerServletTokenUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Renders a layout to a complete HTML document outside of a page request.
 *
 * <p>
 * The recipe is the one the portal already uses to render a display page for
 * the headless APIs: ask the layout for its body content, ask the theme for the
 * document that wraps it, and graft the first into the second.
 * </p>
 *
 * @author Alejandro Tardín
 * @see    com.liferay.headless.delivery.internal.dto.v1_0.util.DisplayPageRendererUtil
 */
public class LayoutHTMLRenderer {

	public LayoutHTMLRenderer(
		LayoutServiceContextHelper layoutServiceContextHelper, Portal portal) {

		_layoutServiceContextHelper = layoutServiceContextHelper;
		_portal = portal;
	}

	public Document render(Layout layout, Locale locale) throws Exception {
		boolean renderLayoutContent =
			RenderLayoutContentThreadLocal.isRenderLayoutContent();

		try (AutoCloseable autoCloseable =
				_layoutServiceContextHelper.getServiceContextAutoCloseable(
					layout)) {

			RenderLayoutContentThreadLocal.setRenderLayoutContent(true);

			ServiceContext serviceContext =
				ServiceContextThreadLocal.getServiceContext();

			ThemeDisplay themeDisplay = serviceContext.getThemeDisplay();

			HttpServletRequest httpServletRequest =
				DynamicServletRequest.addQueryString(
					themeDisplay.getRequest(), "p_l_id=" + layout.getPlid(),
					false);

			HttpServletResponse httpServletResponse =
				new DummyHttpServletResponse();

			httpServletRequest.setAttribute(WebKeys.LAYOUT, layout);
			httpServletRequest.setAttribute(WebKeys.LOCALE, locale);
			httpServletRequest.setAttribute(
				WebKeys.THEME_DISPLAY, themeDisplay);

			// Every reference the theme emits has to survive being moved onto a
			// file system, so keep the CDN out of them

			themeDisplay.setCDNHost(StringPool.BLANK);

			// The theme display built for a background thread carries less
			// than the one a page request builds, and the theme reads some of
			// what is missing. Without the image path and the logo the theme
			// renders an image tag with an empty source

			themeDisplay.setPathImage(_portal.getPathImage());

			LayoutSet logoLayoutSet = layout.getLayoutSet();

			themeDisplay.setCompanyLogo(
				_getCompanyLogo(themeDisplay.getCompany(), logoLayoutSet));

			themeDisplay.setLanguageId(LocaleUtil.toLanguageId(locale));
			themeDisplay.setLayout(layout);
			themeDisplay.setLocale(locale);
			themeDisplay.setPlid(layout.getPlid());
			themeDisplay.setRequest(httpServletRequest);
			themeDisplay.setResponse(httpServletResponse);

			layout.includeLayoutContent(
				httpServletRequest, httpServletResponse);

			LayoutSet layoutSet = layout.getLayoutSet();

			Document document = Jsoup.parse(
				ThemeUtil.include(
					ServletContextPool.get(StringPool.BLANK),
					httpServletRequest, httpServletResponse,
					"portal_normal.ftl", layoutSet.getTheme(), false));

			// The theme reaches its own content through an include that only
			// resolves when a Struts tiles definition named the content path in
			// the request, and a render outside a page request has no such
			// definition, so the include renders nothing at all. The layout
			// content is in the request regardless, so it is placed in the
			// container the theme wraps that include in. Replacing the body
			// instead, the way a display page renderer does, would take the
			// theme's chrome with it.

			StringBundler sb = (StringBundler)httpServletRequest.getAttribute(
				WebKeys.LAYOUT_CONTENT);

			if (sb != null) {
				Element element = document.getElementById(_CONTENT_ELEMENT_ID);

				if (element == null) {
					element = document.body();
				}

				element.html(sb.toString());
			}

			return document;
		}
		finally {
			RenderLayoutContentThreadLocal.setRenderLayoutContent(
				renderLayoutContent);
		}
	}

	/**
	 * Returns the logo the theme renders for the site, which is the layout
	 * set's own when it has one and the company's otherwise.
	 *
	 * @see com.liferay.portal.events.ServicePreAction
	 */
	private String _getCompanyLogo(Company company, LayoutSet layoutSet) {
		String pathImage = _portal.getPathImage();

		if (layoutSet.isLogo()) {
			long layoutSetLogoId = layoutSet.getLogoId();

			if (layoutSetLogoId == 0) {
				layoutSetLogoId = layoutSet.getLiveLogoId();
			}

			if (layoutSetLogoId > 0) {
				return StringBundler.concat(
					pathImage, "/layout_set_logo?img_id=", layoutSetLogoId,
					"&t=", WebServerServletTokenUtil.getToken(layoutSetLogoId));
			}
		}

		long logoId = company.getLogoId();

		if (logoId > 0) {
			return StringBundler.concat(
				pathImage, "/company_logo?img_id=", logoId, "&t=",
				WebServerServletTokenUtil.getToken(logoId));
		}

		return pathImage + "/company_logo";
	}

	private static final String _CONTENT_ELEMENT_ID = "content";

	private final LayoutServiceContextHelper _layoutServiceContextHelper;
	private final Portal _portal;

}