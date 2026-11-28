/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.configuration.internal.exportimport.data.handler.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.configuration.admin.display.ConfigurationScreen;
import com.liferay.exportimport.test.util.exportimport.data.handler.BaseSiteBatchEnginePortletDataHandlerTestCase;
import com.liferay.headless.admin.configuration.dto.v1_0.SiteConfiguration;
import com.liferay.headless.admin.configuration.test.configuration.screen.TestGroupConfigurationScreen;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Date;
import java.util.Dictionary;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alejandro Tardín
 */
@FeatureFlags(
	featureFlags = {@FeatureFlag("LPD-65399"), @FeatureFlag("LPS-155284")}
)
@RunWith(Arquillian.class)
public class SiteConfigurationSiteBatchEnginePortletDataHandlerTest
	extends BaseSiteBatchEnginePortletDataHandlerTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Ignore
	@Override
	@Test
	public void testExportImportKeepCreatorData() throws Exception {
	}

	@Ignore
	@Override
	@Test
	public void testExportImportOverrideCreatorData() throws Exception {
	}

	@Ignore
	@Override
	@Test
	public void testExportImportWithDateRange() throws Exception {
	}

	@Override
	protected void addEntry(String externalReferenceCode, long groupId)
		throws Exception {

		Dictionary<String, Object> properties = _getProperties(groupId);

		properties.put(externalReferenceCode, RandomTestUtil.randomString());

		_configurationScreen.importProperties(properties, groupId);
	}

	@Override
	protected void deleteEntry(String externalReferenceCode, long groupId)
		throws Exception {

		Dictionary<String, Object> properties = _getProperties(groupId);

		properties.remove(externalReferenceCode);

		_configurationScreen.importProperties(properties, groupId);
	}

	@Override
	protected long getCreatorUserId(
		String externalReferenceCode, long groupId) {

		throw new UnsupportedOperationException();
	}

	@Override
	protected String getEntityClassName() {
		return SiteConfiguration.class.getName();
	}

	@Override
	protected long getPrimaryKey(String externalReferenceCode, long groupId) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean hasEntry(String externalReferenceCode, long groupId) {
		Dictionary<String, Object> properties = _getProperties(groupId);

		if (properties.get(externalReferenceCode) == null) {
			return false;
		}

		return true;
	}

	@Override
	protected boolean supportsComments() {
		return false;
	}

	@Override
	protected boolean supportsEmptyEntries() {
		return false;
	}

	@Override
	protected boolean supportsPermissions() {
		return false;
	}

	@Override
	protected void updateCreatorUserId(
		String externalReferenceCode, long groupId, long userId) {

		throw new UnsupportedOperationException();
	}

	@Override
	protected void updateEntry(String externalReferenceCode, long groupId) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void updateModifiedDate(
		String externalReferenceCode, long groupId, Date modifiedDate) {

		throw new UnsupportedOperationException();
	}

	private Dictionary<String, Object> _getProperties(long groupId) {
		TestGroupConfigurationScreen testGroupConfigurationScreen =
			(TestGroupConfigurationScreen)_configurationScreen;

		Dictionary<String, Object> properties =
			testGroupConfigurationScreen.exportProperties(groupId);

		if (properties == null) {
			return new HashMapDictionary<>();
		}

		return properties;
	}

	@Inject(
		filter = "component.name=com.liferay.headless.admin.configuration.test.configuration.screen.TestGroupConfigurationScreen"
	)
	private ConfigurationScreen _configurationScreen;

}