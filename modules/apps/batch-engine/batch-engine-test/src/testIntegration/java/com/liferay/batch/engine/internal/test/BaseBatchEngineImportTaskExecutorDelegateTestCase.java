/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.test;

import com.liferay.batch.engine.BaseBatchEngineTaskItemDelegate;
import com.liferay.batch.engine.BatchEngineImportTaskExecutor;
import com.liferay.batch.engine.BatchEngineTaskExecuteStatus;
import com.liferay.batch.engine.BatchEngineTaskOperation;
import com.liferay.batch.engine.constants.BatchEngineImportTaskConstants;
import com.liferay.batch.engine.model.BatchEngineImportTask;
import com.liferay.batch.engine.pagination.Page;
import com.liferay.batch.engine.pagination.Pagination;
import com.liferay.batch.engine.service.BatchEngineImportTaskLocalService;
import com.liferay.batch.engine.thread.local.BatchEngineThreadLocal;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.TransactionConfig;
import com.liferay.portal.kernel.transaction.TransactionInvokerUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import java.nio.charset.StandardCharsets;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;

/**
 * @author Alejandro Tardín
 */
public abstract class BaseBatchEngineImportTaskExecutorDelegateTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	public static class TestBatchEngineTaskItemDelegate
		extends BaseBatchEngineTaskItemDelegate<BlogPosting> {

		@Override
		public BlogPosting createItem(
			BlogPosting blogPosting, Map<String, Serializable> parameters) {

			_batchImportInProcess =
				BatchEngineThreadLocal.isBatchImportInProcess();
			_batchMode = SearchContext.isBatchMode();
			_createItemInvoked = true;
			_transactionActive = _isTransactionActive();

			return blogPosting;
		}

		@Override
		public Class<BlogPosting> getItemClass() {
			return BlogPosting.class;
		}

		public boolean isBatchImportInProcess() {
			return _batchImportInProcess;
		}

		public boolean isBatchMode() {
			return _batchMode;
		}

		public boolean isCreateItemInvoked() {
			return _createItemInvoked;
		}

		public boolean isTransactionActive() {
			return _transactionActive;
		}

		@Override
		public Page<BlogPosting> read(
			Filter filter, Pagination pagination, Sort[] sorts,
			Map<String, Serializable> parameters, String search) {

			return Page.of(Collections.emptyList(), pagination, 0);
		}

		private boolean _isTransactionActive() {

			// Propagation.MANDATORY fails when no transaction is active, which
			// is the only way to observe from inside an item whether the
			// executor wrapped it in one.

			try {
				TransactionInvokerUtil.invoke(
					_mandatoryTransactionConfig, () -> null);

				return true;
			}
			catch (Throwable throwable) {
				return false;
			}
		}

		private static final TransactionConfig _mandatoryTransactionConfig =
			TransactionConfig.Factory.create(
				Propagation.MANDATORY, new Class<?>[] {Exception.class});

		private boolean _batchImportInProcess;
		private boolean _batchMode;
		private boolean _createItemInvoked;
		private boolean _transactionActive;

	}

	protected void execute(
			TestBatchEngineTaskItemDelegate testBatchEngineTaskItemDelegate)
		throws Exception {

		batchEngineImportTask =
			batchEngineImportTaskLocalService.addBatchEngineImportTask(
				null, TestPropsValues.getCompanyId(),
				TestPropsValues.getUserId(), _BATCH_SIZE, null,
				BlogPosting.class.getName(),
				_compressContent(
					JSONUtil.putAll(
						JSONUtil.put("headline", RandomTestUtil.randomString())
					).toString()),
				"JSON", BatchEngineTaskExecuteStatus.INITIAL.name(), null,
				BatchEngineImportTaskConstants.IMPORT_STRATEGY_ON_ERROR_FAIL,
				BatchEngineTaskOperation.CREATE.toString(), new HashMap<>(),
				null);

		batchEngineImportTaskExecutor.execute(
			batchEngineImportTask, testBatchEngineTaskItemDelegate, false);

		batchEngineImportTask =
			batchEngineImportTaskLocalService.getBatchEngineImportTask(
				batchEngineImportTask.getBatchEngineImportTaskId());

		Assert.assertEquals(
			BatchEngineTaskExecuteStatus.COMPLETED.toString(),
			batchEngineImportTask.getExecuteStatus());

		Assert.assertTrue(
			testBatchEngineTaskItemDelegate.isCreateItemInvoked());
	}

	@DeleteAfterTestRun
	protected BatchEngineImportTask batchEngineImportTask;

	@Inject
	protected BatchEngineImportTaskExecutor batchEngineImportTaskExecutor;

	@Inject
	protected BatchEngineImportTaskLocalService
		batchEngineImportTaskLocalService;

	private byte[] _compressContent(String content) throws Exception {
		try (ByteArrayOutputStream byteArrayOutputStream =
				new ByteArrayOutputStream()) {

			try (ZipOutputStream zipOutputStream = new ZipOutputStream(
					byteArrayOutputStream)) {

				zipOutputStream.putNextEntry(new ZipEntry("import.json"));

				byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

				zipOutputStream.write(bytes, 0, bytes.length);
			}

			return byteArrayOutputStream.toByteArray();
		}

	}

	private static final int _BATCH_SIZE = 10;

}