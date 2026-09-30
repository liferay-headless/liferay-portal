/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.taxonomy.internal.exportimport.data.handler.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.model.AssetTag;
import com.liferay.asset.kernel.service.AssetTagLocalService;
import com.liferay.exportimport.test.rule.ExportImportScopeClassTestRule;
import com.liferay.exportimport.test.util.exportimport.data.handler.BaseBatchEnginePortletDataHandlerTestCase;
import com.liferay.exportimport.vulcan.batch.engine.ExportImportVulcanBatchEngineTaskItemDelegate;
import com.liferay.exportimport.vulcan.batch.engine.ExportImportVulcanBatchEngineTaskItemDelegate.Scope;
import com.liferay.headless.admin.taxonomy.resource.v1_0.KeywordResource;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Date;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;

/**
 * @author Alejandro Tardín
 */
@RunWith(Arquillian.class)
public class KeywordBatchEnginePortletDataHandlerTest
	extends BaseBatchEnginePortletDataHandlerTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@ClassRule
	public static final ExportImportScopeClassTestRule
		exportImportScopeClassTestRule = new ExportImportScopeClassTestRule(
			Scope.SITE);

	@Override
	protected String addEntry(long groupId, Date modifiedDate, long userId)
		throws Exception {

		AssetTag assetTag = _assetTagLocalService.addTag(
			null, userId, groupId,
			StringUtil.toLowerCase(RandomTestUtil.randomString()),
			ServiceContextTestUtil.getServiceContext(groupId, userId));

		assetTag.setModifiedDate(modifiedDate);

		assetTag = _assetTagLocalService.updateAssetTag(assetTag);

		return assetTag.getExternalReferenceCode();
	}

	@Override
	protected void addStagedModels() throws Exception {
	}

	@Override
	protected void deleteEntry(String externalReferenceCode, long groupId)
		throws Exception {

		_assetTagLocalService.deleteTag(
			_fetchAssetTag(externalReferenceCode, groupId));
	}

	@Override
	protected Object fetchEntry(String externalReferenceCode, long groupId)
		throws Exception {

		return _fetchAssetTag(externalReferenceCode, groupId);
	}

	@Override
	protected long getCreatorUserId(String externalReferenceCode, long groupId)
		throws Exception {

		AssetTag assetTag = _fetchAssetTag(externalReferenceCode, groupId);

		return assetTag.getUserId();
	}

	@Override
	protected Object getEntryValue(String externalReferenceCode, long groupId)
		throws Exception {

		AssetTag assetTag = _fetchAssetTag(externalReferenceCode, groupId);

		return assetTag.getName();
	}

	@Override
	protected ExportImportScopeClassTestRule
		getExportImportScopeClassTestRule() {

		return exportImportScopeClassTestRule;
	}

	@Override
	protected ExportImportVulcanBatchEngineTaskItemDelegate<?>
		getExportImportVulcanBatchEngineTaskItemDelegate() {

		return (ExportImportVulcanBatchEngineTaskItemDelegate<?>)
			_keywordResource;
	}

	@Override
	protected long getPrimaryKey(String externalReferenceCode, long groupId)
		throws Exception {

		AssetTag assetTag = _fetchAssetTag(externalReferenceCode, groupId);

		return assetTag.getTagId();
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
	protected void updateEntry(String externalReferenceCode, long groupId)
		throws Exception {

		AssetTag assetTag = _fetchAssetTag(externalReferenceCode, groupId);

		_assetTagLocalService.updateTag(
			assetTag.getExternalReferenceCode(), assetTag.getUserId(),
			assetTag.getTagId(),
			StringUtil.toLowerCase(RandomTestUtil.randomString()),
			ServiceContextTestUtil.getServiceContext(
				groupId, assetTag.getUserId()));
	}

	private AssetTag _fetchAssetTag(String externalReferenceCode, long groupId)
		throws Exception {

		return _assetTagLocalService.fetchAssetTagByExternalReferenceCode(
			externalReferenceCode, groupId);
	}

	@Inject
	private AssetTagLocalService _assetTagLocalService;

	@Inject(
		filter = "export.import.vulcan.batch.engine.task.item.delegate=true"
	)
	private KeywordResource _keywordResource;

}