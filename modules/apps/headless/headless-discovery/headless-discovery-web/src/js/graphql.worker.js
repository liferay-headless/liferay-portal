/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

// eslint-disable-next-line @liferay/no-extraneous-dependencies, no-undef
const {initialize} = require('monaco-editor/esm/vs/editor/editor.worker');

// eslint-disable-next-line @liferay/no-extraneous-dependencies, no-undef
const {GraphQLWorker} = require('monaco-graphql/esm/GraphQLWorker.js');

const _consoleError = console.error;

console.error = (...args) => {
	if (args[0]?.message?.startsWith('Expected {')) {
		return;
	}

	_consoleError.apply(console, args);
};

globalThis.onmessage = () => {
	initialize((ctx, createData) => new GraphQLWorker(ctx, createData));
};
