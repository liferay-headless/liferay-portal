/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.model.listener;

import com.liferay.mcp.server.rest.internal.cache.MCPServerCacheManager;
import com.liferay.mcp.server.rest.internal.search.index.MCPToolIndexInvalidator;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Petteri Karttunen
 */
public class ObjectDefinitionModelListenerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		ReflectionTestUtil.setFieldValue(
			_objectDefinitionModelListener, "_mcpServerCacheManager",
			_mcpServerCacheManager);
		ReflectionTestUtil.setFieldValue(
			_objectDefinitionModelListener, "_mcpToolIndexInvalidator",
			_mcpToolIndexInvalidator);
	}

	@Test
	public void testOnAfterCreate() {
		_objectDefinitionModelListener.onAfterCreate(
			_mockObjectDefinition(true));

		_assertToolSetInvalidated();
	}

	@Test
	public void testOnAfterCreateWhenObjectDefinitionIsNotApproved() {
		_objectDefinitionModelListener.onAfterCreate(
			_mockObjectDefinition(false));

		_assertToolSetNotInvalidated();
	}

	@Test
	public void testOnAfterRemove() {
		_objectDefinitionModelListener.onAfterRemove(
			_mockObjectDefinition(true));

		_assertToolSetInvalidated();
	}

	@Test
	public void testOnAfterRemoveWhenObjectDefinitionIsNotApproved() {
		_objectDefinitionModelListener.onAfterRemove(
			_mockObjectDefinition(false));

		_assertToolSetNotInvalidated();
	}

	@Test
	public void testOnAfterUpdate() {
		_objectDefinitionModelListener.onAfterUpdate(
			_mockObjectDefinition(false), _mockObjectDefinition(true));

		_assertToolSetInvalidated();
	}

	@Test
	public void testOnAfterUpdateWhenObjectDefinitionIsNotApproved() {
		_objectDefinitionModelListener.onAfterUpdate(
			_mockObjectDefinition(false), _mockObjectDefinition(false));

		_assertToolSetNotInvalidated();
	}

	private void _assertToolSetInvalidated() {
		Mockito.verify(
			_mcpServerCacheManager
		).clearOpenAPIJSONObjectCache(
			_COMPANY_ID
		);

		Mockito.verify(
			_mcpToolIndexInvalidator
		).invalidate(
			_COMPANY_ID, _REST_CONTEXT_PATH
		);
	}

	private void _assertToolSetNotInvalidated() {
		Mockito.verify(
			_mcpServerCacheManager, Mockito.never()
		).clearOpenAPIJSONObjectCache(
			Mockito.anyLong()
		);

		Mockito.verify(
			_mcpToolIndexInvalidator, Mockito.never()
		).invalidate(
			Mockito.anyLong(), Mockito.anyString()
		);
	}

	private ObjectDefinition _mockObjectDefinition(boolean approved) {
		ObjectDefinition objectDefinition = Mockito.mock(
			ObjectDefinition.class);

		Mockito.when(
			objectDefinition.getCompanyId()
		).thenReturn(
			_COMPANY_ID
		);

		Mockito.when(
			objectDefinition.getRESTContextPath()
		).thenReturn(
			_REST_CONTEXT_PATH
		);

		Mockito.when(
			objectDefinition.isApproved()
		).thenReturn(
			approved
		);

		return objectDefinition;
	}

	private static final long _COMPANY_ID = RandomTestUtil.randomLong();

	private static final String _REST_CONTEXT_PATH =
		"/c/" + RandomTestUtil.randomString();

	private final MCPServerCacheManager _mcpServerCacheManager = Mockito.mock(
		MCPServerCacheManager.class);
	private final MCPToolIndexInvalidator _mcpToolIndexInvalidator =
		Mockito.mock(MCPToolIndexInvalidator.class);
	private final ObjectDefinitionModelListener _objectDefinitionModelListener =
		new ObjectDefinitionModelListener();

}