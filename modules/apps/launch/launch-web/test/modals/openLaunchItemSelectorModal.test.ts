/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

// eslint-disable-next-line @liferay/portal/no-cross-module-deep-import
import {checkAccessibility} from '@liferay/layout-js-components-web/test/__lib__/index';
import {act, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import fetchMock from 'jest-fetch-mock';

import '@testing-library/jest-dom';

import openLaunchItemSelectorModal from '../../src/main/resources/META-INF/resources/js/modals/openLaunchItemSelectorModal';

const LAUNCH = {
	description: 'Seasonal content',
	id: 42,
	name: 'Halloween Campaign',
	status: {code: 2},
};

function destroyRenderedComponents() {
	(Liferay.component as jest.Mock).mock.calls.forEach(([, component]) =>
		component?.destroy?.()
	);
}

function getRequestedFilter() {
	return new URL(fetchMock.mock.calls[0][0] as string).searchParams.get(
		'filter'
	);
}

async function openLaunchItemSelector(
	options: Partial<Parameters<typeof openLaunchItemSelectorModal>[0]> = {}
) {
	fetchMock.mockResponse(
		JSON.stringify({
			items: [LAUNCH],
			page: 1,
			pageSize: 20,
			totalCount: 1,
		})
	);

	const onSelect = jest.fn();

	act(() => {
		openLaunchItemSelectorModal({onSelect, ...options});
	});

	expect(await screen.findByText(LAUNCH.name)).toBeInTheDocument();

	return {onSelect};
}

describe('openLaunchItemSelectorModal', () => {
	beforeEach(() => {
		(Liferay.component as jest.Mock).mockClear();
	});

	afterEach(() => {
		act(() => {
			destroyRenderedComponents();
		});
	});

	it('lists the draft and scheduled launches', async () => {
		await openLaunchItemSelector();

		expect(getRequestedFilter()).toBe('status in (2, 7)');
	});

	it('excludes the given launches', async () => {
		await openLaunchItemSelector({excludeLaunchIds: [7, 8]});

		expect(getRequestedFilter()).toBe(
			"status in (2, 7) and not (id in ('7', '8'))"
		);
	});

	it('shows the launch columns', async () => {
		await openLaunchItemSelector();

		['name', 'description', 'status', 'scheduled-date'].forEach((label) =>
			expect(
				screen.getByRole('columnheader', {name: new RegExp(label)})
			).toBeInTheDocument()
		);

		expect(screen.getByText(LAUNCH.description)).toBeInTheDocument();
		expect(screen.getByText('in-progress')).toBeInTheDocument();
	});

	it('returns the selected launch', async () => {
		const {onSelect} = await openLaunchItemSelector();

		await userEvent.click(screen.getByRole('radio'));
		await userEvent.click(screen.getByText('add-to-launch'));

		await waitFor(() =>
			expect(onSelect).toHaveBeenCalledWith(
				expect.objectContaining({id: LAUNCH.id, name: LAUNCH.name})
			)
		);
	});

	it('closes without returning a launch when it is canceled', async () => {
		const {onSelect} = await openLaunchItemSelector();

		await userEvent.click(screen.getByText('cancel'));

		await waitFor(() =>
			expect(screen.queryByText(LAUNCH.name)).not.toBeInTheDocument()
		);
		expect(onSelect).not.toHaveBeenCalled();
	});

	it('has no accessibility violations', async () => {
		await openLaunchItemSelector();

		await checkAccessibility({context: screen.getByRole('dialog')});
	});
});
