/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ObjectSerializer} from '../utils/SerDes';

		import {ErcSiteTestEntity} from '../models/ErcSiteTestEntity';
		import {PageErcSiteTestEntity} from '../models/PageErcSiteTestEntity';

/**
 * @author Alejandro Tardín
 * @generated
 */

export class ErcSiteTestEntityAPI {
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
				 * @param siteExternalReferenceCode
		 * @param headers Optional custom request headers
		 */
		public async getSiteByExternalReferenceCodeErcSiteTestEntitiesPage(
						siteExternalReferenceCode: string,
			headers?: {[name: string]: string},
		): Promise<{
				body: PageErcSiteTestEntity;
			response: Response;
		}> {

			const path = this._basePath + "/test/v1.0/sites/{siteExternalReferenceCode}/erc-site-test-entities"
						.replace("{siteExternalReferenceCode}",encodeURIComponent(siteExternalReferenceCode))
				;

			const queryParameters: any = {};

						if (siteExternalReferenceCode === null || siteExternalReferenceCode === undefined) {
							throw new Error("Required parameter siteExternalReferenceCode was null or undefined when calling getSiteByExternalReferenceCodeErcSiteTestEntitiesPage.");
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
						return {body: ObjectSerializer.deserialize(await response.json(), "PageErcSiteTestEntity"), response};
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
				 * @param ercSiteTestEntityExternalReferenceCode
				 * @param siteExternalReferenceCode
		 * @param headers Optional custom request headers
		 */
		public async getSiteByExternalReferenceCodeErcSiteTestEntityByExternalReferenceCode(
						ercSiteTestEntityExternalReferenceCode: string,
						siteExternalReferenceCode: string,
			headers?: {[name: string]: string},
		): Promise<{
				body: ErcSiteTestEntity;
			response: Response;
		}> {

			const path = this._basePath + "/test/v1.0/sites/{siteExternalReferenceCode}/erc-site-test-entities/{ercSiteTestEntityExternalReferenceCode}"
						.replace("{ercSiteTestEntityExternalReferenceCode}",encodeURIComponent(ercSiteTestEntityExternalReferenceCode))
										.replace("{siteExternalReferenceCode}",encodeURIComponent(siteExternalReferenceCode))
				;

			const queryParameters: any = {};

						if (ercSiteTestEntityExternalReferenceCode === null || ercSiteTestEntityExternalReferenceCode === undefined) {
							throw new Error("Required parameter ercSiteTestEntityExternalReferenceCode was null or undefined when calling getSiteByExternalReferenceCodeErcSiteTestEntityByExternalReferenceCode.");
						}

						if (siteExternalReferenceCode === null || siteExternalReferenceCode === undefined) {
							throw new Error("Required parameter siteExternalReferenceCode was null or undefined when calling getSiteByExternalReferenceCodeErcSiteTestEntityByExternalReferenceCode.");
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
						return {body: ObjectSerializer.deserialize(await response.json(), "ErcSiteTestEntity"), response};
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
				 * @param siteId
		 * @param headers Optional custom request headers
		 */
		public async getSiteErcSiteTestEntitiesPage(
						siteId: number,
			headers?: {[name: string]: string},
		): Promise<{
				body: PageErcSiteTestEntity;
			response: Response;
		}> {

			const path = this._basePath + "/test/v1.0/sites/{siteId}/erc-site-test-entities"
						.replace("{siteId}",encodeURIComponent(siteId))
				;

			const queryParameters: any = {};

						if (siteId === null || siteId === undefined) {
							throw new Error("Required parameter siteId was null or undefined when calling getSiteErcSiteTestEntitiesPage.");
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
						return {body: ObjectSerializer.deserialize(await response.json(), "PageErcSiteTestEntity"), response};
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
				 * @param siteExternalReferenceCode
		 		* @param requestBody Request body that can be one of multiple content types
		 * @param headers Optional custom request headers
		 */
		public async postSiteByExternalReferenceCodeErcSiteTestEntityWithContentType(
						siteExternalReferenceCode: string,
					requestBody:
							{
								parameters: {
										ercSiteTestEntity?: ErcSiteTestEntity
								},
								type: "application/json"
							}
								|
							{
								parameters: {
										ercSiteTestEntity?: ErcSiteTestEntity
								},
								type: "application/xml"
							}
								,
			headers?: {[name: string]: string},
		): Promise<{
				body: ErcSiteTestEntity;
			response: Response;
		}> {
				let body;
						if (requestBody.type === "application/json") {
								body = JSON.stringify(ObjectSerializer.serialize(requestBody.parameters.ercSiteTestEntity, "ErcSiteTestEntity"));
						}
						if (requestBody.type === "application/xml") {
								body = JSON.stringify(ObjectSerializer.serialize(requestBody.parameters.ercSiteTestEntity, "ErcSiteTestEntity"));
						}

			const path = this._basePath + "/test/v1.0/sites/{siteExternalReferenceCode}/erc-site-test-entities"
						.replace("{siteExternalReferenceCode}",encodeURIComponent(siteExternalReferenceCode))
				;

			const queryParameters: any = {};

						if (siteExternalReferenceCode === null || siteExternalReferenceCode === undefined) {
							throw new Error("Required parameter siteExternalReferenceCode was null or undefined when calling postSiteByExternalReferenceCodeErcSiteTestEntity.");
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
						return {body: ObjectSerializer.deserialize(await response.json(), "ErcSiteTestEntity"), response};
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
							 * @param siteExternalReferenceCode
						 * @param ercSiteTestEntity
					 */
					public async postSiteByExternalReferenceCodeErcSiteTestEntity(
									siteExternalReferenceCode: string,
							ercSiteTestEntity?: ErcSiteTestEntity,
						headers?: {[name: string]: string}
					): Promise<{
							body: ErcSiteTestEntity;
						response: Response;
					}> {
						return this.postSiteByExternalReferenceCodeErcSiteTestEntityWithContentType(
										siteExternalReferenceCode,
							{
								parameters: {
										ercSiteTestEntity: ercSiteTestEntity
								},
								type: "application/json"
							},
							headers
						);
					}
		/**
		 * 
				 * @param siteId
		 		* @param requestBody Request body that can be one of multiple content types
		 * @param headers Optional custom request headers
		 */
		public async postSiteErcSiteTestEntityWithContentType(
						siteId: number,
					requestBody:
							{
								parameters: {
										ercSiteTestEntity?: ErcSiteTestEntity
								},
								type: "application/json"
							}
								|
							{
								parameters: {
										ercSiteTestEntity?: ErcSiteTestEntity
								},
								type: "application/xml"
							}
								,
			headers?: {[name: string]: string},
		): Promise<{
				body: ErcSiteTestEntity;
			response: Response;
		}> {
				let body;
						if (requestBody.type === "application/json") {
								body = JSON.stringify(ObjectSerializer.serialize(requestBody.parameters.ercSiteTestEntity, "ErcSiteTestEntity"));
						}
						if (requestBody.type === "application/xml") {
								body = JSON.stringify(ObjectSerializer.serialize(requestBody.parameters.ercSiteTestEntity, "ErcSiteTestEntity"));
						}

			const path = this._basePath + "/test/v1.0/sites/{siteId}/erc-site-test-entities"
						.replace("{siteId}",encodeURIComponent(siteId))
				;

			const queryParameters: any = {};

						if (siteId === null || siteId === undefined) {
							throw new Error("Required parameter siteId was null or undefined when calling postSiteErcSiteTestEntity.");
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
						return {body: ObjectSerializer.deserialize(await response.json(), "ErcSiteTestEntity"), response};
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
							 * @param siteId
						 * @param ercSiteTestEntity
					 */
					public async postSiteErcSiteTestEntity(
									siteId: number,
							ercSiteTestEntity?: ErcSiteTestEntity,
						headers?: {[name: string]: string}
					): Promise<{
							body: ErcSiteTestEntity;
						response: Response;
					}> {
						return this.postSiteErcSiteTestEntityWithContentType(
										siteId,
							{
								parameters: {
										ercSiteTestEntity: ercSiteTestEntity
								},
								type: "application/json"
							},
							headers
						);
					}
		/**
		 * 
				 * @param ercSiteTestEntityExternalReferenceCode
				 * @param siteExternalReferenceCode
		 		* @param requestBody Request body that can be one of multiple content types
		 * @param headers Optional custom request headers
		 */
		public async putSiteByExternalReferenceCodeErcSiteTestEntityByExternalReferenceCodeWithContentType(
						ercSiteTestEntityExternalReferenceCode: string,
						siteExternalReferenceCode: string,
					requestBody:
							{
								parameters: {
										ercSiteTestEntity?: ErcSiteTestEntity
								},
								type: "application/json"
							}
								|
							{
								parameters: {
										ercSiteTestEntity?: ErcSiteTestEntity
								},
								type: "application/xml"
							}
								,
			headers?: {[name: string]: string},
		): Promise<{
				body: ErcSiteTestEntity;
			response: Response;
		}> {
				let body;
						if (requestBody.type === "application/json") {
								body = JSON.stringify(ObjectSerializer.serialize(requestBody.parameters.ercSiteTestEntity, "ErcSiteTestEntity"));
						}
						if (requestBody.type === "application/xml") {
								body = JSON.stringify(ObjectSerializer.serialize(requestBody.parameters.ercSiteTestEntity, "ErcSiteTestEntity"));
						}

			const path = this._basePath + "/test/v1.0/sites/{siteExternalReferenceCode}/erc-site-test-entities/{ercSiteTestEntityExternalReferenceCode}"
						.replace("{ercSiteTestEntityExternalReferenceCode}",encodeURIComponent(ercSiteTestEntityExternalReferenceCode))
										.replace("{siteExternalReferenceCode}",encodeURIComponent(siteExternalReferenceCode))
				;

			const queryParameters: any = {};

						if (ercSiteTestEntityExternalReferenceCode === null || ercSiteTestEntityExternalReferenceCode === undefined) {
							throw new Error("Required parameter ercSiteTestEntityExternalReferenceCode was null or undefined when calling putSiteByExternalReferenceCodeErcSiteTestEntityByExternalReferenceCode.");
						}

						if (siteExternalReferenceCode === null || siteExternalReferenceCode === undefined) {
							throw new Error("Required parameter siteExternalReferenceCode was null or undefined when calling putSiteByExternalReferenceCodeErcSiteTestEntityByExternalReferenceCode.");
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
						return {body: ObjectSerializer.deserialize(await response.json(), "ErcSiteTestEntity"), response};
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
							 * @param ercSiteTestEntityExternalReferenceCode
							 * @param siteExternalReferenceCode
						 * @param ercSiteTestEntity
					 */
					public async putSiteByExternalReferenceCodeErcSiteTestEntityByExternalReferenceCode(
									ercSiteTestEntityExternalReferenceCode: string,
									siteExternalReferenceCode: string,
							ercSiteTestEntity?: ErcSiteTestEntity,
						headers?: {[name: string]: string}
					): Promise<{
							body: ErcSiteTestEntity;
						response: Response;
					}> {
						return this.putSiteByExternalReferenceCodeErcSiteTestEntityByExternalReferenceCodeWithContentType(
										ercSiteTestEntityExternalReferenceCode,
										siteExternalReferenceCode,
							{
								parameters: {
										ercSiteTestEntity: ercSiteTestEntity
								},
								type: "application/json"
							},
							headers
						);
					}
}