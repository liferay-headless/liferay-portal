/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.upload.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.exportimport.upload.ImportUploadEntry;
import com.liferay.exportimport.upload.ImportUploadException;
import com.liferay.exportimport.upload.ImportUploadManager;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.io.ByteArrayInputStream;
import java.io.File;

import java.util.Arrays;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Jaime León
 */
@RunWith(Arquillian.class)
public class ImportUploadManagerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();
	}

	@Test
	public void testAddImportUpload() throws Exception {
		String fileName = RandomTestUtil.randomString() + ".lar";

		ImportUploadEntry importUploadEntry =
			_importUploadManager.addImportUpload(
				_group.getGroupId(), TestPropsValues.getUserId(), fileName,
				RandomTestUtil.randomString(), 10, 0, null);

		Assert.assertEquals(fileName, importUploadEntry.getFileName());
		Assert.assertEquals(10, importUploadEntry.getSize());
		Assert.assertEquals(1, importUploadEntry.getTotalChunksCount());
		Assert.assertEquals(0, importUploadEntry.getUploadedChunksCount());

		ImportUploadEntry fetchedImportUploadEntry =
			_importUploadManager.fetchImportUpload(
				_group.getGroupId(), TestPropsValues.getUserId(),
				importUploadEntry.getImportUploadId());

		Assert.assertEquals(
			importUploadEntry.getChunkSize(),
			fetchedImportUploadEntry.getChunkSize());
		Assert.assertEquals(fileName, fetchedImportUploadEntry.getFileName());
	}

	@Test
	public void testDeleteImportUpload() throws Exception {
		ImportUploadEntry importUploadEntry = _addImportUpload(new byte[0]);

		_importUploadManager.deleteImportUpload(
			_group.getGroupId(), TestPropsValues.getUserId(),
			importUploadEntry.getImportUploadId());

		Assert.assertNull(
			_importUploadManager.fetchImportUpload(
				_group.getGroupId(), TestPropsValues.getUserId(),
				importUploadEntry.getImportUploadId()));
	}

	@Test
	public void testFetchImportUploadWithInvalidId() throws Exception {
		try {
			_importUploadManager.fetchImportUpload(
				_group.getGroupId(), TestPropsValues.getUserId(),
				"../" + RandomTestUtil.randomString());

			Assert.fail();
		}
		catch (ImportUploadException importUploadException) {
			Assert.assertNotNull(importUploadException);
		}
	}

	@Test
	public void testPutChunk() throws Exception {
		ImportUploadEntry importUploadEntry =
			_importUploadManager.addImportUpload(
				_group.getGroupId(), TestPropsValues.getUserId(),
				RandomTestUtil.randomString() + ".lar",
				RandomTestUtil.randomString(), 0, 0, null);

		long chunkSize = importUploadEntry.getChunkSize();

		byte[] bytes = _getRandomBytes((int)chunkSize + 3);

		importUploadEntry = _importUploadManager.addImportUpload(
			_group.getGroupId(), TestPropsValues.getUserId(),
			RandomTestUtil.randomString() + ".lar",
			RandomTestUtil.randomString(), bytes.length, 0, null);

		Assert.assertEquals(2, importUploadEntry.getTotalChunksCount());

		importUploadEntry = _importUploadManager.putChunk(
			_group.getGroupId(), TestPropsValues.getUserId(), importUploadEntry,
			1,
			new ByteArrayInputStream(
				Arrays.copyOfRange(bytes, (int)chunkSize, bytes.length)));

		Assert.assertFalse(importUploadEntry.isComplete());
		Assert.assertEquals(0, importUploadEntry.getUploadedChunksCount());

		importUploadEntry = _importUploadManager.putChunk(
			_group.getGroupId(), TestPropsValues.getUserId(), importUploadEntry,
			0,
			new ByteArrayInputStream(
				Arrays.copyOfRange(bytes, 0, (int)chunkSize)));

		Assert.assertTrue(importUploadEntry.isComplete());
		Assert.assertEquals(2, importUploadEntry.getUploadedChunksCount());

		importUploadEntry = _importUploadManager.putChunk(
			_group.getGroupId(), TestPropsValues.getUserId(), importUploadEntry,
			0,
			new ByteArrayInputStream(
				Arrays.copyOfRange(bytes, 0, (int)chunkSize)));

		Assert.assertTrue(importUploadEntry.isComplete());

		File file = FileUtil.createTempFile();

		_importUploadManager.writeTo(
			_group.getGroupId(), TestPropsValues.getUserId(),
			importUploadEntry.getImportUploadId(), file,
			bytesWritten -> {
			});

		Assert.assertArrayEquals(bytes, FileUtil.getBytes(file));

		FileUtil.delete(file);
	}

	@Test
	public void testPutChunkWithWrongSize() throws Exception {
		ImportUploadEntry importUploadEntry =
			_importUploadManager.addImportUpload(
				_group.getGroupId(), TestPropsValues.getUserId(),
				RandomTestUtil.randomString() + ".lar",
				RandomTestUtil.randomString(), 10, 0, null);

		try {
			_importUploadManager.putChunk(
				_group.getGroupId(), TestPropsValues.getUserId(),
				importUploadEntry, 0,
				new ByteArrayInputStream(_getRandomBytes(11)));

			Assert.fail();
		}
		catch (ImportUploadException importUploadException) {
			Assert.assertNotNull(importUploadException);
		}

		importUploadEntry = _importUploadManager.fetchImportUpload(
			_group.getGroupId(), TestPropsValues.getUserId(),
			importUploadEntry.getImportUploadId());

		Assert.assertEquals(0, importUploadEntry.getUploadedChunksCount());
	}

	private ImportUploadEntry _addImportUpload(byte[] bytes) throws Exception {
		ImportUploadEntry importUploadEntry =
			_importUploadManager.addImportUpload(
				_group.getGroupId(), TestPropsValues.getUserId(),
				RandomTestUtil.randomString() + ".lar",
				RandomTestUtil.randomString(), bytes.length, 0, null);

		return _importUploadManager.putChunk(
			_group.getGroupId(), TestPropsValues.getUserId(), importUploadEntry,
			0, new ByteArrayInputStream(bytes));
	}

	private byte[] _getRandomBytes(int length) {
		byte[] bytes = new byte[length];

		Random random = new Random();

		random.nextBytes(bytes);

		return bytes;
	}

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private ImportUploadManager _importUploadManager;

}