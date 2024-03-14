/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.sort;

import com.liferay.object.constants.ObjectRelationshipConstants;
import com.liferay.object.internal.sort.field.ObjectEntryFieldSortDSLQueryBuilderContributor;
import com.liferay.object.internal.sort.relationship.ObjectEntry1ToMRelationshipSortDSLQueryBuilderContributor;
import com.liferay.object.internal.sort.relationship.RelationshipSort;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.petra.sql.dsl.DSLQueryBuilder;
import com.liferay.object.relationship.util.ObjectRelationshipUtil;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.petra.string.StringUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.odata.sort.InvalidSortException;

import java.util.List;
import java.util.Objects;

/**
 * @author Carlos Correa
 */
public class SortDSLQueryBuilderContributor
	extends BaseSortDSLQueryBuilderContributor {

	public SortDSLQueryBuilderContributor(
		ObjectFieldLocalService objectFieldLocalService,
		ObjectRelationshipLocalService objectRelationshipLocalService) {

		super(objectFieldLocalService, objectRelationshipLocalService);
	}

	public void contribute(DSLQueryBuilder dslQueryBuilder, Sort sort)
		throws PortalException {

		ObjectDefinition currentObjectDefinition = sort.getObjectDefinition();

		List<String> fieldNameParts = StringUtil.split(
			sort.getFieldPath(), CharPool.FORWARD_SLASH);

		List<String> relationshipNames = ListUtil.subList(
			fieldNameParts, 0, fieldNameParts.size() - 1);

		for (int i = 0; i < relationshipNames.size(); i++) {
			String relationshipName = relationshipNames.get(i);

			ObjectRelationship objectRelationship =
				objectRelationshipLocalService.
					getObjectRelationshipByObjectDefinitionId(
						currentObjectDefinition.getObjectDefinitionId(),
						relationshipName);

			ObjectDefinition relatedObjectDefinition =
				ObjectRelationshipUtil.getRelatedObjectDefinition(
					currentObjectDefinition, objectRelationship);

			_contributeWithRelationship(
				dslQueryBuilder, currentObjectDefinition, objectRelationship,
				StringUtil.merge(
					relationshipNames.subList(0, i + 1),
					StringPool.FORWARD_SLASH),
				relatedObjectDefinition, sort);

			currentObjectDefinition = relatedObjectDefinition;
		}

		ObjectEntryFieldSortDSLQueryBuilderContributor
			objectEntryFieldSortDSLQueryBuilderContributor =
				new ObjectEntryFieldSortDSLQueryBuilderContributor(
					objectFieldLocalService);

		objectEntryFieldSortDSLQueryBuilderContributor.contribute(
			dslQueryBuilder, new Sort(currentObjectDefinition, sort));
	}

	private void _contributeWithRelationship(
			DSLQueryBuilder dslQueryBuilder, ObjectDefinition objectDefinition,
			ObjectRelationship objectRelationship, String path,
			ObjectDefinition relatedObjectDefinition, Sort sort)
		throws PortalException {

		if (!FeatureFlagManagerUtil.isEnabled("LPD-18730")) {
			throw new InvalidSortException("Unable to sort by a related field");
		}

		if (!Objects.equals(
				objectRelationship.getType(),
				ObjectRelationshipConstants.TYPE_ONE_TO_MANY)) {

			throw new InvalidSortException(
				"Unable to sort by a " + objectRelationship.getType() +
					"  related field");
		}

		if (objectDefinition.getObjectDefinitionId() !=
				objectRelationship.getObjectDefinitionId1()) {

			throw new InvalidSortException(
				"Unable to sort by a many to one related field");
		}

		ObjectEntry1ToMRelationshipSortDSLQueryBuilderContributor
			objectEntry1ToMRelationshipSortDSLQueryBuilderContributor =
				new ObjectEntry1ToMRelationshipSortDSLQueryBuilderContributor(
					objectFieldLocalService, objectRelationshipLocalService);

		objectEntry1ToMRelationshipSortDSLQueryBuilderContributor.contribute(
			dslQueryBuilder,
			new RelationshipSort(
				objectDefinition, objectRelationship, path,
				relatedObjectDefinition, sort));
	}

}