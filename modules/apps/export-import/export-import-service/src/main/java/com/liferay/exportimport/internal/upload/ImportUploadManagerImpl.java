/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.internal.upload;

import com.liferay.document.library.kernel.exception.DuplicateFileEntryException;
import com.liferay.document.library.kernel.util.DLValidatorUtil;
import com.liferay.exportimport.upload.ImportUploadEntry;
import com.liferay.exportimport.upload.ImportUploadException;
import com.liferay.exportimport.upload.ImportUploadManager;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.change.tracking.CTCollectionThreadLocal;
import com.liferay.portal.kernel.exception.NoSuchModelException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portletfilerepository.PortletFileRepositoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.search.IndexStatusManagerThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.upload.configuration.UploadServletRequestConfigurationProviderUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.TempFileEntryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.charset.StandardCharsets;

import java.util.Set;
import java.util.function.LongConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jaime León
 */
@Component(service = ImportUploadManager.class)
public class ImportUploadManagerImpl implements ImportUploadManager {

	@Override
	public ImportUploadEntry addImportUpload(
			long groupId, long userId, String fileName, String contentType,
			long size, long plid, String portletId)
		throws PortalException {

		if (Validator.isNull(fileName)) {
			throw new ImportUploadException("The file name is required");
		}

		if (size < 0) {
			throw new ImportUploadException("The file size is negative");
		}

		long maxSize =
			UploadServletRequestConfigurationProviderUtil.getMaxSize();

		if ((maxSize > 0) && (size > maxSize)) {
			throw new ImportUploadException(
				StringBundler.concat(
					"The file size ", size, " exceeds the maximum size ",
					maxSize));
		}

		long maxAllowableSize = DLValidatorUtil.getMaxAllowableSize(
			groupId, ContentTypes.APPLICATION_OCTET_STREAM);

		long chunkSize = 32 * 1024 * 1024;

		if ((maxAllowableSize > 0) && (maxAllowableSize < chunkSize)) {
			chunkSize = maxAllowableSize;
		}

		JSONObject jsonObject = JSONUtil.put(
			"chunkSize", chunkSize
		).put(
			"contentType", contentType
		).put(
			"fileName", fileName
		).put(
			"plid", plid
		).put(
			"portletId", portletId
		).put(
			"size", size
		);

		String importUploadId = PortalUUIDUtil.generate();

		try (SafeCloseable safeCloseable1 = _setCTCollectionId(groupId);
			SafeCloseable safeCloseable2 = _setIndexReadOnly()) {

			String json = jsonObject.toString();

			TempFileEntryUtil.addTempFileEntry(
				groupId, userId, _getFolderName(importUploadId),
				_METADATA_FILE_NAME,
				new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)),
				ContentTypes.APPLICATION_JSON);
		}

		return _toImportUploadEntry(importUploadId, jsonObject, 0);
	}

	@Override
	public void deleteImportUpload(
			long groupId, long userId, String importUploadId)
		throws PortalException {

		_validateImportUploadId(importUploadId);

		try (SafeCloseable safeCloseable1 = _setCTCollectionId(groupId);
			SafeCloseable safeCloseable2 = _setIndexReadOnly()) {

			FileEntry fileEntry = _fetchTempFileEntry(
				groupId, userId, importUploadId, _METADATA_FILE_NAME);

			if (fileEntry == null) {
				fileEntry = _fetchTempFileEntry(
					groupId, userId, importUploadId, _getChunkFileName(0));
			}

			if (fileEntry != null) {
				PortletFileRepositoryUtil.deletePortletFolder(
					fileEntry.getFolderId());
			}
		}
	}

	@Override
	public ImportUploadEntry fetchImportUpload(
			long groupId, long userId, String importUploadId)
		throws IOException, PortalException {

		_validateImportUploadId(importUploadId);

		try (SafeCloseable safeCloseable = _setCTCollectionId(groupId)) {
			FileEntry fileEntry = _fetchTempFileEntry(
				groupId, userId, importUploadId, _METADATA_FILE_NAME);

			if (fileEntry == null) {
				return null;
			}

			JSONObject jsonObject = null;

			try (InputStream inputStream = fileEntry.getContentStream()) {
				jsonObject = _jsonFactory.createJSONObject(
					StringUtil.read(inputStream));
			}

			return _toImportUploadEntry(
				importUploadId, jsonObject,
				_getUploadedChunksCount(groupId, userId, importUploadId));
		}
	}

	@Override
	public ImportUploadEntry putChunk(
			long groupId, long userId, ImportUploadEntry importUploadEntry,
			int chunkIndex, InputStream inputStream)
		throws IOException, PortalException {

		if ((chunkIndex < 0) ||
			(chunkIndex >= importUploadEntry.getTotalChunksCount())) {

			throw new ImportUploadException(
				"The chunk index " + chunkIndex + " is out of range");
		}

		long chunkLength = Math.min(
			importUploadEntry.getChunkSize(),
			importUploadEntry.getSize() -
				(chunkIndex * importUploadEntry.getChunkSize()));

		File file = null;

		try {
			file = FileUtil.createTempFile();

			if (_copy(inputStream, file, chunkLength) != chunkLength) {
				throw new ImportUploadException(
					StringBundler.concat(
						"The chunk ", chunkIndex, " must have ", chunkLength,
						" bytes"));
			}

			_addChunk(
				groupId, userId, importUploadEntry.getImportUploadId(),
				_getChunkFileName(chunkIndex), file, chunkLength);
		}
		finally {
			FileUtil.delete(file);
		}

		try (SafeCloseable safeCloseable = _setCTCollectionId(groupId)) {
			return new ImportUploadEntry(
				importUploadEntry.getChunkSize(),
				importUploadEntry.getContentType(),
				importUploadEntry.getFileName(),
				importUploadEntry.getImportUploadId(),
				importUploadEntry.getPlid(), importUploadEntry.getPortletId(),
				importUploadEntry.getSize(),
				_getUploadedChunksCount(
					groupId, userId, importUploadEntry.getImportUploadId()));
		}
	}

	@Override
	public void writeTo(
			long groupId, long userId, String importUploadId, File file)
		throws IOException, PortalException {

		writeTo(
			groupId, userId, importUploadId, file,
			bytesWritten -> {
			});
	}

	@Override
	public void writeTo(
			long groupId, long userId, String importUploadId, File file,
			LongConsumer bytesWrittenConsumer)
		throws IOException, PortalException {

		ImportUploadEntry importUploadEntry = fetchImportUpload(
			groupId, userId, importUploadId);

		if ((importUploadEntry == null) || !importUploadEntry.isComplete()) {
			throw new ImportUploadException(
				"The upload " + importUploadId + " is not complete");
		}

		try (SafeCloseable safeCloseable = _setCTCollectionId(groupId);
			OutputStream outputStream = new FileOutputStream(file)) {

			byte[] buffer = new byte[_BUFFER_SIZE];
			long bytesWritten = 0;

			for (int i = 0; i < importUploadEntry.getTotalChunksCount(); i++) {
				FileEntry fileEntry = TempFileEntryUtil.getTempFileEntry(
					groupId, userId, _getFolderName(importUploadId),
					_getChunkFileName(i));

				try (InputStream inputStream = fileEntry.getContentStream()) {
					int length = 0;

					while ((length = inputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, length);

						bytesWritten += length;

						bytesWrittenConsumer.accept(bytesWritten);
					}
				}
			}
		}
	}

	private void _addChunk(
			long groupId, long userId, String importUploadId, String fileName,
			File file, long chunkLength)
		throws PortalException {

		try (SafeCloseable safeCloseable1 = _setCTCollectionId(groupId);
			SafeCloseable safeCloseable2 = _setIndexReadOnly()) {

			FileEntry fileEntry = _fetchTempFileEntry(
				groupId, userId, importUploadId, fileName);

			if (fileEntry != null) {
				if (fileEntry.getSize() == chunkLength) {
					return;
				}

				TempFileEntryUtil.deleteTempFileEntry(
					groupId, userId, _getFolderName(importUploadId), fileName);
			}

			try {
				TempFileEntryUtil.addTempFileEntry(
					groupId, userId, _getFolderName(importUploadId), fileName,
					file, ContentTypes.APPLICATION_OCTET_STREAM);
			}
			catch (DuplicateFileEntryException duplicateFileEntryException) {
				fileEntry = _fetchTempFileEntry(
					groupId, userId, importUploadId, fileName);

				if ((fileEntry == null) ||
					(fileEntry.getSize() != chunkLength)) {

					throw duplicateFileEntryException;
				}
			}
		}
	}

	private long _copy(InputStream inputStream, File file, long maxLength)
		throws IOException {

		long length = 0;

		try (OutputStream outputStream = new FileOutputStream(file)) {
			byte[] buffer = new byte[_BUFFER_SIZE];
			int count = 0;

			while ((count = inputStream.read(buffer)) != -1) {
				length += count;

				if (length > maxLength) {
					return length;
				}

				outputStream.write(buffer, 0, count);
			}
		}

		return length;
	}

	private FileEntry _fetchTempFileEntry(
			long groupId, long userId, String importUploadId, String fileName)
		throws PortalException {

		try {
			return TempFileEntryUtil.getTempFileEntry(
				groupId, userId, _getFolderName(importUploadId), fileName);
		}
		catch (NoSuchModelException noSuchModelException) {
			if (_log.isDebugEnabled()) {
				_log.debug(noSuchModelException);
			}

			return null;
		}
	}

	private String _getChunkFileName(int chunkIndex) {
		return String.format("chunk-%05d", chunkIndex);
	}

	private String _getFolderName(String importUploadId) {
		return "com.liferay.exportimport.upload/" + importUploadId;
	}

	private int _getUploadedChunksCount(
			long groupId, long userId, String importUploadId)
		throws PortalException {

		int uploadedChunksCount = 0;

		Set<String> tempFileNames = SetUtil.fromArray(
			TempFileEntryUtil.getTempFileNames(
				groupId, userId, _getFolderName(importUploadId)));

		while (tempFileNames.contains(_getChunkFileName(uploadedChunksCount))) {
			uploadedChunksCount++;
		}

		return uploadedChunksCount;
	}

	private SafeCloseable _setCTCollectionId(long groupId)
		throws PortalException {

		Group group = _groupLocalService.getGroup(groupId);

		return CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
			group.getCtCollectionId());
	}

	private SafeCloseable _setIndexReadOnly() {
		boolean indexReadOnly = IndexStatusManagerThreadLocal.isIndexReadOnly();

		IndexStatusManagerThreadLocal.setIndexReadOnly(true);

		return () -> IndexStatusManagerThreadLocal.setIndexReadOnly(
			indexReadOnly);
	}

	private ImportUploadEntry _toImportUploadEntry(
		String importUploadId, JSONObject jsonObject, int uploadedChunksCount) {

		return new ImportUploadEntry(
			jsonObject.getLong("chunkSize"),
			jsonObject.getString("contentType"),
			jsonObject.getString("fileName"), importUploadId,
			jsonObject.getLong("plid"), jsonObject.getString("portletId"),
			jsonObject.getLong("size"), uploadedChunksCount);
	}

	private void _validateImportUploadId(String importUploadId)
		throws ImportUploadException {

		Matcher matcher = _importUploadIdPattern.matcher(
			GetterUtil.getString(importUploadId));

		if (!matcher.matches()) {
			throw new ImportUploadException(
				"The upload ID " + importUploadId + " is not valid");
		}
	}

	private static final int _BUFFER_SIZE = 65536;

	private static final String _METADATA_FILE_NAME = "upload.json";

	private static final Log _log = LogFactoryUtil.getLog(
		ImportUploadManagerImpl.class);

	private static final Pattern _importUploadIdPattern = Pattern.compile(
		"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private JSONFactory _jsonFactory;

}