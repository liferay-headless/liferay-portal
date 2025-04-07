/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.resource.v1_0.layout.structure.item.importer;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.headless.admin.site.dto.v1_0.PageElement;
import com.liferay.headless.admin.site.dto.v1_0.PageFragmentDropZoneDefinition;
import com.liferay.headless.admin.site.internal.resource.v1_0.layout.structure.item.importer.context.LayoutStructureItemImporterContext;
import com.liferay.headless.admin.site.internal.resource.v1_0.util.LayoutStructureUtil;
import com.liferay.layout.util.structure.FragmentDropZoneLayoutStructureItem;
import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.layout.util.structure.LayoutStructureItem;

import java.util.LinkedHashMap;

/**
 * @author Eudaldo Alonso
 */
public class FragmentDropZoneLayoutStructureItemImporter
	implements LayoutStructureItemImporter {

	@Override
	public LayoutStructureItem addLayoutStructureItem(
			LayoutStructure layoutStructure,
			LayoutStructureItemImporterContext
				layoutStructureItemImporterContext,
			PageElement pageElement)
		throws Exception {

		FragmentDropZoneLayoutStructureItem
			fragmentDropZoneLayoutStructureItem =
				(FragmentDropZoneLayoutStructureItem)
					layoutStructure.addFragmentDropZoneLayoutStructureItem(
						pageElement.getExternalReferenceCode(),
						LayoutStructureUtil.getParentExternalReferenceCode(
							pageElement, layoutStructure),
						pageElement.getPosition());

		Object definitionObject = pageElement.getDefinition();

		PageFragmentDropZoneDefinition pageFragmentDropZoneDefinition = null;

		if (definitionObject != null) {
			if (definitionObject instanceof PageFragmentDropZoneDefinition) {
				pageFragmentDropZoneDefinition =
					(PageFragmentDropZoneDefinition)definitionObject;
			}
			else if (definitionObject instanceof LinkedHashMap) {
				ObjectMapper objectMapper = new ObjectMapper();

				pageFragmentDropZoneDefinition = objectMapper.convertValue(
					definitionObject, PageFragmentDropZoneDefinition.class);
			}
		}

		if (pageFragmentDropZoneDefinition == null) {
			return fragmentDropZoneLayoutStructureItem;
		}

		fragmentDropZoneLayoutStructureItem.setFragmentDropZoneId(
			pageFragmentDropZoneDefinition.getFragmentDropZoneId());

		return fragmentDropZoneLayoutStructureItem;
	}

}