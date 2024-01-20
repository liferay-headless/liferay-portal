/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import * as os from 'node:os'; // eslint-disable-line @liferay/no-extraneous-dependencies
import * as path from 'path';
import {zip} from 'zip-a-folder';

export function formatPlural(s: string): string {
	if (!s) {
		return s;
	}
	if (
		s.endsWith(`ch`) ||
		s.endsWith(`s`) ||
		s.endsWith(`sh`) ||
		s.endsWith(`x`) ||
		s.endsWith(`z`)
	) {
		return s + `es`;
	}
	if (
		s.endsWith(`y`) &&
		!s.endsWith(`ay`) &&
		!s.endsWith(`ey`) &&
		!s.endsWith(`oy`) &&
		!s.endsWith(`uy`)
	) {
		return s.substring(0, s.length - 1) + `ies`;
	}

	return s + `s`;
}

export function getRandomInt(): number {
	return Math.floor(Math.random() * 9999999999);
}

export async function zipFolder(folderPath: string) {
	const tempFilePath = path.join(os.tmpdir(), path.basename(folderPath));
	await zip(folderPath, tempFilePath);

	return tempFilePath;
}
