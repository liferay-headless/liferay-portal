/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.resource.v1_0.layout.structure.item.importer;

import com.liferay.headless.admin.site.dto.v1_0.PageElement;
import com.liferay.headless.admin.site.dto.v1_0.PageRowDefinition;
import com.liferay.headless.admin.site.internal.resource.v1_0.layout.structure.item.importer.context.LayoutStructureItemImporterContext;
import com.liferay.headless.admin.site.internal.resource.v1_0.util.LayoutStructureUtil;
import com.liferay.layout.util.constants.LayoutDataItemTypeConstants;
import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.layout.util.structure.LayoutStructureItem;
import com.liferay.layout.util.structure.RowStyledLayoutStructureItem;
import com.liferay.portal.kernel.util.SetUtil;

import java.util.LinkedHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Eudaldo Alonso
 */
public class RowLayoutStructureItemImporter
	implements LayoutStructureItemImporter {

	@Override
	public LayoutStructureItem addLayoutStructureItem(
			LayoutStructure layoutStructure,
			LayoutStructureItemImporterContext
				layoutStructureItemImporterContext,
			PageElement pageElement)
		throws Exception {

		RowStyledLayoutStructureItem rowStyledLayoutStructureItem =
			(RowStyledLayoutStructureItem)
				layoutStructure.addLayoutStructureItem(
					pageElement.getExternalReferenceCode(),
					LayoutDataItemTypeConstants.TYPE_ROW,
					LayoutStructureUtil.getParentExternalReferenceCode(
						pageElement, layoutStructure),
					pageElement.getPosition());
		
		Object definitionObj = pageElement.getDefinition();

		PageRowDefinition pageRowDefinition = null;

		if (definitionObj != null) {
			if (definitionObj instanceof PageRowDefinition) {
				pageRowDefinition = (PageRowDefinition)definitionObj;
			}
			else if (definitionObj instanceof LinkedHashMap) {
				ObjectMapper objectMapper = new ObjectMapper();
				pageRowDefinition = objectMapper.convertValue(
					definitionObj, PageRowDefinition.class);
			}
		}

		if (pageRowDefinition == null) {
			return rowStyledLayoutStructureItem;
		}

		rowStyledLayoutStructureItem.setCssClasses(
			SetUtil.fromArray(pageRowDefinition.getCssClasses()));
		rowStyledLayoutStructureItem.setCustomCSS(
			pageRowDefinition.getCustomCSS());
		rowStyledLayoutStructureItem.setGutters(pageRowDefinition.getGutters());
		rowStyledLayoutStructureItem.setIndexed(pageRowDefinition.getIndexed());
		rowStyledLayoutStructureItem.setName(pageRowDefinition.getName());
		rowStyledLayoutStructureItem.setNumberOfColumns(
			pageRowDefinition.getNumberOfColumns());
		rowStyledLayoutStructureItem.setModulesPerRow(
			pageRowDefinition.getModulesPerRow());
		rowStyledLayoutStructureItem.setReverseOrder(
			pageRowDefinition.getReverseOrder());
		rowStyledLayoutStructureItem.setVerticalAlignment(
			pageRowDefinition.getVerticalAlignment());

		return rowStyledLayoutStructureItem;
	}

}