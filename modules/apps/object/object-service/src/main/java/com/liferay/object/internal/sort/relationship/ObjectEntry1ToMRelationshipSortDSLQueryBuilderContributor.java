/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.sort.relationship;

import com.liferay.object.internal.sort.BaseSortDSLQueryBuilderContributor;
import com.liferay.object.internal.sort.Sort;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.petra.sql.dsl.DSLQueryBuilder;
import com.liferay.object.petra.sql.dsl.DynamicObjectDefinitionTable;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.sql.dsl.Column;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.StringUtil;

/**
 * @author Carlos Correa
 */
public class ObjectEntry1ToMRelationshipSortDSLQueryBuilderContributor
	extends BaseSortDSLQueryBuilderContributor {

	public ObjectEntry1ToMRelationshipSortDSLQueryBuilderContributor(
		ObjectFieldLocalService objectFieldLocalService,
		ObjectRelationshipLocalService objectRelationshipLocalService) {

		super(objectFieldLocalService, objectRelationshipLocalService);
	}

	@Override
	public void contribute(DSLQueryBuilder dslQueryBuilder, Sort sort)
		throws PortalException {

		RelationshipSort relationshipSort = (RelationshipSort)sort;

		ObjectDefinition objectDefinition =
			relationshipSort.getObjectDefinition();

		ObjectRelationship objectRelationship =
			relationshipSort.getObjectRelationship();

		String relationshipFieldName = StringBundler.concat(
			"r_", objectRelationship.getName(), "_",
			objectDefinition.getPKObjectFieldName());

		DynamicObjectDefinitionTable dynamicObjectDefinitionTable =
			(DynamicObjectDefinitionTable)getAliasedTable(
				relationshipFieldName,
				relationshipSort.getRelatedObjectDefinition(),
				StringUtil.replace(
					relationshipSort.getFieldPath(), CharPool.FORWARD_SLASH,
					CharPool.UNDERLINE));

		Column<DynamicObjectDefinitionTable, Long> relationshipColumn =
			(Column<DynamicObjectDefinitionTable, Long>)
				dynamicObjectDefinitionTable.getColumn(relationshipFieldName);

		dslQueryBuilder.leftJoin(
			relationshipColumn, dynamicObjectDefinitionTable);

		dslQueryBuilder.groupBySelectExpressions();
	}

}