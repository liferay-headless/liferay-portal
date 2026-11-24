/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {act, renderHook} from '@testing-library/react';

import {
	getLiveProcess,
	publishLiveProcess,
	useLiveProcess,
} from '../../../../src/main/resources/META-INF/resources/revamp/js/fds/liveProcesses';

function createProcess(id: number, label = 'Successful') {
	return {
		dateCompleted: '2026-11-24T10:00:00Z',
		id,
		status: {code: 3, label},
	};
}

describe('liveProcesses', () => {
	describe('useLiveProcess', () => {
		it('returns undefined without a process id', () => {
			const {result} = renderHook(() => useLiveProcess());

			expect(result.current).toBeUndefined();
		});

		it('returns undefined until the process is published', () => {
			const {result} = renderHook(() => useLiveProcess(1));

			expect(result.current).toBeUndefined();
		});

		it('returns the process once it is published', () => {
			const {result} = renderHook(() => useLiveProcess(2));

			const process = createProcess(2);

			act(() => {
				publishLiveProcess(process);
			});

			expect(result.current).toBe(process);
		});

		it('returns a process published before the mount', () => {
			const process = createProcess(3);

			publishLiveProcess(process);

			const {result} = renderHook(() => useLiveProcess(3));

			expect(result.current).toBe(process);
		});

		it('returns the latest published version of the process', () => {
			const {result} = renderHook(() => useLiveProcess(4));

			act(() => {
				publishLiveProcess(createProcess(4, 'Failed'));
			});

			const process = createProcess(4);

			act(() => {
				publishLiveProcess(process);
			});

			expect(result.current).toBe(process);
		});

		it('does not render again when another process is published', () => {
			let renderCount = 0;

			renderHook(() => {
				renderCount++;

				return useLiveProcess(5);
			});

			const initialRenderCount = renderCount;

			act(() => {
				publishLiveProcess(createProcess(6));
			});

			expect(renderCount).toBe(initialRenderCount);
		});

		it('follows the new process when the id changes', () => {
			const process = createProcess(7);

			publishLiveProcess(process);

			const {rerender, result} = renderHook(
				({processId}) => useLiveProcess(processId),
				{initialProps: {processId: 8}}
			);

			expect(result.current).toBeUndefined();

			rerender({processId: 7});

			expect(result.current).toBe(process);
		});
	});

	describe('getLiveProcess', () => {
		it('returns the published process', () => {
			const process = createProcess(9);

			publishLiveProcess(process);

			expect(getLiveProcess(9)).toBe(process);
		});
	});
});
