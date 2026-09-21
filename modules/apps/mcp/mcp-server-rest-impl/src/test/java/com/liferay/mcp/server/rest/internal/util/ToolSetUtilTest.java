/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.util;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Petteri Karttunen
 */
public class ToolSetUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testToRequiredInputSchema() {
		Map<String, Object> requiredInputSchema = _toRequiredInputSchema(
			_getInputSchema(
				HashMapBuilder.<String, Object>put(
					"body", _getProperty("object")
				).put(
					"siteId", _getProperty("string")
				).put(
					"sort", _getProperty("string")
				).build(),
				ListUtil.fromArray("body", "siteId")));

		Map<String, Object> properties = _getProperties(requiredInputSchema);

		Assert.assertEquals(
			properties.toString(), ListUtil.fromArray("body", "siteId"),
			ListUtil.fromCollection(properties.keySet()));

		Assert.assertEquals(
			ListUtil.fromArray("body", "siteId"),
			requiredInputSchema.get("required"));
	}

	@Test
	public void testToRequiredInputSchemaWhenABodyHasNoRequiredProperties() {
		Map<String, Object> requiredInputSchema = _toRequiredInputSchema(
			_getInputSchema(
				HashMapBuilder.<String, Object>put(
					"body",
					_getInputSchema(
						HashMapBuilder.<String, Object>put(
							"description", _getProperty("string")
						).put(
							"name", _getProperty("string")
						).build(),
						Collections.emptyList())
				).build(),
				Collections.singletonList("body")));

		Map<String, Object> requiredBody = (Map<String, Object>)_getProperties(
			requiredInputSchema
		).get(
			"body"
		);

		Map<String, Object> bodyProperties = _getProperties(requiredBody);

		Assert.assertEquals(
			bodyProperties.toString(),
			ListUtil.fromArray("description", "name"),
			ListUtil.sort(ListUtil.fromCollection(bodyProperties.keySet())));
	}

	@Test
	public void testToRequiredInputSchemaWhenARequiredPropertyIsMissing() {
		Map<String, Object> requiredInputSchema = _toRequiredInputSchema(
			_getInputSchema(
				HashMapBuilder.<String, Object>put(
					"body", _getProperty("object")
				).build(),
				ListUtil.fromArray("body", "siteId")));

		Map<String, Object> properties = _getProperties(requiredInputSchema);

		Assert.assertEquals(
			properties.toString(), Collections.singleton("body"),
			properties.keySet());

		Assert.assertEquals(
			Collections.singletonList("body"),
			requiredInputSchema.get("required"));
	}

	@Test
	public void testToRequiredInputSchemaWhenAnObjectBodyHasNoRequiredProperties() {
		Map<String, Object> requiredInputSchema = _toRequiredInputSchema(
			_getInputSchema(
				HashMapBuilder.<String, Object>put(
					"body",
					() -> {
						Map<String, Object> body = _getInputSchema(
							HashMapBuilder.<String, Object>put(
								"description", _getProperty("string")
							).put(
								"name", _getProperty("string")
							).build(),
							Collections.emptyList());

						body.put("description", "The entry to create");

						return body;
					}
				).build(),
				Collections.singletonList("body")),
			true);

		Map<String, Object> properties = _getProperties(requiredInputSchema);

		Map<String, Object> requiredBody = (Map<String, Object>)properties.get(
			"body");

		Assert.assertEquals(
			requiredBody.toString(), "The entry to create",
			requiredBody.get("description"));
		Assert.assertEquals(
			requiredBody.toString(), "object", requiredBody.get("type"));
		Assert.assertFalse(
			requiredBody.toString(), requiredBody.containsKey("properties"));
	}

	@Test
	public void testToRequiredInputSchemaWhenAnObjectPicklistHasNoRequiredProperties() {
		Map<String, Object> requiredInputSchema = _toRequiredInputSchema(
			_getInputSchema(
				HashMapBuilder.<String, Object>put(
					"body",
					_getInputSchema(
						HashMapBuilder.<String, Object>put(
							"name", _getProperty("string")
						).put(
							"status",
							_getInputSchema(
								HashMapBuilder.<String, Object>put(
									"key", _getProperty("string")
								).put(
									"name", _getProperty("string")
								).build(),
								Collections.emptyList())
						).build(),
						ListUtil.fromArray("name", "status"))
				).build(),
				Collections.singletonList("body")),
			true);

		Map<String, Object> requiredBody = (Map<String, Object>)_getProperties(
			requiredInputSchema
		).get(
			"body"
		);

		Map<String, Object> requiredStatus =
			(Map<String, Object>)_getProperties(
				requiredBody
			).get(
				"status"
			);

		Map<String, Object> statusProperties = _getProperties(requiredStatus);

		Assert.assertEquals(
			statusProperties.toString(), ListUtil.fromArray("key", "name"),
			ListUtil.sort(ListUtil.fromCollection(statusProperties.keySet())));
	}

	@Test
	public void testToRequiredInputSchemaWhenNoRequiredPropertyIsPresent() {
		Map<String, Object> requiredInputSchema = _toRequiredInputSchema(
			_getInputSchema(
				HashMapBuilder.<String, Object>put(
					"body", _getProperty("object")
				).build(),
				Collections.singletonList("siteId")));

		Map<String, Object> properties = _getProperties(requiredInputSchema);

		Assert.assertTrue(properties.toString(), properties.isEmpty());

		Assert.assertFalse(requiredInputSchema.containsKey("required"));
	}

	private Map<String, Object> _getInputSchema(
		Map<String, Object> properties, List<String> requiredPropertyNames) {

		return HashMapBuilder.<String, Object>put(
			"properties", properties
		).put(
			"required", requiredPropertyNames
		).put(
			"type", "object"
		).build();
	}

	private Map<String, Object> _getProperties(
		Map<String, Object> requiredInputSchema) {

		return (Map<String, Object>)requiredInputSchema.get("properties");
	}

	private Map<String, Object> _getProperty(String type) {
		return HashMapBuilder.<String, Object>put(
			"type", type
		).build();
	}

	private Map<String, Object> _toRequiredInputSchema(
		Map<String, Object> inputSchema) {

		return _toRequiredInputSchema(inputSchema, false);
	}

	private Map<String, Object> _toRequiredInputSchema(
		Map<String, Object> inputSchema, boolean objectToolSet) {

		return ReflectionTestUtil.invoke(
			ToolSetUtil.class, "_toRequiredInputSchema",
			new Class<?>[] {Map.class, boolean.class, String.class},
			inputSchema, objectToolSet, "postSiteBlogPosting");
	}

}