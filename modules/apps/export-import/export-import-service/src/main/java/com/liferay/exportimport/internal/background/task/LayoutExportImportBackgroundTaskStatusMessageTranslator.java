/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.internal.background.task;

import com.liferay.portal.kernel.backgroundtask.BackgroundTaskStatus;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.util.GetterUtil;

import java.io.Serializable;

/**
 * @author Daniel Kocsis
 */
public class LayoutExportImportBackgroundTaskStatusMessageTranslator
	extends DefaultExportImportBackgroundTaskStatusMessageTranslator {

	@Override
	protected synchronized void translateLayoutMessage(
		BackgroundTaskStatus backgroundTaskStatus, Message message) {

		int phaseIndex = GetterUtil.getInteger(
			backgroundTaskStatus.getAttribute("phaseIndex"), -1);
		Serializable totalPhases = backgroundTaskStatus.getAttribute(
			"totalPhases");

		clearBackgroundTaskStatus(backgroundTaskStatus);

		backgroundTaskStatus.setAttribute("phaseIndex", phaseIndex + 1);

		if (totalPhases != null) {
			backgroundTaskStatus.setAttribute("totalPhases", totalPhases);
		}

		super.translateLayoutMessage(backgroundTaskStatus, message);
	}

}