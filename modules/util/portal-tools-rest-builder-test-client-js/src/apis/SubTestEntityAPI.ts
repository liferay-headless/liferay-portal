/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ObjectSerializer} from '../utils/SerDes';

		import {SubTestEntity} from '../models/SubTestEntity';

/**
 * @author Alejandro Tardín
 * @generated
 */

export class SubTestEntityAPI {
	protected _basePath: string;
	protected _defaultHeaders: any = {};

	constructor(basePath?: string) {
		if (basePath) {
			this._basePath = basePath;
		}
	}

	set defaultHeaders(defaultHeaders: any) {
		this._defaultHeaders = defaultHeaders;
	}

		/**
		 * 
				 * @param ercSiteTestEntityExternalReferenceCode
				 * @param siteExternalReferenceCode
				 * @param subTestEntityExternalReferenceCode
		 * @param headers Optional custom request headers
		 */
		public async getSiteByExternalReferenceCodeErcSiteTestEntityByExternalReferenceCodeSubTestEntityByExternalReferenceCode(
						ercSiteTestEntityExternalReferenceCode: string,
						siteExternalReferenceCode: string,
						subTestEntityExternalReferenceCode: string,
			headers?: {[name: string]: string},
		): Promise<{
				body: SubTestEntity;
			response: Response;
		}> {

			const path = this._basePath + "/test/v1.0/sites/{siteExternalReferenceCode}/erc-site-test-entities/{ercSiteTestEntityExternalReferenceCode}/sub-test-entities/{subTestEntityExternalReferenceCode}"
						.replace("{ercSiteTestEntityExternalReferenceCode}",encodeURIComponent(ercSiteTestEntityExternalReferenceCode))
										.replace("{siteExternalReferenceCode}",encodeURIComponent(siteExternalReferenceCode))
										.replace("{subTestEntityExternalReferenceCode}",encodeURIComponent(subTestEntityExternalReferenceCode))
				;

			const queryParameters: any = {};

						if (ercSiteTestEntityExternalReferenceCode === null || ercSiteTestEntityExternalReferenceCode === undefined) {
							throw new Error("Required parameter ercSiteTestEntityExternalReferenceCode was null or undefined when calling getSiteByExternalReferenceCodeErcSiteTestEntityByExternalReferenceCodeSubTestEntityByExternalReferenceCode.");
						}

						if (siteExternalReferenceCode === null || siteExternalReferenceCode === undefined) {
							throw new Error("Required parameter siteExternalReferenceCode was null or undefined when calling getSiteByExternalReferenceCodeErcSiteTestEntityByExternalReferenceCodeSubTestEntityByExternalReferenceCode.");
						}

						if (subTestEntityExternalReferenceCode === null || subTestEntityExternalReferenceCode === undefined) {
							throw new Error("Required parameter subTestEntityExternalReferenceCode was null or undefined when calling getSiteByExternalReferenceCodeErcSiteTestEntityByExternalReferenceCodeSubTestEntityByExternalReferenceCode.");
						}

			const queryString = Object.keys(queryParameters).length ?
				"?" + new URLSearchParams(queryParameters).toString() :
					"";

			const response = await fetch(path + queryString, {
				headers:
					Object.assign({}, this._defaultHeaders
						,{
								Accept: "application/json"
						}
					,headers || {}
					),
				method: "GET",
			});

			if (response.ok) {
				const contentType = response.headers.get("content-type") || "";

					if (contentType.includes("application/json")) {
						return {body: ObjectSerializer.deserialize(await response.json(), "SubTestEntity"), response};
					}
					else {
						return {body: await response.text() as any, response};
					}
			}
			else {
				throw new Error("HTTP Error " + response.status + ": " + response.statusText + ". " + await response.text());
			}
		}

}