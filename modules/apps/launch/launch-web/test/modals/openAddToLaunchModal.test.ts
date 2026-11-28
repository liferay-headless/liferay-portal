/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {act, screen, waitFor} from '@testing-library/react';
import fetchMock from 'jest-fetch-mock';

import '@testing-library/jest-dom';

import {Launch} from '../../src/main/resources/META-INF/resources/js/api/launches';
import openAddToLaunchModal from '../../src/main/resources/META-INF/resources/js/modals/openAddToLaunchModal';

const mockOpenItemSelectorModal = jest.fn();

jest.mock('@liferay/frontend-js-item-selector-web', () => ({
	openItemSelectorModal: (props: unknown) => mockOpenItemSelectorModal(props),
}));

const ASSET = {
	className: 'com.liferay.journal.model.JournalArticle',
	classPK: 1001,
	classVersion: '1.0',
};

const LAUNCH: Launch = {
	id: 42,
	name: 'Halloween Campaign',
	status: {code: 2},
};

function getLaunchItemSelectorProps() {
	return mockOpenItemSelectorModal.mock.calls.at(-1)[0];
}

function mockLaunchEntries(launchSetIds: number[]) {
	fetchMock.mockResponseOnce(
		JSON.stringify({
			items: launchSetIds.map((launchSetId) => ({
				r_launchSetToLaunchEntries_c_launchSetId: launchSetId,
			})),
		})
	);
}

async function selectLaunch(launch: Launch) {
	await act(async () => {
		getLaunchItemSelectorProps().onItemsChange([launch]);
	});
}

describe('openAddToLaunchModal', () => {
	beforeEach(() => {
		jest.clearAllMocks();

		fetchMock.resetMocks();
	});

	it('opens the Launch selector without the launches that already contain the asset', async () => {
		mockLaunchEntries([7, 8]);

		await openAddToLaunchModal(ASSET);

		expect(decodeURIComponent(fetchMock.mock.calls[0][0] as string)).toBe(
			"/o/launch-entries?filter=className eq 'com.liferay.journal.model.JournalArticle' and classPK eq 1001&pageSize=-1"
		);
		expect(decodeURIComponent(getLaunchItemSelectorProps().apiURL)).toBe(
			"/o/launch-sets?filter=status in (2, 7) and not (id in ('7', '8'))"
		);
	});

	it('opens a single Launch selector while the launch entries load', async () => {
		mockLaunchEntries([]);

		const promise = openAddToLaunchModal(ASSET);

		await openAddToLaunchModal(ASSET);

		await promise;

		expect(mockOpenItemSelectorModal).toHaveBeenCalledTimes(1);
		expect(fetchMock).toHaveBeenCalledTimes(1);
	});

	it('adds the asset to the selected launch and calls onAdd with the launch and its entry', async () => {
		const onAdd = jest.fn();

		mockLaunchEntries([]);

		await openAddToLaunchModal(ASSET, {onAdd});

		fetchMock.mockResponseOnce(JSON.stringify({id: 1}));

		await selectLaunch(LAUNCH);

		await waitFor(() =>
			expect(onAdd).toHaveBeenCalledWith({
				launch: LAUNCH,
				launchEntry: {id: 1},
			})
		);

		const [url, init] = fetchMock.mock.calls[1];

		expect(url).toBe('/o/launch-entries');
		expect(init?.method).toBe('POST');
		expect(JSON.parse(init?.body as string)).toEqual({
			baseVersionKey: '1.0',
			className: ASSET.className,
			classPK: ASSET.classPK,
			r_launchSetToLaunchEntries_c_launchSetId: 42,
		});
		expect(
			await screen.findByText('the-item-was-added-to-x')
		).toBeInTheDocument();
	});

	it('shows an error and does not call onAdd when the asset cannot be added', async () => {
		const onAdd = jest.fn();

		mockLaunchEntries([]);

		await openAddToLaunchModal(ASSET, {onAdd});

		fetchMock.mockResponseOnce(JSON.stringify({title: 'unable-to-add'}), {
			status: 400,
		});

		await selectLaunch(LAUNCH);

		expect(await screen.findByText('unable-to-add')).toBeInTheDocument();
		expect(onAdd).not.toHaveBeenCalled();
	});

	it('ignores another call while the asset is being added', async () => {
		const onAdd = jest.fn();

		mockLaunchEntries([]);

		await openAddToLaunchModal(ASSET, {onAdd});

		let respond: (body: string) => void = () => {};

		fetchMock.mockResponseOnce(
			() =>
				new Promise((resolve) => {
					respond = resolve;
				})
		);

		await selectLaunch(LAUNCH);

		await openAddToLaunchModal(ASSET);

		expect(mockOpenItemSelectorModal).toHaveBeenCalledTimes(1);

		await act(async () => {
			respond(JSON.stringify({id: 1}));
		});

		await waitFor(() => expect(onAdd).toHaveBeenCalled());
		expect(fetchMock).toHaveBeenCalledTimes(2);

		mockLaunchEntries([]);

		await openAddToLaunchModal(ASSET);

		expect(mockOpenItemSelectorModal).toHaveBeenCalledTimes(2);
		expect(fetchMock).toHaveBeenCalledTimes(3);
	});

	it('does not open the Launch selector when the launch entries cannot be loaded', async () => {
		fetchMock.mockResponseOnce('', {status: 500});

		await act(async () => {
			await openAddToLaunchModal(ASSET);
		});

		expect(mockOpenItemSelectorModal).not.toHaveBeenCalled();
		expect(
			await screen.findByText('unable-to-list-launch-entries')
		).toBeInTheDocument();
	});
});
