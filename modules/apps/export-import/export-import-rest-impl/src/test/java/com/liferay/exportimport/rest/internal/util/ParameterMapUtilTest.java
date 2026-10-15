/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.rest.internal.util;

import com.liferay.exportimport.kernel.lar.PortletDataHandlerKeys;
import com.liferay.exportimport.rest.dto.v1_0.ExportProcessRequest;
import com.liferay.exportimport.rest.dto.v1_0.ImportProcessRequest;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.ws.rs.BadRequestException;

import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Petteri Karttunen
 */
public class ParameterMapUtilTest {

	@ClassRule
	@Rule
	public static LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@FeatureFlags(featureFlags = @FeatureFlag("LPD-41307"))
	@Test
	public void testToParameterMapWhenExportAssetLibraryAndSiteExternalReferenceCodesAreSet() {
		ExportProcessRequest exportProcessRequest = new ExportProcessRequest();

		exportProcessRequest.setAssetLibraryExternalReferenceCodes(
			new String[] {_EXTERNAL_REFERENCE_CODE_1, StringPool.BLANK});
		exportProcessRequest.setSiteExternalReferenceCodes(
			new String[] {
				_EXTERNAL_REFERENCE_CODE_2, _EXTERNAL_REFERENCE_CODE_1
			});

		Map<String, String[]> parameterMap = ParameterMapUtil.toParameterMap(
			exportProcessRequest, false);

		Assert.assertArrayEquals(
			new String[] {
				_EXTERNAL_REFERENCE_CODE_1, _EXTERNAL_REFERENCE_CODE_2
			},
			parameterMap.get(
				PortletDataHandlerKeys.GROUP_EXTERNAL_REFERENCE_CODES));
	}

	@FeatureFlags(featureFlags = @FeatureFlag("LPD-41307"))
	@Test
	public void testToParameterMapWhenExportAssetLibraryExternalReferenceCodesAreSet() {
		ExportProcessRequest exportProcessRequest = new ExportProcessRequest();

		exportProcessRequest.setAssetLibraryExternalReferenceCodes(
			new String[] {_EXTERNAL_REFERENCE_CODE_1});

		Map<String, String[]> parameterMap = ParameterMapUtil.toParameterMap(
			exportProcessRequest, false);

		Assert.assertArrayEquals(
			new String[] {_EXTERNAL_REFERENCE_CODE_1},
			parameterMap.get(
				PortletDataHandlerKeys.GROUP_EXTERNAL_REFERENCE_CODES));
	}

	@Test(expected = BadRequestException.class)
	public void testToParameterMapWhenExportAssetLibraryExternalReferenceCodesAreSetWithoutFeatureFlag() {
		ExportProcessRequest exportProcessRequest = new ExportProcessRequest();

		exportProcessRequest.setAssetLibraryExternalReferenceCodes(
			new String[] {_EXTERNAL_REFERENCE_CODE_1});

		ParameterMapUtil.toParameterMap(exportProcessRequest, false);
	}

	@Test
	public void testToParameterMapWhenExportGroupExternalReferenceCodeIsNull() {
		_assertExportGroupExternalReferenceCodes(
			new String[] {_EXTERNAL_REFERENCE_CODE_1, null},
			new String[] {_EXTERNAL_REFERENCE_CODE_1});
	}

	@Test
	public void testToParameterMapWhenExportGroupExternalReferenceCodesAreBlank() {
		ExportProcessRequest exportProcessRequest = new ExportProcessRequest();

		exportProcessRequest.setSiteExternalReferenceCodes(
			new String[] {StringPool.BLANK, StringPool.THREE_SPACES});

		Map<String, String[]> parameterMap = ParameterMapUtil.toParameterMap(
			exportProcessRequest, false);

		Assert.assertFalse(
			parameterMap.containsKey(
				PortletDataHandlerKeys.GROUP_EXTERNAL_REFERENCE_CODES));
	}

	@Test
	public void testToParameterMapWhenExportGroupExternalReferenceCodesAreDuplicated() {
		_assertExportGroupExternalReferenceCodes(
			new String[] {
				_EXTERNAL_REFERENCE_CODE_1, _EXTERNAL_REFERENCE_CODE_2,
				_EXTERNAL_REFERENCE_CODE_1
			},
			new String[] {
				_EXTERNAL_REFERENCE_CODE_1, _EXTERNAL_REFERENCE_CODE_2
			});
	}

