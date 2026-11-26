/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.constants;

/**
 * @author Magdalena Jedraszak
 */
public enum DeleteStrategy {

	DELETE("DELETE", "delete-records", true),
	DELETE_IF_EXISTS("DELETE_IF_EXISTS", "delete-records-if-they-exist", false);

	public static DeleteStrategy getDefaultDeleteStrategy() {
		for (DeleteStrategy deleteStrategy : values()) {
			if (deleteStrategy._defaultStrategy) {
				return deleteStrategy;
			}
		}

		throw new IllegalStateException();
	}

	public String getDBOperation() {
		return _dbOperation;
	}

	public String getLabel() {
		return _label;
	}

	public boolean isDefaultStrategy() {
		return _defaultStrategy;
	}

	private DeleteStrategy(
		String dbOperation, String label, boolean defaultStrategy) {

		_dbOperation = dbOperation;
		_label = label;
		_defaultStrategy = defaultStrategy;
	}

	private final String _dbOperation;
	private final boolean _defaultStrategy;
	private final String _label;

}