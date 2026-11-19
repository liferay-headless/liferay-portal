/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.web.internal.servlet.taglib.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.launch.LaunchPreviewThreadLocal;
import com.liferay.portal.kernel.model.LayoutSet;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.LayoutSetLocalService;
import com.liferay.portal.kernel.servlet.taglib.DynamicInclude;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Jose Luis Navarro
 */
@FeatureFlag("LPD-72278")
@RunWith(Arquillian.class)
public class LaunchPreviewTopbarDynamicIncludeTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testInclude() throws Exception {
		long launchSetId = RandomTestUtil.randomLong();

		// Not previewing

		Assert.assertEquals("", _include(launchSetId));

		// Previewing

		try (SafeCloseable safeCloseable =
				LaunchPreviewThreadLocal.setLaunchSetIdWithSafeCloseable(
					launchSetId)) {

			String content = _include(launchSetId);

			Assert.assertTrue(content.contains("LaunchPreviewTopbar.css"));
		}
	}

	private String _include(long launchSetId) throws Exception {
		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setParameter("p_l_mode", Constants.PREVIEW);
		mockHttpServletRequest.setParameter(
			"previewLaunchSetId", String.valueOf(launchSetId));

		LayoutSet layoutSet = _layoutSetLocalService.getLayoutSet(
			TestPropsValues.getGroupId(), false);

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			_companyLocalService.getCompany(TestPropsValues.getCompanyId()));
		themeDisplay.setLookAndFeel(layoutSet.getTheme(), null);
		themeDisplay.setSignedIn(true);
		themeDisplay.setUser(TestPropsValues.getUser());

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		MockHttpServletResponse mockHttpServletResponse =
			new MockHttpServletResponse();

		_dynamicInclude.include(
			mockHttpServletRequest, mockHttpServletResponse,
			"/html/common/themes/top_head.jsp#post");

		return mockHttpServletResponse.getContentAsString();
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject(
		filter = "component.name=com.liferay.launch.web.internal.servlet.taglib.LaunchPreviewTopbarDynamicInclude"
	)
	private DynamicInclude _dynamicInclude;

	@Inject
	private LayoutSetLocalService _layoutSetLocalService;

}