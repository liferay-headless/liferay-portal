/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import fs from 'fs';
import path from 'path';

export class PortalLogChecker {
	private logFilePaths: string[] = [];

	constructor() {
		const liferayHome = process.env.LIFERAY_HOME;
		if (!liferayHome) {
			console.error('PortalLogChecker: LIFERAY_HOME is not defined.');

			return;
		}

		const logsDir = path.join(liferayHome, 'logs');
		this.logFilePaths = this.getXmlLogFiles(logsDir);
	}

	private getXmlLogFiles(dir: string): string[] {
		let results: string[] = [];
		if (!fs.existsSync(dir)) {
			return results;
		}

		const list = fs.readdirSync(dir);
		for (const file of list) {
			const filePath = path.join(dir, file);
			const stat = fs.statSync(filePath);

			if (stat && stat.isDirectory()) {
				results = results.concat(this.getXmlLogFiles(filePath));
			}
			else if (file.startsWith('liferay') && file.endsWith('.xml')) {
				results.push(filePath);
			}
		}

		return results;
	}

	async isConsoleTextPresent(text: string): Promise<boolean> {
		for (const filePath of this.logFilePaths) {
			if (!fs.existsSync(filePath)) {
				continue;
			}

			const rawContent = fs.readFileSync(filePath, 'utf8');

			const sanitizedContent = rawContent.replace(/log4j:/g, '');

			const eventRegex = /<event[\s\S]*?<\/event>/g;
			const events = sanitizedContent.match(eventRegex);

			if (!events) {
				continue;
			}

			for (const event of events) {
				const messageMatch = event.match(
					/<message>([\s\S]*?)<\/message>/
				);
				const messageText = messageMatch ? messageMatch[1] : '';

				const throwableMatch = event.match(
					/<throwable>([\s\S]*?)<\/throwable>/
				);
				const throwableText = throwableMatch ? throwableMatch[1] : '';

				const pattern = new RegExp(text);
				if (pattern.test(messageText) || pattern.test(throwableText)) {
					return true;
				}
			}
		}

		return false;
	}
}
