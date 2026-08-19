/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal;

import com.liferay.petra.string.CharPool;
import com.liferay.portal.kernel.util.CSVUtil;

/**
 * @author Gabor Komaromi
 */
public class BatchEngineFormulaInjectionUtil {

	public static Object neutralize(Object value) {
		if (!(value instanceof String)) {
			return value;
		}

		return CSVUtil.escapeValue((String)value);
	}

	public static String restore(String value) {
		if ((value == null) || (value.length() < 2) ||
			(value.charAt(0) != CharPool.APOSTROPHE) ||
			!CSVUtil.isFormulaInjectionPrefix(value.charAt(1))) {

			return value;
		}

		return value.substring(1);
	}

}