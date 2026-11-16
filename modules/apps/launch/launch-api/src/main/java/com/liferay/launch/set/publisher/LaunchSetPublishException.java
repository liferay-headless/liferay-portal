/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.launch.set.publisher;

import com.liferay.portal.kernel.exception.PortalException;

/**
 * @author Alberto Moreno
 */
public class LaunchSetPublishException extends PortalException {

	public static class MustNotBePublished extends LaunchSetPublishException {

		public MustNotBePublished(long launchSetId) {
			super("Launch set " + launchSetId + " was already published");
		}

	}

	public static class MustNotBePublishing extends LaunchSetPublishException {

		public MustNotBePublishing(long launchSetId) {
			super("Launch set " + launchSetId + " is already being published");
		}

	}

	public static class MustNotBeScheduled extends LaunchSetPublishException {

		public MustNotBeScheduled(long launchSetId) {
			super(
				"Launch set " + launchSetId + " is scheduled to be published");
		}

	}

	private LaunchSetPublishException(String msg) {
		super(msg);
	}

}