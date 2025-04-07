/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.resource.v1_0.layout.structure.item.importer;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.headless.admin.site.dto.v1_0.PageColumnDefinition;
import com.liferay.headless.admin.site.dto.v1_0.PageElement;
import com.liferay.headless.admin.site.internal.resource.v1_0.layout.structure.item.importer.context.LayoutStructureItemImporterContext;
import com.liferay.headless.admin.site.internal.resource.v1_0.util.LayoutStructureUtil;
import com.liferay.layout.util.structure.ColumnLayoutStructureItem;
import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.layout.util.structure.LayoutStructureItem;

import java.util.LinkedHashMap;

/**
 * @author Eudaldo Alonso
 */
public class ColumnLayoutStructureItemImporter
	implements LayoutStructureItemImporter {

	@Override
	public LayoutStructureItem addLayoutStructureItem(
			LayoutStructure layoutStructure,
			LayoutStructureItemImporterContext
				layoutStructureItemImporterContext,
			PageElement pageElement)
		throws Exception {

		ColumnLayoutStructureItem columnLayoutStructureItem =
			(ColumnLayoutStructureItem)
				layoutStructure.addColumnLayoutStructureItem(
					pageElement.getExternalReferenceCode(),
					LayoutStructureUtil.getParentExternalReferenceCode(
						pageElement, layoutStructure),
					pageElement.getPosition());

		Object definitionObject = pageElement.getDefinition();

		PageColumnDefinition pageColumnDefinition = null;

		if (definitionObject != null) {
			if (definitionObject instanceof PageColumnDefinition) {
				pageColumnDefinition = (PageColumnDefinition)definitionObject;
			}
			else if (definitionObject instanceof LinkedHashMap) {
				ObjectMapper objectMapper = new ObjectMapper();

				pageColumnDefinition = objectMapper.convertValue(
					definitionObject, PageColumnDefinition.class);
			}
		}

		if (pageColumnDefinition == null) {
			return columnLayoutStructureItem;
		}

		columnLayoutStructureItem.setSize(pageColumnDefinition.getSize());

		return columnLayoutStructureItem;
	}

}