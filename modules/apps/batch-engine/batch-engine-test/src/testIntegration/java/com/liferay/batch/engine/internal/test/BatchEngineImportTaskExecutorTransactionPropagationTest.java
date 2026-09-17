/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.kernel.transaction.Propagation;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alejandro Tardín
 */
@DataGuard(scope = DataGuard.Scope.METHOD)
@RunWith(Arquillian.class)
public class BatchEngineImportTaskExecutorTransactionPropagationTest
	extends BaseBatchEngineImportTaskExecutorDelegateTestCase {

	@Test
	public void testTransactionPropagationIsNestedByDefault() throws Exception {
		TestBatchEngineTaskItemDelegate testBatchEngineTaskItemDelegate =
			new TestBatchEngineTaskItemDelegate();

		Assert.assertEquals(
			Propagation.NESTED,
			testBatchEngineTaskItemDelegate.getTransactionPropagation());

		execute(testBatchEngineTaskItemDelegate);

		Assert.assertTrue(
			testBatchEngineTaskItemDelegate.isTransactionActive());
	}

	@Test
	public void testTransactionSuspendedWhenDelegateOptsOut() throws Exception {
		TestBatchEngineTaskItemDelegate testBatchEngineTaskItemDelegate =
			new TransactionNotSupportedTestBatchEngineTaskItemDelegate();

		execute(testBatchEngineTaskItemDelegate);

		Assert.assertFalse(
			testBatchEngineTaskItemDelegate.isTransactionActive());
	}

	private static class TransactionNotSupportedTestBatchEngineTaskItemDelegate
		extends TestBatchEngineTaskItemDelegate {

		@Override
		public Propagation getTransactionPropagation() {
			return Propagation.NOT_SUPPORTED;
		}

	}

}