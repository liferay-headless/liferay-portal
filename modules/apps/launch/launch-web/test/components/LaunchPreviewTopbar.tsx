/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import fetch from 'jest-fetch-mock';
import React from 'react';

import '@testing-library/jest-dom';

import LaunchPreviewTopbar from '../../src/main/resources/META-INF/resources/js/components/LaunchPreviewTopbar';

const DEFAULT_PROPS = {
	launchSetId: 1,
	noLaunchURL: '/web/guest/home?p_l_mode=preview&previewLaunchSetId=0',
};

function mockLaunch() {
	fetch.mockResponseOnce(JSON.stringify({id: 1, name: 'Christmas Campaign'}));
}

describe('LaunchPreviewTopbar', () => {
	it('names the launch the site is being previewed with', async () => {
		mockLaunch();

		render(<LaunchPreviewTopbar {...DEFAULT_PROPS} />);

		expect(
			await screen.findByText('Christmas Campaign')
		).toBeInTheDocument();
		expect(screen.getByText('previewing')).toBeInTheDocument();
	});

	it('switches the launch the site is previewed with', async () => {
		mockLaunch();

		render(<LaunchPreviewTopbar {...DEFAULT_PROPS} />);

		fetch.mockResponseOnce(
			JSON.stringify({
				items: [
					{id: 1, name: 'Christmas Campaign'},
					{id: 2, name: 'Spring Campaign'},
				],
			})
		);

		await userEvent.click(await screen.findByText('Christmas Campaign'));

		expect(await screen.findByText('Spring Campaign')).toBeInTheDocument();
	});

	it('offers the page as the site serves it alongside the launches', async () => {
		mockLaunch();

		render(<LaunchPreviewTopbar {...DEFAULT_PROPS} />);

		fetch.mockResponseOnce(JSON.stringify({items: []}));

		await userEvent.click(await screen.findByText('Christmas Campaign'));

		expect(
			await screen.findByRole('menuitem', {name: 'no-launch'})
		).toHaveAttribute(
			'href',
			'/web/guest/home?p_l_mode=preview&previewLaunchSetId=0'
		);
	});

	it('stands over the live page when no launch is picked', async () => {
		render(<LaunchPreviewTopbar {...DEFAULT_PROPS} launchSetId={0} />);

		expect(screen.getByText('previewing')).toBeInTheDocument();
		expect(
			screen.getByRole('button', {name: 'no-launch'})
		).toBeInTheDocument();
		expect(fetch).not.toHaveBeenCalled();
	});
});
