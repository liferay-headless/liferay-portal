/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.writer;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.ListUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 * @author Ivica Cardic
 * @author Igor Beslic
 * @author Matija Petanjek
 */
public class CSVBatchEngineExportTaskItemWriterImpl
	implements BatchEngineExportTaskItemWriter {

	public CSVBatchEngineExportTaskItemWriterImpl(
			String delimiter, List<String> fieldNames, Class<?> itemClass,
			OutputStream outputStream, Map<String, Serializable> parameters)
		throws IOException {

		if (fieldNames.isEmpty()) {
			throw new IllegalArgumentException("Field names are not set");
		}

		_csvPrinter = new CSVPrinter(
			new BufferedWriter(new OutputStreamWriter(outputStream)),
			_getCSVFormat(delimiter));

		fieldNames = ListUtil.sort(
			fieldNames, (value1, value2) -> value1.compareToIgnoreCase(value2));

		_columnValuesExtractor = new ColumnValuesExtractor(
			fieldNames, itemClass);

		_containsHeaders = Boolean.valueOf(
			(String)parameters.getOrDefault(
				"containsHeaders", StringPool.TRUE));
	}

	@Override
	public void close() throws IOException {
		_csvPrinter.close();
	}

	@Override
	public void write(Collection<?> items) throws Exception {
		SortedSet<String> sortedSet = new TreeSet<>();

		for (Object item : items) {
			Collections.addAll(
				sortedSet, _columnValuesExtractor.getHeaders(item));
		}

		sortedSet.removeIf(String::isEmpty);

		if (_containsHeaders) {
			_containsHeaders = false;

			_csvPrinter.printRecord(sortedSet);
		}

		for (Object item : items) {
			for (List<Object> values :
					_columnValuesExtractor.extractValues(item, sortedSet)) {

				_write(values);
			}
		}
	}

	private CSVFormat _getCSVFormat(String delimiter) {
		CSVFormat.Builder builder = CSVFormat.Builder.create();

		builder.setDelimiter(delimiter);

		return builder.build();
	}

	private void _write(List<Object> values) throws Exception {
		for (Object value : values) {
			if (value instanceof Map) {
				Map<String, Object> map = (Map<String, Object>)value;

				StringBundler sb = new StringBundler();

				for (Map.Entry<String, Object> entry : map.entrySet()) {
					sb.append(entry.getKey());
					sb.append(StringPool.COLON);
					sb.append(entry.getValue());
					sb.append(StringPool.RETURN_NEW_LINE);
				}

				value = sb.toString();
			}

			_csvPrinter.print(value);
		}

		_csvPrinter.println();
	}

	private final ColumnValuesExtractor _columnValuesExtractor;
	private boolean _containsHeaders;
	private final CSVPrinter _csvPrinter;

}