/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.sort;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntryTable;
import com.liferay.object.petra.sql.dsl.DSLQueryBuilder;
import com.liferay.object.petra.sql.dsl.DynamicObjectDefinitionLocalizationTable;
import com.liferay.object.petra.sql.dsl.DynamicObjectDefinitionTable;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.sql.dsl.Column;
import com.liferay.petra.sql.dsl.Table;
import com.liferay.petra.string.CharPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author Carlos Correa
 */
public abstract class BaseSortDSLQueryBuilderContributor {

	public BaseSortDSLQueryBuilderContributor(
		ObjectFieldLocalService objectFieldLocalService,
		ObjectRelationshipLocalService objectRelationshipLocalService) {

		this.objectFieldLocalService = objectFieldLocalService;
		this.objectRelationshipLocalService = objectRelationshipLocalService;
	}

	public abstract void contribute(DSLQueryBuilder dslQueryBuilder, Sort sort)
		throws PortalException;

	protected Table getAliasedTable(
			String fieldName, ObjectDefinition objectDefinition, String prefix)
		throws PortalException {

		Table table = objectFieldLocalService.getTable(
			objectDefinition.getObjectDefinitionId(), fieldName);

		if (Validator.isBlank(prefix)) {
			return table;
		}

		return table.as(prefix + CharPool.UNDERLINE + table.getName());
	}

	protected Column<?, Long> getPrimaryKeyColumn(Table<?> table) {
		if (table instanceof DynamicObjectDefinitionLocalizationTable) {
			DynamicObjectDefinitionLocalizationTable
				dynamicObjectDefinitionLocalizationTable =
					(DynamicObjectDefinitionLocalizationTable)table;

			return dynamicObjectDefinitionLocalizationTable.
				getForeignKeyColumn();
		}
		else if (table instanceof DynamicObjectDefinitionTable) {
			DynamicObjectDefinitionTable dynamicObjectDefinitionTable =
				(DynamicObjectDefinitionTable)table;

			return dynamicObjectDefinitionTable.getPrimaryKeyColumn();
		}

		ObjectEntryTable objectEntryTable = (ObjectEntryTable)table;

		return objectEntryTable.objectEntryId;
	}

	protected ObjectFieldLocalService objectFieldLocalService;
	protected ObjectRelationshipLocalService objectRelationshipLocalService;

}