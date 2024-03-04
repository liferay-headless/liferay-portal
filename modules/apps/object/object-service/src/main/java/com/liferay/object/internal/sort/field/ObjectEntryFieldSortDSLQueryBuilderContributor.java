/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.sort.field;

import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.internal.sort.BaseSortDSLQueryBuilderContributor;
import com.liferay.object.internal.sort.Sort;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectField;
import com.liferay.object.petra.sql.dsl.DSLQueryBuilder;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.petra.sql.dsl.Column;
import com.liferay.petra.sql.dsl.DSLFunctionFactoryUtil;
import com.liferay.petra.sql.dsl.Table;
import com.liferay.petra.sql.dsl.expression.Expression;
import com.liferay.petra.sql.dsl.query.sort.OrderByExpression;
import com.liferay.petra.sql.dsl.spi.expression.AggregateExpression;
import com.liferay.petra.string.CharPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.StringUtil;

import java.sql.Clob;
import java.sql.Types;

/**
 * @author Carlos Correa
 */
public class ObjectEntryFieldSortDSLQueryBuilderContributor
	extends BaseSortDSLQueryBuilderContributor {

	public ObjectEntryFieldSortDSLQueryBuilderContributor(
		ObjectFieldLocalService objectFieldLocalService) {

		super(objectFieldLocalService, null);
	}

	@Override
	public void contribute(DSLQueryBuilder dslQueryBuilder, Sort sort)
		throws PortalException {

		ObjectDefinition objectDefinition = sort.getObjectDefinition();

		ObjectField objectField = objectFieldLocalService.getObjectField(
			objectDefinition.getObjectDefinitionId(), sort.getFieldName());

		Table fieldTable = getAliasedTable(
			objectField.getName(), objectDefinition, _getSuffix(sort));

		dslQueryBuilder.leftJoin(getPrimaryKeyColumn(fieldTable), fieldTable);

		dslQueryBuilder.orderBy(
			_getOrderByExpression(
				_isParentComplexField(sort),
				_getColumnExpression(objectField, fieldTable),
				sort.isReverse()));
	}

	private Expression<?> _getColumnExpression(
		ObjectField objectField, Table table) {

		if (objectField.compareBusinessType(
				ObjectFieldConstants.BUSINESS_TYPE_AUTO_INCREMENT)) {

			return table.getColumn(objectField.getSortableDBColumnName());
		}

		Column<?, ?> column = table.getColumn(objectField.getDBColumnName());

		if (column.getSQLType() == Types.CLOB) {
			return DSLFunctionFactoryUtil.castClobText(
				(Expression<Clob>)column);
		}

		return column;
	}

	private OrderByExpression _getOrderByExpression(
		boolean aggregate, Expression<?> expression, boolean reverse) {

		if (reverse) {
			if (aggregate) {
				expression = new AggregateExpression<>(
					false, expression, "max");
			}

			return expression.descending();
		}

		if (aggregate) {
			expression = new AggregateExpression<>(false, expression, "min");
		}

		return expression.ascending();
	}

	private String _getSuffix(Sort sort) {
		if (_isParentComplexField(sort)) {
			return StringUtil.replace(
				StringUtil.removeLast(
					sort.getFieldPath(),
					CharPool.FORWARD_SLASH + sort.getFieldName()),
				CharPool.FORWARD_SLASH, CharPool.UNDERLINE);
		}

		return null;
	}

	private boolean _isParentComplexField(Sort sort) {
		if (StringUtil.equals(sort.getFieldName(), sort.getFieldPath())) {
			return false;
		}

		return true;
	}

}