/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.rest.internal.model.listener.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.object.constants.ObjectRelationshipConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.object.test.util.ObjectDefinitionTestUtil;
import com.liferay.object.test.util.ObjectRelationshipTestUtil;
import com.liferay.object.test.util.OpenAPIDocumentTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.vulcan.application.HeadlessApplicationProvider;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Petteri Karttunen
 */
@RunWith(Arquillian.class)
public class ObjectRelationshipModelListenerTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testOnAfterCreate() throws Exception {
		ObjectDefinition objectDefinition1 =
			ObjectDefinitionTestUtil.publishObjectDefinition();
		ObjectDefinition objectDefinition2 =
			ObjectDefinitionTestUtil.publishObjectDefinition();

		Assert.assertNotNull(
			OpenAPIDocumentTestUtil.getContent(
				objectDefinition1.getRESTContextPath(),
				_headlessApplicationProvider));
		Assert.assertNotNull(
			OpenAPIDocumentTestUtil.getContent(
				objectDefinition2.getRESTContextPath(),
				_headlessApplicationProvider));

		String name = "a" + RandomTestUtil.randomString();

		ObjectRelationshipTestUtil.addObjectRelationship(
			_objectRelationshipLocalService, objectDefinition1,
			objectDefinition2,
			ObjectRelationshipConstants.DELETION_TYPE_CASCADE, name);

		String content = OpenAPIDocumentTestUtil.getContent(
			objectDefinition1.getRESTContextPath(),
			_headlessApplicationProvider);

		Assert.assertTrue(content.contains(name));

		content = OpenAPIDocumentTestUtil.getContent(
			objectDefinition2.getRESTContextPath(),
			_headlessApplicationProvider);

		Assert.assertTrue(content.contains(name));
	}

	@Inject
	private HeadlessApplicationProvider _headlessApplicationProvider;

	@Inject
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

}