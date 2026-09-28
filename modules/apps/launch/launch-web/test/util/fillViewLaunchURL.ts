/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import fillViewLaunchURL from '../../src/main/resources/META-INF/resources/js/util/fillViewLaunchURL';

const NAMESPACE = '_com_liferay_launch_web_portlet_LaunchPortlet_';

const VIEW_LAUNCH_URL = `/group/guest/~/control_panel/manage?p_p_id=com_liferay_launch_web_portlet_LaunchPortlet&${NAMESPACE}mvcRenderCommandName=%2Flaunch%2Fview_launch&${NAMESPACE}launchSetId=%7Bid%7D&${NAMESPACE}launchSetName=%7Bname%7D`;

describe('fillViewLaunchURL', () => {
	it('points the template at the launch that was just created', () => {
		const {searchParams} = new URL(
			fillViewLaunchURL(VIEW_LAUNCH_URL, {
				id: 38593,
				name: 'Christmas Campaign',
			})
		);

		expect(searchParams.get(`${NAMESPACE}launchSetId`)).toBe('38593');
		expect(searchParams.get(`${NAMESPACE}launchSetName`)).toBe(
			'Christmas Campaign'
		);
	});

	it('leaves the rest of the template alone', () => {
		const {searchParams} = new URL(
			fillViewLaunchURL(VIEW_LAUNCH_URL, {id: 1, name: 'Any'})
		);

		expect(searchParams.get(`${NAMESPACE}mvcRenderCommandName`)).toBe(
			'/launch/view_launch'
		);
	});

	it('escapes a name that would otherwise break out of the URL', () => {
		const {searchParams} = new URL(
			fillViewLaunchURL(VIEW_LAUNCH_URL, {
				id: 1,
				name: 'Black Friday & Cyber Monday',
			})
		);

		expect(searchParams.get(`${NAMESPACE}launchSetName`)).toBe(
			'Black Friday & Cyber Monday'
		);
	});
});
