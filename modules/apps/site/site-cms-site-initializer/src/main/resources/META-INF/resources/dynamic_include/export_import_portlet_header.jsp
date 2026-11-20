<%--
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %><%@
taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>

<%@ page import="com.liferay.portal.kernel.util.HtmlUtil" %><%@
page import="com.liferay.portal.kernel.util.HttpComponentsUtil" %><%@
page import="com.liferay.portal.kernel.util.JavaConstants" %><%@
page import="com.liferay.portal.kernel.util.ParamUtil" %><%@
page import="com.liferay.portal.kernel.util.Validator" %>

<%@ page import="jakarta.portlet.PortletRequest" %>

<liferay-theme:defineObjects />

<c:if test="<%= portletDisplay.isShowBackIcon() %>">
	<aui:style type="text/css">
		body:has(nav.cms-control-menu .cms-title) #portlet_<%= portletDisplay.getId() %> .portlet-header,
		nav.cms-control-menu .tbar-item:has(> [data-lfr-editable-id='sidebarImage']) {
			display: none;
		}

		nav.cms-control-menu .tbar-item:has(.cms-title) {
			visibility: hidden;
		}
	</aui:style>

	<aui:script position="inline">
		(function () {
			const pageBarElement = document.querySelector(
				'nav.cms-control-menu .tbar-nav'
			);
			const portletHeaderElement = document.querySelector(
				'#portlet_<%= portletDisplay.getId() %> .portlet-header'
			);

			const pageBarTitleElement = pageBarElement?.querySelector('.cms-title');

			if (!pageBarTitleElement || !portletHeaderElement) {
				return;
			}

			const pageBarTitleItemElement = pageBarTitleElement.closest('.tbar-item');

			pageBarTitleItemElement.after(
				...portletHeaderElement.querySelectorAll('.tbar-nav > .tbar-item')
			);

			pageBarTitleItemElement.classList.add('d-none');
		})();
	</aui:script>
</c:if>

<%
PortletRequest portletRequest = (PortletRequest)request.getAttribute(JavaConstants.JAKARTA_PORTLET_REQUEST);

String originURL = ParamUtil.getString(portletRequest, "redirect");

if (Validator.isNull(originURL)) {
	originURL = HttpComponentsUtil.decodeURL(HttpComponentsUtil.getParameter(ParamUtil.getString(portletRequest, "backURL"), portletDisplay.getNamespace() + "redirect", false));
}
%>

<c:if test="<%= Validator.isNotNull(originURL) %>">
	<aui:script position="inline">
		(function () {
			const sidebarElement = document.querySelector('.sidebar-body');

			if (!sidebarElement) {
				return;
			}

			const originPathname = new URL(
				'<%= HtmlUtil.escapeJS(originURL) %>',
				window.location.href
			).pathname;

			const markOriginLinkActive = () => {
				const [originLinkElement] = Array.from(
					sidebarElement.querySelectorAll('a.nav-link[href]')
				)
					.filter((linkElement) => {
						const pathname = new URL(linkElement.href).pathname;

						return (
							originPathname === pathname ||
							originPathname.startsWith(pathname + '/')
						);
					})
					.sort(
						(linkElement1, linkElement2) =>
							linkElement2.href.length - linkElement1.href.length
					);

				if (
					originLinkElement &&
					!originLinkElement.classList.contains('active')
				) {
					originLinkElement.classList.add('active');
					originLinkElement.setAttribute('aria-current', 'page');
				}
			};

			markOriginLinkActive();

			new MutationObserver(markOriginLinkActive).observe(sidebarElement, {
				childList: true,
				subtree: true,
			});
		})();
	</aui:script>
</c:if>