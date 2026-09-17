/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.test.rule.DataGuard;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alejandro Tardín
 */
@DataGuard(scope = DataGuard.Scope.METHOD)
@RunWith(Arquillian.class)
public class BatchEngineImportTaskExecutorBatchModeTest
	extends BaseBatchEngineImportTaskExecutorDelegateTestCase {

	@Test
	public void testBatchModeEnabledByDefault() throws Exception {
		TestBatchEngineTaskItemDelegate testBatchEngineTaskItemDelegate =
			new TestBatchEngineTaskItemDelegate();

		Assert.assertTrue(testBatchEngineTaskItemDelegate.isBatchModeEnabled());

		execute(testBatchEngineTaskItemDelegate);

		Assert.assertTrue(
			testBatchEngineTaskItemDelegate.isBatchImportInProcess());
		Assert.assertTrue(testBatchEngineTaskItemDelegate.isBatchMode());
	}

	@Test
	public void testBatchModeSkippedWhenDelegateOptsOut() throws Exception {
		TestBatchEngineTaskItemDelegate testBatchEngineTaskItemDelegate =
			new BatchModeDisabledTestBatchEngineTaskItemDelegate();

		execute(testBatchEngineTaskItemDelegate);

		Assert.assertFalse(
			testBatchEngineTaskItemDelegate.isBatchImportInProcess());
		Assert.assertFalse(testBatchEngineTaskItemDelegate.isBatchMode());
	}

	private static class BatchModeDisabledTestBatchEngineTaskItemDelegate
		extends TestBatchEngineTaskItemDelegate {

		@Override
		public boolean isBatchModeEnabled() {
			return false;
		}

	}

}