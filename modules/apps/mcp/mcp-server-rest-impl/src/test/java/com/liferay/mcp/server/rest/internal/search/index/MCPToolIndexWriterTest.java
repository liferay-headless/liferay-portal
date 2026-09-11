/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.search.index;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Petteri Karttunen
 */
public class MCPToolIndexWriterTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_companyId = RandomTestUtil.randomLong();

		_mcpToolIndexWriter = new MCPToolIndexWriter();

		_mcpToolIndexWriter.invalidate(_companyId);

		_getStaleToolSetsMap().clear();
	}

	@Test
	public void testClearStaleToolSet() throws Exception {
		String toolSetName = RandomTestUtil.randomString();

		_mcpToolIndexWriter.invalidate(_companyId, toolSetName);

		_clearStaleToolSet(toolSetName, _getStaleToolSet(toolSetName));

		Map<String, Object> staleToolSets = _getStaleToolSets();

		Assert.assertTrue(staleToolSets.isEmpty());
	}

	@Test
	public void testClearStaleToolSetWhenTheEntryIsReplaced() throws Exception {
		String toolSetName = RandomTestUtil.randomString();

		_mcpToolIndexWriter.invalidate(_companyId, toolSetName);

		Object staleToolSet = _getStaleToolSet(toolSetName);

		Assert.assertNotNull(staleToolSet);

		_mcpToolIndexWriter.invalidate(_companyId, toolSetName);

		_clearStaleToolSet(toolSetName, staleToolSet);

		Assert.assertTrue(_isStaleToolSet(toolSetName));
	}

	@Test
	public void testClearStaleToolSetWithTheAllToolSetsEntry()
		throws Exception {

		_mcpToolIndexWriter.invalidate(_companyId);

		_clearStaleToolSet(StringPool.STAR, _getStaleToolSet(StringPool.STAR));

		Map<String, Object> staleToolSets = _getStaleToolSets();

		Assert.assertTrue(staleToolSets.isEmpty());
	}

	@Test
	public void testClearStaleToolSetWithTheAllToolSetsEntryWhenTheEntryIsReplaced()
		throws Exception {

		_mcpToolIndexWriter.invalidate(_companyId);

		Object staleToolSet = _getStaleToolSet(StringPool.STAR);

		_mcpToolIndexWriter.invalidate(_companyId);

		_clearStaleToolSet(StringPool.STAR, staleToolSet);

		Assert.assertTrue(_isStaleToolSet(StringPool.STAR));
	}

	@Test
	public void testInvalidateWithABlankToolSetName() throws Exception {
		_mcpToolIndexWriter.invalidate(_companyId, null);

		Map<String, Object> staleToolSets = _getStaleToolSets();

		Assert.assertTrue(staleToolSets.containsKey(StringPool.STAR));
	}

	@Test
	public void testInvalidateWithAToolSetName() throws Exception {
		String toolSetName = RandomTestUtil.randomString();

		_mcpToolIndexWriter.invalidate(_companyId, toolSetName);

		Assert.assertTrue(_isStaleToolSet(toolSetName));
	}

	@Test
	public void testInvalidateWithoutAToolSetName() throws Exception {
		Set<String> toolSetNames = Collections.singleton(
			RandomTestUtil.randomString());

		_setIndexedToolSetNames(toolSetNames);

		_mcpToolIndexWriter.invalidate(_companyId);

		Map<String, Object> staleToolSets = _getStaleToolSets();

		Assert.assertEquals(toolSetNames, _getIndexedToolSetNames());
		Assert.assertTrue(staleToolSets.containsKey(StringPool.STAR));
	}

	@Test
	public void testRemoveIndexState() throws Exception {
		Assert.assertNotNull(_getMCPToolIndexState());

		_mcpToolIndexWriter.removeIndexState(_companyId);

		Assert.assertNull(_getMCPToolIndexState());
	}

	@Test
	public void testSetIndexedToolSetNames() throws Exception {
		_setIndexedToolSetNames(
			Collections.singleton(RandomTestUtil.randomString()));

		Set<String> toolSetNames = Collections.singleton(
			RandomTestUtil.randomString());

		_setIndexedToolSetNames(toolSetNames);

		Assert.assertEquals(toolSetNames, _getIndexedToolSetNames());
	}

	private void _clearStaleToolSet(String toolSetName, Object staleToolSet) {
		ReflectionTestUtil.invoke(
			_getMCPToolIndexState(), "clearStaleToolSet",
			new Class<?>[] {String.class, Object.class}, toolSetName,
			staleToolSet);
	}

	private Set<String> _getIndexedToolSetNames() {
		return ReflectionTestUtil.invoke(
			_getMCPToolIndexState(), "getIndexedToolSetNames", new Class<?>[0]);
	}

	private Object _getMCPToolIndexState() {
		Map<Long, Object> companyIndexStates = ReflectionTestUtil.getFieldValue(
			_mcpToolIndexWriter, "_companyIndexStates");

		return companyIndexStates.get(_companyId);
	}

	private Object _getStaleToolSet(String toolSetName) {
		Map<String, Object> staleToolSets = _getStaleToolSets();

		return staleToolSets.get(toolSetName);
	}

	private Map<String, Object> _getStaleToolSets() {
		return ReflectionTestUtil.invoke(
			_getMCPToolIndexState(), "getStaleToolSets", new Class<?>[0]);
	}

	private Map<String, Object> _getStaleToolSetsMap() {
		return ReflectionTestUtil.getFieldValue(
			_getMCPToolIndexState(), "_staleToolSets");
	}

	private boolean _isStaleToolSet(String toolSetName) {
		Map<String, Object> staleToolSets = _getStaleToolSets();

		return staleToolSets.containsKey(toolSetName);
	}

	private void _setIndexedToolSetNames(Set<String> toolSetNames) {
		ReflectionTestUtil.invoke(
			_getMCPToolIndexState(), "setIndexedToolSetNames",
			new Class<?>[] {Set.class}, toolSetNames);
	}

	private long _companyId;
	private MCPToolIndexWriter _mcpToolIndexWriter;

}