	@Test
	public void testToParameterMapWhenExportGroupExternalReferenceCodesAreEmpty() {
		ExportProcessRequest exportProcessRequest = new ExportProcessRequest();

		exportProcessRequest.setSiteExternalReferenceCodes(new String[0]);

		Map<String, String[]> parameterMap = ParameterMapUtil.toParameterMap(
			exportProcessRequest, false);

		Assert.assertFalse(
			parameterMap.containsKey(
				PortletDataHandlerKeys.GROUP_EXTERNAL_REFERENCE_CODES));
	}

	@Test
	public void testToParameterMapWhenExportGroupExternalReferenceCodesAreNull() {
		Map<String, String[]> parameterMap = ParameterMapUtil.toParameterMap(
			new ExportProcessRequest(), false);

		Assert.assertFalse(
			parameterMap.containsKey(
				PortletDataHandlerKeys.GROUP_EXTERNAL_REFERENCE_CODES));
	}

	@Test
	public void testToParameterMapWhenExportGroupExternalReferenceCodesAreSet() {
		_assertExportGroupExternalReferenceCodes(
			new String[] {
				_EXTERNAL_REFERENCE_CODE_1, _EXTERNAL_REFERENCE_CODE_2
			},
			new String[] {
				_EXTERNAL_REFERENCE_CODE_1, _EXTERNAL_REFERENCE_CODE_2
			});
	}

	@FeatureFlags(featureFlags = @FeatureFlag("LPD-41307"))
	@Test
	public void testToParameterMapWhenImportAssetLibraryAndSiteExternalReferenceCodesAreSet() {
		ImportProcessRequest importProcessRequest = new ImportProcessRequest();

		importProcessRequest.setAssetLibraryExternalReferenceCodes(
			new String[] {_EXTERNAL_REFERENCE_CODE_1});
		importProcessRequest.setSiteExternalReferenceCodes(
			new String[] {_EXTERNAL_REFERENCE_CODE_2});

		Map<String, String[]> parameterMap = ParameterMapUtil.toParameterMap(
			importProcessRequest, false);

		Assert.assertArrayEquals(
			new String[] {
				_EXTERNAL_REFERENCE_CODE_1, _EXTERNAL_REFERENCE_CODE_2
			},
			parameterMap.get(
				PortletDataHandlerKeys.GROUP_EXTERNAL_REFERENCE_CODES));
	}

	@Test(expected = BadRequestException.class)
	public void testToParameterMapWhenImportAssetLibraryExternalReferenceCodesAreSetWithoutFeatureFlag() {
		ImportProcessRequest importProcessRequest = new ImportProcessRequest();

		importProcessRequest.setAssetLibraryExternalReferenceCodes(
			new String[] {_EXTERNAL_REFERENCE_CODE_1});

		ParameterMapUtil.toParameterMap(importProcessRequest, false);
	}

	@Test
	public void testToParameterMapWhenImportGroupExternalReferenceCodesAreEmpty() {
		ImportProcessRequest importProcessRequest = new ImportProcessRequest();

		importProcessRequest.setSiteExternalReferenceCodes(new String[0]);

		Map<String, String[]> parameterMap = ParameterMapUtil.toParameterMap(
			importProcessRequest, false);

		Assert.assertFalse(
			parameterMap.containsKey(
				PortletDataHandlerKeys.GROUP_EXTERNAL_REFERENCE_CODES));
	}

	@Test
	public void testToParameterMapWhenImportGroupExternalReferenceCodesAreSet() {
		ImportProcessRequest importProcessRequest = new ImportProcessRequest();

		importProcessRequest.setSiteExternalReferenceCodes(
			new String[] {_EXTERNAL_REFERENCE_CODE_1});

		Map<String, String[]> parameterMap = ParameterMapUtil.toParameterMap(
			importProcessRequest, false);

		Assert.assertArrayEquals(
			new String[] {_EXTERNAL_REFERENCE_CODE_1},
			parameterMap.get(
				PortletDataHandlerKeys.GROUP_EXTERNAL_REFERENCE_CODES));
	}

	private void _assertExportGroupExternalReferenceCodes(
		String[] externalReferenceCodes,
		String[] expectedExternalReferenceCodes) {

		ExportProcessRequest exportProcessRequest = new ExportProcessRequest();

		exportProcessRequest.setSiteExternalReferenceCodes(
			externalReferenceCodes);

		Map<String, String[]> parameterMap = ParameterMapUtil.toParameterMap(
			exportProcessRequest, false);

		Assert.assertArrayEquals(
			expectedExternalReferenceCodes,
			parameterMap.get(
				PortletDataHandlerKeys.GROUP_EXTERNAL_REFERENCE_CODES));
	}

	private static final String _EXTERNAL_REFERENCE_CODE_1 =
		RandomTestUtil.randomString();

	private static final String _EXTERNAL_REFERENCE_CODE_2 =
		RandomTestUtil.randomString();

}