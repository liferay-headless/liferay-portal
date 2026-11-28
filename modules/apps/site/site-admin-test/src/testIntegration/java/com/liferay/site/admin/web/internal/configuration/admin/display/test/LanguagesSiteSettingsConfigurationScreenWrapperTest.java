/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.admin.web.internal.configuration.admin.display.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.configuration.admin.display.ConfigurationScreen;
import com.liferay.portal.kernel.exception.LocaleException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Dictionary;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Thiago Buarque
 */
@RunWith(Arquillian.class)
public class LanguagesSiteSettingsConfigurationScreenWrapperTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testExportImportProperties() {
		try {
			Group group = GroupTestUtil.addGroup();

			String locales = StringUtil.merge(
				LocaleUtil.toLanguageIds(LanguageUtil.getAvailableLocales()));

			Dictionary<String, Object> properties =
				HashMapDictionaryBuilder.<String, Object>put(
					GroupConstants.TYPE_SETTINGS_KEY_INHERIT_LOCALES, "true"
				).put(
					PropsKeys.LOCALES, locales
				).put(
					"languageId", "en_US"
				).build();

			_configurationScreen.importProperties(
				properties, group.getGroupId());

			properties = _configurationScreen.exportProperties(
				group.getGroupId());

			Assert.assertEquals(
				"true",
				properties.get(
					GroupConstants.TYPE_SETTINGS_KEY_INHERIT_LOCALES));
			Assert.assertEquals(locales, properties.get(PropsKeys.LOCALES));
			Assert.assertEquals("en_US", properties.get("languageId"));

			properties.put("languageId", "invalid_language");

			_configurationScreen.importProperties(
				properties, group.getGroupId());
		}
		catch (Exception exception) {
			Assert.assertTrue(exception instanceof LocaleException);

			Set<Locale> availableLocales = LanguageUtil.getAvailableLocales();

			LocaleException localeException = (LocaleException)exception;

			Set<Locale> sourceAvailableLocales =
				(Set<Locale>)localeException.getSourceAvailableLocales();

			ArrayUtil.equalsIgnoreCase(
				ArrayUtil.toStringArray(availableLocales.toArray()),
				ArrayUtil.toStringArray(sourceAvailableLocales.toArray()));
		}
	}

	@Inject(
		filter = "component.name=com.liferay.site.admin.web.internal.configuration.admin.display.LanguagesSiteSettingsConfigurationScreenWrapper"
	)
	private ConfigurationScreen _configurationScreen;

}