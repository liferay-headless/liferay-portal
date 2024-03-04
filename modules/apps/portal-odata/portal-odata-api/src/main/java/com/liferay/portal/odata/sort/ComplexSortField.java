/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.odata.sort;

import com.liferay.petra.string.StringPool;
import com.liferay.petra.string.StringUtil;
import com.liferay.portal.odata.entity.ComplexEntityField;
import com.liferay.portal.odata.entity.EntityField;

import java.util.List;
import java.util.Locale;

/**
 * @author Carlos Correa
 */
public class ComplexSortField extends SortField {

	public ComplexSortField(
		boolean asc, EntityField entityField,
		List<ComplexEntityField> pathComplexEntityFields) {

		super(entityField, asc);

		_pathComplexEntityFields = pathComplexEntityFields;
	}

	public String getSortableComplexFieldName(Locale locale) {
		String sortableFieldName = super.getSortableFieldName(locale);

		String prefix = StringUtil.merge(
			_pathComplexEntityFields,
			complexEntityField -> complexEntityField.getSortableName(locale),
			StringPool.FORWARD_SLASH);

		return prefix + StringPool.FORWARD_SLASH + sortableFieldName;
	}

	private final List<ComplexEntityField> _pathComplexEntityFields;

}