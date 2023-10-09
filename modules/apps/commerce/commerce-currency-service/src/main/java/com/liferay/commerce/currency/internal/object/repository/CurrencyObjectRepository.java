/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.currency.internal.object.repository;

import com.liferay.commerce.currency.constants.CurrencyRepositoryConstants;
import com.liferay.commerce.currency.object.entity.CurrencyObjectEntity;
import com.liferay.object.petra.sql.dsl.DynamicObjectDefinitionTable;
import com.liferay.object.repository.BaseObjectRepository;
import com.liferay.object.repository.ObjectRepository;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.sql.dsl.query.sort.OrderByExpression;
import com.liferay.portal.kernel.util.ObjectValuePair;

import java.util.Objects;

import org.osgi.service.component.annotations.Component;

/**
 * @author Riccardo Alberti
 */
@Component(service = ObjectRepository.class)
public class CurrencyObjectRepository
	extends BaseObjectRepository<CurrencyObjectEntity> {

	@Override
	protected ObjectValuePair<Predicate, OrderByExpression[]>
		getPredicateAndOrderByExpression(
			String predicateName,
			DynamicObjectDefinitionTable dynamicObjectDefinitionTable,
			Object... parameters) {

		ObjectValuePair<Predicate, OrderByExpression[]> objectValuePair =
			new ObjectValuePair<>(null, null);

		if (Objects.equals(
				predicateName, CurrencyRepositoryConstants.FIND_BY_ACTIVE)) {

			objectValuePair.setKey(
				dynamicObjectDefinitionTable.getColumn(
					"active_", Boolean.class
				).eq(
					(Boolean)parameters[0]
				));

			objectValuePair.setValue(null);
		}
		else if (Objects.equals(
					predicateName,
					CurrencyRepositoryConstants.FIND_BY_PRIMARY)) {

			objectValuePair.setKey(
				dynamicObjectDefinitionTable.getColumn(
					"primary_", Boolean.class
				).eq(
					(Boolean)parameters[0]
				));

			objectValuePair.setValue(null);
		}
		else if (Objects.equals(
					predicateName, CurrencyRepositoryConstants.FIND_BY_CODE)) {

			objectValuePair.setKey(
				dynamicObjectDefinitionTable.getColumn(
					"code_", String.class
				).eq(
					(String)parameters[0]
				));

			objectValuePair.setValue(null);
		}

		return objectValuePair;
	}

}