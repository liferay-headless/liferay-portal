/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.discovery.jaxrs.application.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.kernel.test.util.HTTPTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Http;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Magdalena Jedraszak
 */
@DataGuard(scope = DataGuard.Scope.METHOD)
@RunWith(Arquillian.class)
public class HeadlessDiscoveryAPIApplicationTest {

	@Test
	@TestInfo("LPD-59421")
	public void test() throws Exception {
		Assert.assertEquals(
			403,
			HTTPTestUtil.invokeToHttpCode(
				null,
				"api?endpoint=http://attacker.local:8087/o/batch-planner/v1.0" +
					"/openapi.json",
				HashMapBuilder.put(
					"Accept", "text/html"
				).put(
					"Referer", "http://attacker.local:8087"
				).build(),
				Http.Method.GET));

		Assert.assertEquals(
			200,
			HTTPTestUtil.invokeToHttpCode(
				null,
				"api?endpoint=http://localhost:8080/o/batch-planner/v1.0" +
					"/openapi.json",
				HashMapBuilder.put(
					"Accept", "text/html"
				).put(
					"Referer", "http://localhost:8080"
				).build(),
				Http.Method.GET));
	}

}