/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import http from 'http';

import localVarRequest from 'request';
/* tslint:disable:no-unused-locals */
import {
	Authentication,
	Interceptor,
	ObjectSerializer,
	VoidAuth,
} from '../model/models';
		import {SubTestEntity} from '../model/subTestEntity';

import {HttpError} from './apis';
const defaultBasePath = 'http://localhost';

/**
 * @author Alejandro Tardín
 * @generated
 */

export enum SubTestEntityApiApiKeys {}

export class SubTestEntityApi {
	protected _basePath = defaultBasePath;
	protected _defaultHeaders: any = {};
	protected _useQuerystring: boolean = false;

	protected authentications = {
		default: <Authentication>new VoidAuth(),
	};

	protected interceptors: Interceptor[] = [];

	constructor(basePath?: string);
	constructor(
		basePathOrUsername: string,
		password?: string,
		basePath?: string
	) {
		if (password) {
			if (basePath) {
				this.basePath = basePath;
			}
		}
		else {
			if (basePathOrUsername) {
				this.basePath = basePathOrUsername;
			}
		}
	}

	set useQuerystring(value: boolean) {
		this._useQuerystring = value;
	}

	set basePath(basePath: string) {
		this._basePath = basePath;
	}

	set defaultHeaders(defaultHeaders: any) {
		this._defaultHeaders = defaultHeaders;
	}

	get defaultHeaders() {
		return this._defaultHeaders;
	}

	get basePath() {
		return this._basePath;
	}

	public setDefaultAuthentication(auth: Authentication) {
		this.authentications.default = auth;
	}

	public setApiKey(key: SubTestEntityApiApiKeys, value: string) {
		(this.authentications as any)[SubTestEntityApiApiKeys[key]].apiKey =
			value;
	}

	public addInterceptor(interceptor: Interceptor) {
		this.interceptors.push(interceptor);
	}

		/**
		 * 
				 * @param ercSiteTestEntityExternalReferenceCode 
				 * @param siteExternalReferenceCode 
				 * @param subTestEntityExternalReferenceCode 
		 */
		public async getSiteErcSiteTestEntityByExternalReferenceCodeSubTestEntityByExternalReferenceCode(
					ercSiteTestEntityExternalReferenceCode: string,
					siteExternalReferenceCode: string,
					subTestEntityExternalReferenceCode: string,
			options: {
				headers: {[name: string]: string};
			} = {headers: {}}
		): Promise<{
				body: SubTestEntity;
			response: http.IncomingMessage;
		}> {
			const localVarPath = this.basePath + '/test/v1.0/sites/{siteExternalReferenceCode}/erc-site-test-entities/{ercSiteTestEntityExternalReferenceCode}/sub-test-entities/{subTestEntityExternalReferenceCode}'
						.replace(
							'{' + 'ercSiteTestEntityExternalReferenceCode' + '}',
							encodeURIComponent(String(ercSiteTestEntityExternalReferenceCode))
						)
										.replace(
							'{' + 'siteExternalReferenceCode' + '}',
							encodeURIComponent(String(siteExternalReferenceCode))
						)
										.replace(
							'{' + 'subTestEntityExternalReferenceCode' + '}',
							encodeURIComponent(String(subTestEntityExternalReferenceCode))
						)
				;
			const localVarQueryParameters: any = {};
			const localVarHeaderParams: any = (<any>Object).assign({}, this._defaultHeaders);
				const responseContentTypes = ['application/json', 'application/xml'];
				if (responseContentTypes.indexOf('application/json') >= 0) {
					localVarHeaderParams.Accept = 'application/json';
				} else {
					localVarHeaderParams.Accept = responseContentTypes.join(',');
				}
			const localVarFormParams: any = {};

						if (ercSiteTestEntityExternalReferenceCode === null || ercSiteTestEntityExternalReferenceCode === undefined) {
							throw new Error('Required parameter ercSiteTestEntityExternalReferenceCode was null or undefined when calling getSiteErcSiteTestEntityByExternalReferenceCodeSubTestEntityByExternalReferenceCode.');
						}
						if (siteExternalReferenceCode === null || siteExternalReferenceCode === undefined) {
							throw new Error('Required parameter siteExternalReferenceCode was null or undefined when calling getSiteErcSiteTestEntityByExternalReferenceCodeSubTestEntityByExternalReferenceCode.');
						}
						if (subTestEntityExternalReferenceCode === null || subTestEntityExternalReferenceCode === undefined) {
							throw new Error('Required parameter subTestEntityExternalReferenceCode was null or undefined when calling getSiteErcSiteTestEntityByExternalReferenceCodeSubTestEntityByExternalReferenceCode.');
						}
			(<any>Object).assign(localVarHeaderParams, options.headers);

			const localVarUseFormData = false;

			const localVarRequestOptions: localVarRequest.Options = {
				headers: localVarHeaderParams,
				json: true,
				method: 'GET',
				qs: localVarQueryParameters,
				uri: localVarPath,
				useQuerystring: this._useQuerystring
			};

			let authenticationPromise = Promise.resolve();
			authenticationPromise = authenticationPromise.then(() => this.authentications.default.applyToRequest(localVarRequestOptions));

			let interceptorPromise = authenticationPromise;
			for (const interceptor of this.interceptors) {
				interceptorPromise = interceptorPromise.then(() => interceptor(localVarRequestOptions));
			}

			return interceptorPromise.then(() => {
				if (Object.keys(localVarFormParams).length) {
					if (localVarUseFormData) {
						(<any>localVarRequestOptions).formData = localVarFormParams;
					} else {
						localVarRequestOptions.form = localVarFormParams;
					}
				}
				return new Promise<{  body: SubTestEntity; response: http.IncomingMessage;}>((resolve, reject) => {
					localVarRequest(localVarRequestOptions, (error, response, body) => {
						if (error) {
							reject(error);
						}
						else {
							if (
								response.statusCode &&
								response.statusCode >= 200 &&
								response.statusCode <= 299
							) {
								resolve({body, response});
							}
							else {
								reject(
									new HttpError(
										body,
										response,
										response.statusCode
									)
								);
							}
						}
					}
				);
			});
		});
	}
}