/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ObjectSerializer} from '../utils/SerDes';

		import {ErcAssetLibraryTestEntity} from '../models/ErcAssetLibraryTestEntity';
		import {PageErcAssetLibraryTestEntity} from '../models/PageErcAssetLibraryTestEntity';

/**
 * @author Alejandro Tardín
 * @generated
 */

export class ErcAssetLibraryTestEntityAPI {
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
				 * @param assetLibraryExternalReferenceCode
		 * @param headers Optional custom request headers
		 */
		public async getAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntitiesPage(
						assetLibraryExternalReferenceCode: string,
			headers?: {[name: string]: string},
		): Promise<{
				body: PageErcAssetLibraryTestEntity;
			response: Response;
		}> {

			const path = this._basePath + "/test/v1.0/asset-libraries/{assetLibraryExternalReferenceCode}/erc-asset-library-test-entities"
						.replace("{assetLibraryExternalReferenceCode}",encodeURIComponent(assetLibraryExternalReferenceCode))
				;

			const queryParameters: any = {};

						if (assetLibraryExternalReferenceCode === null || assetLibraryExternalReferenceCode === undefined) {
							throw new Error("Required parameter assetLibraryExternalReferenceCode was null or undefined when calling getAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntitiesPage.");
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
						return {body: ObjectSerializer.deserialize(await response.json(), "PageErcAssetLibraryTestEntity"), response};
					}
					else {
						return {body: await response.text() as any, response};
					}
			}
			else {
				throw new Error("HTTP Error " + response.status + ": " + response.statusText + ". " + await response.text());
			}
		}

		/**
		 * 
				 * @param ErcAssetLibraryTestEntityExternalReferenceCode
				 * @param assetLibraryExternalReferenceCode
		 * @param headers Optional custom request headers
		 */
		public async getAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityByExternalReferenceCode(
						ErcAssetLibraryTestEntityExternalReferenceCode: string,
						assetLibraryExternalReferenceCode: string,
			headers?: {[name: string]: string},
		): Promise<{
				body: ErcAssetLibraryTestEntity;
			response: Response;
		}> {

			const path = this._basePath + "/test/v1.0/asset-libraries/{assetLibraryExternalReferenceCode}/erc-asset-library-test-entities/{ErcAssetLibraryTestEntityExternalReferenceCode}"
						.replace("{ErcAssetLibraryTestEntityExternalReferenceCode}",encodeURIComponent(ErcAssetLibraryTestEntityExternalReferenceCode))
										.replace("{assetLibraryExternalReferenceCode}",encodeURIComponent(assetLibraryExternalReferenceCode))
				;

			const queryParameters: any = {};

						if (ErcAssetLibraryTestEntityExternalReferenceCode === null || ErcAssetLibraryTestEntityExternalReferenceCode === undefined) {
							throw new Error("Required parameter ErcAssetLibraryTestEntityExternalReferenceCode was null or undefined when calling getAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityByExternalReferenceCode.");
						}

						if (assetLibraryExternalReferenceCode === null || assetLibraryExternalReferenceCode === undefined) {
							throw new Error("Required parameter assetLibraryExternalReferenceCode was null or undefined when calling getAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityByExternalReferenceCode.");
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
						return {body: ObjectSerializer.deserialize(await response.json(), "ErcAssetLibraryTestEntity"), response};
					}
					else {
						return {body: await response.text() as any, response};
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
		public async getAssetLibraryErcAssetLibraryTestEntitiesPage(
						assetLibraryId: number,
			headers?: {[name: string]: string},
		): Promise<{
				body: PageErcAssetLibraryTestEntity;
			response: Response;
		}> {

			const path = this._basePath + "/test/v1.0/asset-libraries/{assetLibraryId}/erc-asset-library-test-entities"
						.replace("{assetLibraryId}",encodeURIComponent(assetLibraryId))
				;

			const queryParameters: any = {};

						if (assetLibraryId === null || assetLibraryId === undefined) {
							throw new Error("Required parameter assetLibraryId was null or undefined when calling getAssetLibraryErcAssetLibraryTestEntitiesPage.");
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
						return {body: ObjectSerializer.deserialize(await response.json(), "PageErcAssetLibraryTestEntity"), response};
					}
					else {
						return {body: await response.text() as any, response};
					}
			}
			else {
				throw new Error("HTTP Error " + response.status + ": " + response.statusText + ". " + await response.text());
			}
		}

		/**
		 * 
				 * @param assetLibraryExternalReferenceCode
		 		* @param requestBody Request body that can be one of multiple content types
		 * @param headers Optional custom request headers
		 */
		public async postAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityWithContentType(
						assetLibraryExternalReferenceCode: string,
					requestBody:
							{
								parameters: {
										ercAssetLibraryTestEntity?: ErcAssetLibraryTestEntity
								},
								type: "application/json"
							}
								|
							{
								parameters: {
										ercAssetLibraryTestEntity?: ErcAssetLibraryTestEntity
								},
								type: "application/xml"
							}
								,
			headers?: {[name: string]: string},
		): Promise<{
				body: ErcAssetLibraryTestEntity;
			response: Response;
		}> {
				let body;
						if (requestBody.type === "application/json") {
								body = JSON.stringify(ObjectSerializer.serialize(requestBody.parameters.ercAssetLibraryTestEntity, "ErcAssetLibraryTestEntity"));
						}
						if (requestBody.type === "application/xml") {
								body = JSON.stringify(ObjectSerializer.serialize(requestBody.parameters.ercAssetLibraryTestEntity, "ErcAssetLibraryTestEntity"));
						}

			const path = this._basePath + "/test/v1.0/asset-libraries/{assetLibraryExternalReferenceCode}/erc-asset-library-test-entities"
						.replace("{assetLibraryExternalReferenceCode}",encodeURIComponent(assetLibraryExternalReferenceCode))
				;

			const queryParameters: any = {};

						if (assetLibraryExternalReferenceCode === null || assetLibraryExternalReferenceCode === undefined) {
							throw new Error("Required parameter assetLibraryExternalReferenceCode was null or undefined when calling postAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntity.");
						}

			const queryString = Object.keys(queryParameters).length ?
				"?" + new URLSearchParams(queryParameters).toString() :
					"";

			const response = await fetch(path + queryString, {
					body: body,
				headers:
					Object.assign({}, this._defaultHeaders
						,{
								Accept: "application/json"
						}
								,{"Content-Type": requestBody.type}
					,headers || {}
					),
				method: "POST",
			});

			if (response.ok) {
				const contentType = response.headers.get("content-type") || "";

					if (contentType.includes("application/json")) {
						return {body: ObjectSerializer.deserialize(await response.json(), "ErcAssetLibraryTestEntity"), response};
					}
					else {
						return {body: await response.text() as any, response};
					}
			}
			else {
				throw new Error("HTTP Error " + response.status + ": " + response.statusText + ". " + await response.text());
			}
		}

					/**
					 *  - Default method for JSON body
							 * @param assetLibraryExternalReferenceCode
						 * @param ercAssetLibraryTestEntity
					 */
					public async postAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntity(
									assetLibraryExternalReferenceCode: string,
							ercAssetLibraryTestEntity?: ErcAssetLibraryTestEntity,
						headers?: {[name: string]: string}
					): Promise<{
							body: ErcAssetLibraryTestEntity;
						response: Response;
					}> {
						return this.postAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityWithContentType(
										assetLibraryExternalReferenceCode,
							{
								parameters: {
										ercAssetLibraryTestEntity: ercAssetLibraryTestEntity
								},
								type: "application/json"
							},
							headers
						);
					}
		/**
		 * 
				 * @param assetLibraryId
		 		* @param requestBody Request body that can be one of multiple content types
		 * @param headers Optional custom request headers
		 */
		public async postAssetLibraryErcAssetLibraryTestEntityWithContentType(
						assetLibraryId: number,
					requestBody:
							{
								parameters: {
										ercAssetLibraryTestEntity?: ErcAssetLibraryTestEntity
								},
								type: "application/json"
							}
								|
							{
								parameters: {
										ercAssetLibraryTestEntity?: ErcAssetLibraryTestEntity
								},
								type: "application/xml"
							}
								,
			headers?: {[name: string]: string},
		): Promise<{
				body: ErcAssetLibraryTestEntity;
			response: Response;
		}> {
				let body;
						if (requestBody.type === "application/json") {
								body = JSON.stringify(ObjectSerializer.serialize(requestBody.parameters.ercAssetLibraryTestEntity, "ErcAssetLibraryTestEntity"));
						}
						if (requestBody.type === "application/xml") {
								body = JSON.stringify(ObjectSerializer.serialize(requestBody.parameters.ercAssetLibraryTestEntity, "ErcAssetLibraryTestEntity"));
						}

			const path = this._basePath + "/test/v1.0/asset-libraries/{assetLibraryId}/erc-asset-library-test-entities"
						.replace("{assetLibraryId}",encodeURIComponent(assetLibraryId))
				;

			const queryParameters: any = {};

						if (assetLibraryId === null || assetLibraryId === undefined) {
							throw new Error("Required parameter assetLibraryId was null or undefined when calling postAssetLibraryErcAssetLibraryTestEntity.");
						}

			const queryString = Object.keys(queryParameters).length ?
				"?" + new URLSearchParams(queryParameters).toString() :
					"";

			const response = await fetch(path + queryString, {
					body: body,
				headers:
					Object.assign({}, this._defaultHeaders
						,{
								Accept: "application/json"
						}
								,{"Content-Type": requestBody.type}
					,headers || {}
					),
				method: "POST",
			});

			if (response.ok) {
				const contentType = response.headers.get("content-type") || "";

					if (contentType.includes("application/json")) {
						return {body: ObjectSerializer.deserialize(await response.json(), "ErcAssetLibraryTestEntity"), response};
					}
					else {
						return {body: await response.text() as any, response};
					}
			}
			else {
				throw new Error("HTTP Error " + response.status + ": " + response.statusText + ". " + await response.text());
			}
		}

					/**
					 *  - Default method for JSON body
							 * @param assetLibraryId
						 * @param ercAssetLibraryTestEntity
					 */
					public async postAssetLibraryErcAssetLibraryTestEntity(
									assetLibraryId: number,
							ercAssetLibraryTestEntity?: ErcAssetLibraryTestEntity,
						headers?: {[name: string]: string}
					): Promise<{
							body: ErcAssetLibraryTestEntity;
						response: Response;
					}> {
						return this.postAssetLibraryErcAssetLibraryTestEntityWithContentType(
										assetLibraryId,
							{
								parameters: {
										ercAssetLibraryTestEntity: ercAssetLibraryTestEntity
								},
								type: "application/json"
							},
							headers
						);
					}
		/**
		 * 
				 * @param ErcAssetLibraryTestEntityExternalReferenceCode
				 * @param assetLibraryExternalReferenceCode
		 		* @param requestBody Request body that can be one of multiple content types
		 * @param headers Optional custom request headers
		 */
		public async putAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityByExternalReferenceCodeWithContentType(
						ErcAssetLibraryTestEntityExternalReferenceCode: string,
						assetLibraryExternalReferenceCode: string,
					requestBody:
							{
								parameters: {
										ercAssetLibraryTestEntity?: ErcAssetLibraryTestEntity
								},
								type: "application/json"
							}
								|
							{
								parameters: {
										ercAssetLibraryTestEntity?: ErcAssetLibraryTestEntity
								},
								type: "application/xml"
							}
								,
			headers?: {[name: string]: string},
		): Promise<{
				body: ErcAssetLibraryTestEntity;
			response: Response;
		}> {
				let body;
						if (requestBody.type === "application/json") {
								body = JSON.stringify(ObjectSerializer.serialize(requestBody.parameters.ercAssetLibraryTestEntity, "ErcAssetLibraryTestEntity"));
						}
						if (requestBody.type === "application/xml") {
								body = JSON.stringify(ObjectSerializer.serialize(requestBody.parameters.ercAssetLibraryTestEntity, "ErcAssetLibraryTestEntity"));
						}

			const path = this._basePath + "/test/v1.0/asset-libraries/{assetLibraryExternalReferenceCode}/erc-asset-library-test-entities/{ErcAssetLibraryTestEntityExternalReferenceCode}"
						.replace("{ErcAssetLibraryTestEntityExternalReferenceCode}",encodeURIComponent(ErcAssetLibraryTestEntityExternalReferenceCode))
										.replace("{assetLibraryExternalReferenceCode}",encodeURIComponent(assetLibraryExternalReferenceCode))
				;

			const queryParameters: any = {};

						if (ErcAssetLibraryTestEntityExternalReferenceCode === null || ErcAssetLibraryTestEntityExternalReferenceCode === undefined) {
							throw new Error("Required parameter ErcAssetLibraryTestEntityExternalReferenceCode was null or undefined when calling putAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityByExternalReferenceCode.");
						}

						if (assetLibraryExternalReferenceCode === null || assetLibraryExternalReferenceCode === undefined) {
							throw new Error("Required parameter assetLibraryExternalReferenceCode was null or undefined when calling putAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityByExternalReferenceCode.");
						}

			const queryString = Object.keys(queryParameters).length ?
				"?" + new URLSearchParams(queryParameters).toString() :
					"";

			const response = await fetch(path + queryString, {
					body: body,
				headers:
					Object.assign({}, this._defaultHeaders
						,{
								Accept: "application/json"
						}
								,{"Content-Type": requestBody.type}
					,headers || {}
					),
				method: "PUT",
			});

			if (response.ok) {
				const contentType = response.headers.get("content-type") || "";

					if (contentType.includes("application/json")) {
						return {body: ObjectSerializer.deserialize(await response.json(), "ErcAssetLibraryTestEntity"), response};
					}
					else {
						return {body: await response.text() as any, response};
					}
			}
			else {
				throw new Error("HTTP Error " + response.status + ": " + response.statusText + ". " + await response.text());
			}
		}

					/**
					 *  - Default method for JSON body
							 * @param ErcAssetLibraryTestEntityExternalReferenceCode
							 * @param assetLibraryExternalReferenceCode
						 * @param ercAssetLibraryTestEntity
					 */
					public async putAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityByExternalReferenceCode(
									ErcAssetLibraryTestEntityExternalReferenceCode: string,
									assetLibraryExternalReferenceCode: string,
							ercAssetLibraryTestEntity?: ErcAssetLibraryTestEntity,
						headers?: {[name: string]: string}
					): Promise<{
							body: ErcAssetLibraryTestEntity;
						response: Response;
					}> {
						return this.putAssetLibraryByExternalReferenceCodeErcAssetLibraryTestEntityByExternalReferenceCodeWithContentType(
										ErcAssetLibraryTestEntityExternalReferenceCode,
										assetLibraryExternalReferenceCode,
							{
								parameters: {
										ercAssetLibraryTestEntity: ercAssetLibraryTestEntity
								},
								type: "application/json"
							},
							headers
						);
					}
}