/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.resource.v1_0.layout.structure.item.importer;

import com.liferay.headless.admin.site.dto.v1_0.PageContainerDefinition;
import com.liferay.headless.admin.site.dto.v1_0.PageElement;
import com.liferay.headless.admin.site.internal.resource.v1_0.layout.structure.item.importer.context.LayoutStructureItemImporterContext;
import com.liferay.headless.admin.site.internal.resource.v1_0.util.LayoutStructureUtil;
import com.liferay.layout.util.structure.ContainerStyledLayoutStructureItem;
import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.layout.util.structure.LayoutStructureItem;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;

/**
 * @author Eudaldo Alonso
 */
public class ContainerLayoutStructureItemImporter
	implements LayoutStructureItemImporter {

	@Override
	public LayoutStructureItem addLayoutStructureItem(
			LayoutStructure layoutStructure,
			LayoutStructureItemImporterContext
				layoutStructureItemImporterContext,
			PageElement pageElement)
		throws Exception {

		ContainerStyledLayoutStructureItem containerStyledLayoutStructureItem =
			(ContainerStyledLayoutStructureItem)
				layoutStructure.addContainerStyledLayoutStructureItem(
					pageElement.getExternalReferenceCode(),
					LayoutStructureUtil.getParentExternalReferenceCode(
						pageElement, layoutStructure),
					pageElement.getPosition());

		Object definitionObj = pageElement.getDefinition();

		PageContainerDefinition pageContainerDefinition = null;

		if (definitionObj != null) {
			if (definitionObj instanceof PageContainerDefinition) {
				pageContainerDefinition = (PageContainerDefinition)definitionObj;
			}
			else if (definitionObj instanceof LinkedHashMap) {
				ObjectMapper objectMapper = new ObjectMapper();
				pageContainerDefinition = objectMapper.convertValue(
					definitionObj, PageContainerDefinition.class);
			}
		}

		if (pageContainerDefinition == null) {
			return containerStyledLayoutStructureItem;
		}

		containerStyledLayoutStructureItem.setIndexed(
			pageContainerDefinition.getIndexed());
		containerStyledLayoutStructureItem.setName(
			pageContainerDefinition.getName());

		return containerStyledLayoutStructureItem;
	}

}