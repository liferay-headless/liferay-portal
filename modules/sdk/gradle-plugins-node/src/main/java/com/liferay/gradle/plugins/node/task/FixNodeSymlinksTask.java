/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.gradle.plugins.node.task;

import com.liferay.gradle.plugins.node.internal.NodeExecutor;
import com.liferay.gradle.plugins.node.internal.util.NodePluginUtil;
import com.liferay.gradle.util.OSDetector;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

/**
 * @author Alejandro Tardín
 */
public class FixNodeSymlinksTask extends DefaultTask {

	public FixNodeSymlinksTask() {
		_nodeExecutor = new NodeExecutor(getProject());
	}

	@TaskAction
	public void fixNodeSymlinks() throws IOException {
		if (!OSDetector.isWindows()) {
			File nodeDir = _nodeExecutor.getNodeDir();

			File binDir = new File(nodeDir, "bin");

			Path binDirPath = binDir.toPath();

			Path linkPath = binDirPath.resolve("npm");

			Files.deleteIfExists(linkPath);

			File npmDir = NodePluginUtil.getNpmDir(nodeDir);

			File linkTargetFile = new File(npmDir, "bin/npm-cli.js");

			Files.createSymbolicLink(linkPath, linkTargetFile.toPath());
		}
	}

	public void setNodeDir(Object nodeDir) {
		_nodeExecutor.setNodeDir(nodeDir);
	}

	private final NodeExecutor _nodeExecutor;

}