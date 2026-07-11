/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.odata.entity;

/**
 * Models a collection entity field.
 *
 * @author Rubén Pulido
 * @review
 */
public class CollectionEntityField extends EntityField {

	/**
	 * Creates a new {@code EntityField} of type COLLECTION. The field is not
	 * sortable.
	 *
	 * @param  entityField the entity field
	 * @review
	 */
	public CollectionEntityField(EntityField entityField) {
		this(entityField, false);
	}

	/**
	 * Creates a new {@code EntityField} of type COLLECTION.
	 *
	 * @param  entityField the entity field
	 * @param  sortable whether the underlying indexed field is genuinely
	 *         single-valued and safe to sort by, despite being wrapped as a
	 *         collection (usually to support OData's {@code any()} filter
	 *         syntax). Most collection fields are not sortable, since
	 *         sorting a multi-valued field relies on the search engine's
	 *         implicit reduction rather than a well-defined ordering.
	 * @review
	 */
	public CollectionEntityField(EntityField entityField, boolean sortable) {
		super(
			entityField.getName(), Type.COLLECTION,
			entityField::getSortableName, entityField::getFilterableName,
			String::valueOf);

		_entityField = entityField;
		_sortable = sortable;
	}

	/**
	 * Gets the {@code EntityField}.
	 *
	 * @return the entity field
	 * @review
	 */
	public EntityField getEntityField() {
		return _entityField;
	}

	/**
	 * Returns whether the underlying indexed field is genuinely
	 * single-valued and safe to sort by.
	 *
	 * @return whether the field is sortable
	 * @review
	 */
	public boolean isSortable() {
		return _sortable;
	}

	private final EntityField _entityField;
	private final boolean _sortable;

}