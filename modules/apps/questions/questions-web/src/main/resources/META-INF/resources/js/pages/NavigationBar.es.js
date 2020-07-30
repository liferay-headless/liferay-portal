/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import ClayLink from '@clayui/link';
import ClayNavigationBar from '@clayui/navigation-bar';
import React, {useContext} from 'react';

import {AppContext} from '../AppContext.es';
import {historyPushWithSlug} from '../utils/utils.es';
import Link from '../components/Link.es';

export default ({pathname, sectionTitle}) => {
	const context = useContext(AppContext);

	const isActive = (value) => pathname.includes(value);

	const label = () => {
		if (pathname.includes('tags')) {
			return Liferay.Language.get('tags');
		}
		else if (pathname.includes('activity')) {
			return Liferay.Language.get('my-activity');
		}
		else if (pathname.includes('subscriptions')) {
			return Liferay.Language.get('my-subscriptions');
		}

		return Liferay.Language.get('questions');
	};

	const historyPushParser = historyPushWithSlug(history.push);

	return (
		<section className="border-bottom questions-section questions-section-nav">
			<div className="questions-container">
				<div className="row">
					{pathname !== '/' && (
						<div className="align-items-center col d-flex justify-content-between">
							<ClayNavigationBar
								className="navigation-bar"
								triggerLabel={label()}
							>
								<ClayNavigationBar.Item
									active={
										!isActive('activity') &&
										!isActive('tags') &&
										!isActive('subscriptions')
									}
								>
									<Link
										className="nav-link"
										displayType="unstyled"
										to={
											sectionTitle
												? `/questions/${sectionTitle}`
												: '/'
										}
									>
										{Liferay.Language.get('questions')}
									</Link>
								</ClayNavigationBar.Item>

								<ClayNavigationBar.Item
									active={isActive('tags')}
								>
									<Link
										className="nav-link"
										displayType="unstyled"
										to={
											sectionTitle
												? `/questions/${sectionTitle}/tags`
												: '/'
										}
									>
										{Liferay.Language.get('tags')}
									</Link>
								</ClayNavigationBar.Item>

								<ClayNavigationBar.Item
									active={isActive('subscriptions')}
									className={
										Liferay.ThemeDisplay.isSignedIn()
											? 'ml-md-auto'
											: 'd-none'
									}
								>
									<Link
										className="nav-link"
										displayType="unstyled"
										to={
											sectionTitle
												? `/subscriptions/${context.userId}?sectionTitle=${sectionTitle}`
												: '/'
										}
									>
										{Liferay.Language.get(
											'my-subscriptions'
										)}
									</Link>
								</ClayNavigationBar.Item>

								<ClayNavigationBar.Item
									active={isActive('activity')}
									className={
										Liferay.ThemeDisplay.isSignedIn()
											? ''
											: 'd-none'
									}
								>
									<Link
										className="nav-link"
										displayType="unstyled"
										to={
											sectionTitle
												? `/activity/${context.userId}?sectionTitle=${sectionTitle}`
												: '/'
										}
									>
										{Liferay.Language.get('my-activity')}
									</Link>
								</ClayNavigationBar.Item>
							</ClayNavigationBar>
						</div>
					)}
				</div>
			</div>
		</section>
	);
};
