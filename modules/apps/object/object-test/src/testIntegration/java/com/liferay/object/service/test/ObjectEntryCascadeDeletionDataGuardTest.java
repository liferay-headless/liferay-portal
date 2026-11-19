/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.object.constants.ObjectRelationshipConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectRelationshipLocalServiceUtil;
import com.liferay.object.test.util.ObjectDefinitionTestUtil;
import com.liferay.object.test.util.ObjectEntryTestUtil;
import com.liferay.object.test.util.ObjectRelationshipTestUtil;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Carlos Correa
 */
@DataGuard(scope = DataGuard.Scope.METHOD)
@RunWith(Arquillian.class)
public class ObjectEntryCascadeDeletionDataGuardTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@BeforeClass
	public static void setUpClass() throws Exception {
		_objectDefinition1 = ObjectDefinitionTestUtil.publishObjectDefinition();
		_objectDefinition2 = ObjectDefinitionTestUtil.publishObjectDefinition();

		_objectRelationship = ObjectRelationshipTestUtil.addObjectRelationship(
			ObjectRelationshipLocalServiceUtil.getService(), _objectDefinition1,
			_objectDefinition2,
			ObjectRelationshipConstants.DELETION_TYPE_CASCADE);
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		long count = _count();

		ObjectDefinitionLocalServiceUtil.deleteObjectDefinition(
			_objectDefinition2);
		ObjectDefinitionLocalServiceUtil.deleteObjectDefinition(
			_objectDefinition1);

		Assert.assertEquals(
			"Object entry rows left in " + _objectDefinition1.getDBTableName() +
				" after the cleanup",
			0, count);
	}

	@Test
	public void test() throws Exception {
		ObjectEntry objectEntry1 = ObjectEntryTestUtil.addObjectEntry(
			_objectDefinition1);
		ObjectEntry objectEntry2 = ObjectEntryTestUtil.addObjectEntry(
			_objectDefinition2);

		ObjectRelationshipTestUtil.relateObjectEntries(
			objectEntry1.getObjectEntryId(), objectEntry2.getObjectEntryId(),
			_objectRelationship, TestPropsValues.getUserId());
	}

	private static long _count() throws Exception {
		try (Connection connection = DataAccess.getConnection();

			PreparedStatement preparedStatement = connection.prepareStatement(
				"select count(*) as count from " +
					_objectDefinition1.getDBTableName());

			ResultSet resultSet = preparedStatement.executeQuery()) {

			resultSet.next();

			return resultSet.getLong("count");
		}
	}

	private static ObjectDefinition _objectDefinition1;
	private static ObjectDefinition _objectDefinition2;
	private static ObjectRelationship _objectRelationship;

}