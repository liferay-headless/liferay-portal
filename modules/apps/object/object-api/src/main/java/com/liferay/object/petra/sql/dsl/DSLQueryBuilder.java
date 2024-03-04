/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.petra.sql.dsl;

import com.liferay.object.model.ObjectEntryTable;
import com.liferay.petra.sql.dsl.Column;
import com.liferay.petra.sql.dsl.DSLQueryFactoryUtil;
import com.liferay.petra.sql.dsl.Table;
import com.liferay.petra.sql.dsl.expression.Expression;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.sql.dsl.query.DSLQuery;
import com.liferay.petra.sql.dsl.query.GroupByStep;
import com.liferay.petra.sql.dsl.query.JoinStep;
import com.liferay.petra.sql.dsl.query.LimitStep;
import com.liferay.petra.sql.dsl.query.OrderByStep;
import com.liferay.petra.sql.dsl.query.sort.OrderByExpression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Carlos Correa
 */
public class DSLQueryBuilder {

	public DSLQuery build() {
		JoinStep joinStep = DSLQueryFactoryUtil.select(
			_selectExpressions
		).from(
			_fromTable
		);

		Set<String> allTableNames = new HashSet<>();

		allTableNames.add(_fromTable.getName());

		for (JoinItem joinItem : _joinItems) {
			Table<?> table = joinItem.getTable();

			if (!allTableNames.add(table.getName())) {
				continue;
			}

			if (joinItem.getJoinType() == JoinItem.JoinType.INNER) {
				joinStep = joinStep.innerJoinON(
					joinItem.getTable(), joinItem.getPredicate());
			}
			else if (joinItem.getJoinType() == JoinItem.JoinType.LEFT) {
				joinStep = joinStep.leftJoinOn(
					joinItem.getTable(), joinItem.getPredicate());
			}
		}

		GroupByStep groupByStep = joinStep;

		if (_wherePredicate != null) {
			groupByStep = joinStep.where(_wherePredicate);
		}

		OrderByStep orderByStep = groupByStep;

		if (_groupByExpressions != null) {
			orderByStep = groupByStep.groupBy(_groupByExpressions);
		}

		LimitStep limitStep = orderByStep;

		if (!_orderByExpressions.isEmpty()) {
			limitStep = orderByStep.orderBy(
				_orderByExpressions.toArray(new OrderByExpression[0]));
		}

		DSLQuery dslQuery = limitStep;

		if (_limitItem != null) {
			dslQuery = limitStep.limit(
				_limitItem.getStart(), _limitItem.getEnd());
		}

		return dslQuery;
	}

	public DSLQueryBuilder from(Table<?> table) {
		_fromTable = table;
		_lastTable = table;

		return this;
	}

	public DSLQueryBuilder groupBySelectExpressions() {
		_groupByExpressions = _selectExpressions;

		return this;
	}

	public DSLQueryBuilder innerJoin(Predicate predicate, Table<?> table) {
		if (table == null) {
			return this;
		}

		_joinItems.add(new JoinItem(JoinItem.JoinType.INNER, predicate, table));
		_lastTable = table;

		return this;
	}

	public DSLQueryBuilder leftJoin(Column<?, Long> column, Table<?> table) {
		if (table == null) {
			return this;
		}

		_joinItems.add(
			new JoinItem(
				JoinItem.JoinType.LEFT, _getJoinPredicate(column, _lastTable),
				table));
		_lastTable = table;

		return this;
	}

	public DSLQueryBuilder leftJoin(Predicate predicate, Table<?> table) {
		if (table == null) {
			return this;
		}

		_joinItems.add(new JoinItem(JoinItem.JoinType.LEFT, predicate, table));
		_lastTable = table;

		return this;
	}

	public DSLQueryBuilder limit(int end, int start) {
		_limitItem = new LimitItem(end, start);

		return this;
	}

	public DSLQueryBuilder orderBy(OrderByExpression orderByExpression) {
		_orderByExpressions.add(orderByExpression);

		return this;
	}

	public DSLQueryBuilder orderBy(OrderByExpression... orderByExpressions) {
		Collections.addAll(_orderByExpressions, orderByExpressions);

		return this;
	}

	public DSLQueryBuilder select(Expression<?>[] selectExpressions) {
		_selectExpressions = selectExpressions;

		return this;
	}

	public DSLQueryBuilder where(Predicate predicate) {
		_wherePredicate = predicate;

		return this;
	}

	private Predicate _getJoinPredicate(
		Column<?, Long> column, Table<?> table) {

		Column<?, Long> pkColumn = null;

		if (table instanceof DynamicObjectDefinitionLocalizationTable) {
			DynamicObjectDefinitionLocalizationTable
				dynamicObjectDefinitionLocalizationTable =
					(DynamicObjectDefinitionLocalizationTable)table;

			pkColumn =
				dynamicObjectDefinitionLocalizationTable.getForeignKeyColumn();
		}
		else if (table instanceof DynamicObjectDefinitionTable) {
			DynamicObjectDefinitionTable dynamicObjectDefinitionTable =
				(DynamicObjectDefinitionTable)table;

			pkColumn = dynamicObjectDefinitionTable.getPrimaryKeyColumn();
		}
		else if (table instanceof ObjectEntryTable) {
			ObjectEntryTable objectEntryTable = (ObjectEntryTable)table;

			pkColumn = objectEntryTable.objectEntryId;
		}

		return pkColumn.eq(column);
	}

	private Table<?> _fromTable;
	private Expression<?>[] _groupByExpressions;
	private final List<JoinItem> _joinItems = new ArrayList<>();
	private Table<?> _lastTable;
	private LimitItem _limitItem;
	private final List<OrderByExpression> _orderByExpressions =
		new ArrayList<>();
	private Expression<?>[] _selectExpressions;
	private Predicate _wherePredicate;

	private static class JoinItem {

		public JoinItem(
			JoinType joinType, Predicate predicate, Table<?> table) {

			_joinType = joinType;
			_predicate = predicate;
			_table = table;
		}

		public JoinType getJoinType() {
			return _joinType;
		}

		public Predicate getPredicate() {
			return _predicate;
		}

		public Table<?> getTable() {
			return _table;
		}

		public enum JoinType {

			INNER, LEFT

		}

		private final JoinType _joinType;
		private final Predicate _predicate;
		private final Table<?> _table;

	}

	private static class LimitItem {

		public LimitItem(int end, int start) {
			_end = end;
			_start = start;
		}

		public int getEnd() {
			return _end;
		}

		public int getStart() {
			return _start;
		}

		private final int _end;
		private final int _start;

	}

}