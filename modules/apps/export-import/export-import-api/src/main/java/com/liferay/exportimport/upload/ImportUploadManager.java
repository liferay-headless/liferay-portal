/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.upload;

import com.liferay.portal.kernel.exception.PortalException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.function.LongConsumer;

/**
 * @author Jaime León
 */
public interface ImportUploadManager {

	public ImportUploadEntry addImportUpload(
			long groupId, long userId, String fileName, String contentType,
			long size, long plid, String portletId)
		throws PortalException;

	public void deleteImportUpload(
			long groupId, long userId, String importUploadId)
		throws PortalException;

	public ImportUploadEntry fetchImportUpload(
			long groupId, long userId, String importUploadId)
		throws IOException, PortalException;

	public ImportUploadEntry putChunk(
			long groupId, long userId, ImportUploadEntry importUploadEntry,
			int chunkIndex, InputStream inputStream)
		throws IOException, PortalException;

	public void writeTo(
			long groupId, long userId, String importUploadId, File file)
		throws IOException, PortalException;

	public void writeTo(
			long groupId, long userId, String importUploadId, File file,
			LongConsumer bytesWrittenConsumer)
		throws IOException, PortalException;

}