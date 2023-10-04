/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.action;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Carlos Correa
 */
public class EntityExtensionItem<T> {

	public T getItem() {
		return _item;
	}

	public void setItem(T item) {
		_item = item;
	}

	public Map<String, Serializable> getExtendedProperties() {
		return _extendedProperties;
	}

	public void setExtendedProperties(
		Map<String, Serializable> extendedProperties) {
		_extendedProperties = extendedProperties;
	}

	private T _item;
	private Map<String, Serializable> _extendedProperties;
}
