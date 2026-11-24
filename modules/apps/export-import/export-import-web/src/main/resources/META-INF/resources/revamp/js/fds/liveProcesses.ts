/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {useSyncExternalStore} from 'react';

export interface Process {
	dateCompleted?: string;
	errorMessage?: string;
	id: number;
	name?: string;
	status: {
		code: number;
		label: string;
		label_i18n?: string;
	};
}

const listeners = new Set<() => void>();
const processes = new Map<number, Process>();

function subscribe(listener: () => void) {
	listeners.add(listener);

	return () => {
		listeners.delete(listener);
	};
}

export function getLiveProcess(processId: number): Process | undefined {
	return processes.get(processId);
}

export function publishLiveProcess(process: Process) {
	processes.set(process.id, process);

	listeners.forEach((listener) => listener());
}

export function useLiveProcess(processId?: number): Process | undefined {
	return useSyncExternalStore(subscribe, () =>
		processId ? processes.get(processId) : undefined
	);
}
