/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.internal.background.task;

import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.function.UnsafeRunnable;

import java.io.File;

/**
 * @author Jaime León
 */
public class ImportFileSource {

	public ImportFileSource(
		String fileName, UnsafeConsumer<File, Exception> unsafeConsumer,
		UnsafeRunnable<Exception> unsafeRunnable) {

		_fileName = fileName;
		_unsafeConsumer = unsafeConsumer;
		_unsafeRunnable = unsafeRunnable;
	}

	public void delete() throws Exception {
		_unsafeRunnable.run();
	}

	public String getFileName() {
		return _fileName;
	}

	public void write(File file) throws Exception {
		_unsafeConsumer.accept(file);
	}

	private final String _fileName;
	private final UnsafeConsumer<File, Exception> _unsafeConsumer;
	private final UnsafeRunnable<Exception> _unsafeRunnable;

}