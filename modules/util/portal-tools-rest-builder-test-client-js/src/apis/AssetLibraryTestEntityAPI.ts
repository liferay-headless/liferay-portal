/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ObjectSerializer} from '../utils/SerDes';

		import {PageAssetLibraryTestEntity} from '../models/PageAssetLibraryTestEntity';

/**
 * @author Alejandro Tardín
 * @generated
 */

export class AssetLibraryTestEntityAPI {
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
				 * @param assetLibraryId
				 * @param assetLibraryTestEntityExternalReferenceCode
		 * @param headers Optional custom request headers
		 */
		public async deleteAssetLibraryAssetLibraryTestEntityByExternalReferenceCodeAssetLibraryTestEntityExternalReferenceCode(
						assetLibraryId: number,
						assetLibraryTestEntityExternalReferenceCode: string,
			headers?: {[name: string]: string},
		): Promise<{
				body?: any;
			response: Response;
		}> {

			const path = this._basePath + "/test/v1.0/asset-libraries/{assetLibraryId}/asset-library-test-entities/by-external-reference-code/{assetLibraryTestEntityExternalReferenceCode}"
						.replace("{assetLibraryId}",encodeURIComponent(assetLibraryId))
										.replace("{assetLibraryTestEntityExternalReferenceCode}",encodeURIComponent(assetLibraryTestEntityExternalReferenceCode))
				;

			const queryParameters: any = {};

						if (assetLibraryId === null || assetLibraryId === undefined) {
							throw new Error("Required parameter assetLibraryId was null or undefined when calling deleteAssetLibraryAssetLibraryTestEntityByExternalReferenceCodeAssetLibraryTestEntityExternalReferenceCode.");
						}

						if (assetLibraryTestEntityExternalReferenceCode === null || assetLibraryTestEntityExternalReferenceCode === undefined) {
							throw new Error("Required parameter assetLibraryTestEntityExternalReferenceCode was null or undefined when calling deleteAssetLibraryAssetLibraryTestEntityByExternalReferenceCodeAssetLibraryTestEntityExternalReferenceCode.");
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
				method: "DELETE",
			});

			if (response.ok) {
				const contentType = response.headers.get("content-type") || "";

					if (contentType.includes("application/json")) {
						return {body: await response.json(), response};
					}
					else {
						return {body: await response.text(), response};
					}
			}
			else {
				throw new Error("HTTP Error " + response.status + ": " + response.statusText + ". " + await response.text());
			}
		}

		/**
		 * 
				 * @param assetLibraryId
		 * @param headers Optional custom request headers
		 */
		public async getAssetLibraryAssetLibraryTestEntitiesPage(
						assetLibraryId: number,
			headers?: {[name: string]: string},
		): Promise<{
				body: PageAssetLibraryTestEntity;
			response: Response;
		}> {

			const path = this._basePath + "/test/v1.0/asset-libraries/{assetLibraryId}/asset-library-test-entities"
						.replace("{assetLibraryId}",encodeURIComponent(assetLibraryId))
				;

			const queryParameters: any = {};

						if (assetLibraryId === null || assetLibraryId === undefined) {
							throw new Error("Required parameter assetLibraryId was null or undefined when calling getAssetLibraryAssetLibraryTestEntitiesPage.");
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
						return {body: ObjectSerializer.deserialize(await response.json(), "PageAssetLibraryTestEntity"), response};
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