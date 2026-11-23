/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.admin.web.internal.configuration.admin.display;

import com.liferay.configuration.admin.display.ConfigurationScreen;
import com.liferay.configuration.admin.display.ConfigurationScreenWrapper;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.LayoutSet;
import com.liferay.portal.kernel.model.LayoutSetPrototype;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutSetLocalService;
import com.liferay.portal.kernel.service.LayoutSetPrototypeLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.site.settings.configuration.admin.display.SiteSettingsConfigurationScreenContributor;
import com.liferay.site.settings.configuration.admin.display.SiteSettingsConfigurationScreenFactory;
import com.liferay.sites.kernel.util.Sites;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;

import java.util.Dictionary;
import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Eudaldo Alonso
 */
@Component(service = ConfigurationScreen.class)
public class PagesSiteSettingsConfigurationScreenWrapper
	extends ConfigurationScreenWrapper {

	@Override
	protected ConfigurationScreen getConfigurationScreen() {
		return _siteSettingsConfigurationScreenFactory.create(
			new PagesSiteSettingsConfigurationScreenContributor());
	}

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private Language _language;

	@Reference
	private LayoutSetLocalService _layoutSetLocalService;

	@Reference
	private LayoutSetPrototypeLocalService _layoutSetPrototypeLocalService;

	@Reference(target = "(osgi.web.symbolicname=com.liferay.site.admin.web)")
	private ServletContext _servletContext;

	@Reference
	private SiteSettingsConfigurationScreenFactory
		_siteSettingsConfigurationScreenFactory;

	@Reference
	private Sites _sites;

	private class PagesSiteSettingsConfigurationScreenContributor
		implements SiteSettingsConfigurationScreenContributor {

		@Override
		public Dictionary<String, Object> exportProperties(
			Serializable scopePK) {

			LayoutSet layoutSet = _layoutSetLocalService.fetchLayoutSet(
				(long)scopePK, false);

			LayoutSetPrototype layoutSetPrototype = _fetchLayoutSetPrototype(
				layoutSet);

			return HashMapDictionaryBuilder.<String, Object>put(
				"publicLayoutSetPrototypeLinkEnabled",
				() -> {
					if (layoutSetPrototype == null) {
						return null;
					}

					return layoutSet.isLayoutSetPrototypeLinkEnabled();
				}
			).put(
				"publicLayoutSetPrototypeSiteExternalReferenceCode",
				() -> {
					if (layoutSetPrototype == null) {
						return null;
					}

					Group group = layoutSetPrototype.getGroup();

					return group.getExternalReferenceCode();
				}
			).build();
		}

		@Override
		public String getCategoryKey() {
			return "pages";
		}

		@Override
		public String getJspPath() {
			return "/site_settings/pages.jsp";
		}

		@Override
		public String getKey() {
			return "site-configuration-pages";
		}

		@Override
		public String getName(Locale locale) {
			if (FeatureFlagManagerUtil.isEnabled(
					CompanyThreadLocal.getCompanyId(), "LPD-82107")) {

				return _language.get(locale, "site-template-sync");
			}

			return _language.get(locale, "pages");
		}

		@Override
		public String getSaveMVCActionCommandName() {
			return "/site_admin/edit_pages";
		}

		@Override
		public ServletContext getServletContext() {
			return _servletContext;
		}

		@Override
		public void importProperties(
				Dictionary<String, Object> properties, Serializable scopePK)
			throws Exception {

			Object publicLayoutSetPrototypeLinkEnabled = properties.get(
				"publicLayoutSetPrototypeLinkEnabled");
			String publicLayoutSetPrototypeSiteExternalReferenceCode =
				GetterUtil.getString(
					properties.get(
						"publicLayoutSetPrototypeSiteExternalReferenceCode"));

			if ((publicLayoutSetPrototypeLinkEnabled == null) &&
				Validator.isNull(
					publicLayoutSetPrototypeSiteExternalReferenceCode)) {

				return;
			}

			Group group = _groupLocalService.getGroup((long)scopePK);

			if (group.hasStagingGroup() && !group.isStagedRemotely()) {
				group = group.getStagingGroup();
			}

			LayoutSet publicLayoutSet = group.getPublicLayoutSet();

			long publicLayoutSetPrototypeId =
				publicLayoutSet.getLayoutSetPrototypeId();

			if (Validator.isNotNull(
					publicLayoutSetPrototypeSiteExternalReferenceCode)) {

				Group layoutSetPrototypeGroup =
					_groupLocalService.getGroupByExternalReferenceCode(
						publicLayoutSetPrototypeSiteExternalReferenceCode,
						group.getCompanyId());

				publicLayoutSetPrototypeId =
					layoutSetPrototypeGroup.getClassPK();
			}

			LayoutSet privateLayoutSet = group.getPrivateLayoutSet();

			_sites.updateLayoutSetPrototypesLinks(
				group, false, publicLayoutSetPrototypeId,
				privateLayoutSet.getLayoutSetPrototypeId(),
				GetterUtil.getBoolean(
					publicLayoutSetPrototypeLinkEnabled,
					publicLayoutSet.isLayoutSetPrototypeLinkEnabled()),
				privateLayoutSet.isLayoutSetPrototypeLinkEnabled());
		}

		@Override
		public boolean isVisible(Group group) {
			if ((group != null) && group.isCompany()) {
				return false;
			}

			if ((group != null) && !group.isPrivateLayoutsEnabled()) {
				return true;
			}

			ServiceContext serviceContext =
				ServiceContextThreadLocal.getServiceContext();

			ThemeDisplay themeDisplay = serviceContext.getThemeDisplay();

			HttpServletRequest httpServletRequest = themeDisplay.getRequest();

			long layoutSetPrototypeId = ParamUtil.getLong(
				httpServletRequest, "layoutSetPrototypeId");

			if (layoutSetPrototypeId > 0) {
				LayoutSetPrototype layoutSetPrototype =
					_layoutSetPrototypeLocalService.fetchLayoutSetPrototype(
						layoutSetPrototypeId);

				if (layoutSetPrototype != null) {
					return true;
				}
			}

			return false;
		}

		private LayoutSetPrototype _fetchLayoutSetPrototype(
			LayoutSet layoutSet) {

			if ((layoutSet == null) ||
				Validator.isNull(layoutSet.getLayoutSetPrototypeUuid())) {

				return null;
			}

			return _layoutSetPrototypeLocalService.
				fetchLayoutSetPrototypeByUuidAndCompanyId(
					layoutSet.getLayoutSetPrototypeUuid(),
					layoutSet.getCompanyId());
		}

	}

}