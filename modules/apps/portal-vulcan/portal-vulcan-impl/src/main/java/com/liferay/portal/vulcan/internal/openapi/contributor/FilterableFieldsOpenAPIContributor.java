/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.openapi.contributor;

import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.odata.entity.CollectionEntityField;
import com.liferay.portal.odata.entity.ComplexEntityField;
import com.liferay.portal.vulcan.openapi.contributor.OpenAPIContributor;

import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Magdalena Jedraszak
 */
@Component(service = OpenAPIContributor.class)
public class FilterableFieldsOpenAPIContributor
	extends BaseFieldsOpenAPIContributor {

	public FilterableFieldsOpenAPIContributor() {
		super("x-filterable", "filter");
	}

	@Override
	protected Map<String, Object> getCollectionFieldValue(
		CollectionEntityField collectionEntityField) {

		return HashMapBuilder.<String, Object>put(
			"items",
			StringUtil.toLowerCase(
				String.valueOf(
					collectionEntityField.getEntityField(
					).getType()))
		).put(
			"type", "array"
		).build();
	}

	@Override
	protected boolean shouldRecurse(ComplexEntityField complexEntityField) {
		return true;
	}

}