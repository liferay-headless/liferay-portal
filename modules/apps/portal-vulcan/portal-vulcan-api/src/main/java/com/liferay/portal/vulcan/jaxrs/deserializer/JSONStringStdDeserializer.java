/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.jaxrs.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

/**
 * @author Sergio Jimenez del Coso
 */
public class JSONStringStdDeserializer extends StdDeserializer<String> {

	public JSONStringStdDeserializer() {
		super(String.class);
	}

	@Override
	public String deserialize(
			JsonParser jsonParser,
			DeserializationContext deserializationContext)
		throws IOException {

		TreeNode treeNode = jsonParser.readValueAsTree();

		return treeNode.toString();
	}

}