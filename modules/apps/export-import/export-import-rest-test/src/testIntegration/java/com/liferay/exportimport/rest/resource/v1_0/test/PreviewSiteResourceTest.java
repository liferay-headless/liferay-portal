/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.rest.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.exportimport.rest.client.dto.v1_0.PreviewSite;
import com.liferay.exportimport.rest.client.pagination.Page;
import com.liferay.exportimport.rest.client.pagination.Pagination;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Petteri Karttunen
 */
@FeatureFlags(
	featureFlags = {
		@FeatureFlag(value = "LPD-57655"), @FeatureFlag("LPD-85946")
	}
)
@RunWith(Arquillian.class)
public class PreviewSiteResourceTest extends BasePreviewSiteResourceTestCase {

	@Override
	@Test
	public void testGetExportPreviewPreviewSitesPage() throws Exception {
		super.testGetExportPreviewPreviewSitesPage();

		_testGetExportPreviewPreviewSitesPage();
		_testGetExportPreviewPreviewSitesPageWithInactiveSite();
		_testGetExportPreviewPreviewSitesPageWithSearch();
		_testGetExportPreviewPreviewSitesPageWithSpace();
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"externalReferenceCode"};
	}

	@Override
	protected PreviewSite testGetExportPreviewPreviewSitesPage_addPreviewSite(
			PreviewSite previewSite)
		throws Exception {

		Group group = GroupTestUtil.addGroup();

		previewSite.setExternalReferenceCode(group.getExternalReferenceCode());

		return previewSite;
	}

	private PreviewSite _getExportPreviewSite(String externalReferenceCode)
		throws Exception {

		Page<PreviewSite> page =
			previewSiteResource.getExportPreviewPreviewSitesPage(
				null, null, null);

		page = previewSiteResource.getExportPreviewPreviewSitesPage(
			null, Pagination.of(1, (int)page.getTotalCount()), null);

		for (PreviewSite previewSite : page.getItems()) {
			if (externalReferenceCode.equals(
					previewSite.getExternalReferenceCode())) {

				return previewSite;
			}
		}

		return null;
	}

	private void _testGetExportPreviewPreviewSitesPage() throws Exception {
		Group group = GroupTestUtil.addGroup();

		Group childGroup = GroupTestUtil.addGroup(group.getGroupId());

		PreviewSite previewSite = _getExportPreviewSite(
			group.getExternalReferenceCode());

		Assert.assertEquals(
			Integer.valueOf(1), previewSite.getChildSitesCount());
		Assert.assertEquals(
			group.getDescriptiveName(), previewSite.getDescriptiveName());

		PreviewSite childPreviewSite = _getExportPreviewSite(
			childGroup.getExternalReferenceCode());

		Assert.assertEquals(
			previewSite.getPath() + " / " + childGroup.getDescriptiveName(),
			childPreviewSite.getPath());
	}

	private void _testGetExportPreviewPreviewSitesPageWithInactiveSite()
		throws Exception {

		Group group = GroupTestUtil.addGroup();

		group.setActive(false);

		group = _groupLocalService.updateGroup(group);

		Assert.assertNull(
			_getExportPreviewSite(group.getExternalReferenceCode()));
	}

	private void _testGetExportPreviewPreviewSitesPageWithSearch()
		throws Exception {

		Group group = GroupTestUtil.addGroup();

		GroupTestUtil.addGroup();

		Page<PreviewSite> page =
			previewSiteResource.getExportPreviewPreviewSitesPage(
				group.getDescriptiveName(), Pagination.of(1, 10), null);

		Assert.assertEquals(1, page.getTotalCount());

		List<PreviewSite> previewSites = (List<PreviewSite>)page.getItems();

		PreviewSite previewSite = previewSites.get(0);

		Assert.assertEquals(
			group.getExternalReferenceCode(),
			previewSite.getExternalReferenceCode());
	}

	private void _testGetExportPreviewPreviewSitesPageWithSpace()
		throws Exception {

		DepotEntry depotEntry = _depotEntryLocalService.addDepotEntry(
			HashMapBuilder.put(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()
			).build(),
			HashMapBuilder.put(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()
			).build(),
			DepotConstants.TYPE_SPACE,
			ServiceContextTestUtil.getServiceContext());

		_depotGroup = depotEntry.getGroup();

		Assert.assertNull(
			_getExportPreviewSite(_depotGroup.getExternalReferenceCode()));
	}

	@Inject
	private DepotEntryLocalService _depotEntryLocalService;

	@DeleteAfterTestRun
	private Group _depotGroup;

	@Inject
	private GroupLocalService _groupLocalService;

}