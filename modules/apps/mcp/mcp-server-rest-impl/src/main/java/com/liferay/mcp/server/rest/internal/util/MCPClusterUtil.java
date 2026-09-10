/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.util;

import com.liferay.mcp.server.rest.internal.search.index.MCPToolIndexWriter;
import com.liferay.portal.kernel.cluster.ClusterExecutorUtil;
import com.liferay.portal.kernel.cluster.ClusterRequest;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.transaction.TransactionCallbackUtil;
import com.liferay.portal.kernel.util.MethodHandler;
import com.liferay.portal.kernel.util.MethodKey;

/**
 * @author Petteri Karttunen
 */
public class MCPClusterUtil {

	public static void invalidate(long companyId, String restContextPath) {
		OpenAPIBriefUtil.clearOpenAPIJSONObjectCache(
			companyId, restContextPath);

		if (!SearchToolUtil.isToolSearchSupported()) {
			return;
		}

		MCPToolIndexWriter mcpToolIndexWriter =
			_mcpToolIndexWriterSnapshot.get();

		if (mcpToolIndexWriter == null) {
			return;
		}

		mcpToolIndexWriter.invalidate(
			companyId,
			OpenAPIBriefUtil.getToolSetName(companyId, restContextPath));
	}

	public static void notifyCluster(long companyId, String restContextPath) {
		if (!ClusterExecutorUtil.isEnabled()) {
			return;
		}

		TransactionCallbackUtil.registerCommitCallback(
			() -> {
				ClusterRequest clusterRequest =
					ClusterRequest.createMulticastRequest(
						new MethodHandler(
							_invalidateMethodKey, companyId, restContextPath),
						true);

				clusterRequest.setFireAndForget(true);

				ClusterExecutorUtil.execute(clusterRequest);

				return null;
			});
	}

	private static final MethodKey _invalidateMethodKey = new MethodKey(
		MCPClusterUtil.class, "invalidate", long.class, String.class);
	private static final Snapshot<MCPToolIndexWriter>
		_mcpToolIndexWriterSnapshot = new Snapshot<>(
			MCPClusterUtil.class, MCPToolIndexWriter.class);

}