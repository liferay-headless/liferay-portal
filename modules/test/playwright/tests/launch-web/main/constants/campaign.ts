/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export const FRAGMENT_SET_NAME = 'Campaign Fragments';

export const HEADER_FRAGMENT = {
	html: '<header style="align-items:center;background:#fffdf9;border-bottom:1px solid #e7e2da;display:flex;flex-wrap:wrap;font-family:system-ui,-apple-system,Segoe UI,sans-serif;gap:1.25rem;justify-content:space-between;padding:1.1rem 2rem;"><div style="align-items:center;display:flex;gap:0.7rem;"><span style="background:#0f172a;border-radius:10px;color:#fff;display:inline-block;font-size:1.05rem;font-weight:800;height:2.1rem;line-height:2.1rem;text-align:center;width:2.1rem;">N</span><span style="color:#0f172a;font-size:1.22rem;font-weight:800;letter-spacing:-0.01em;">Northwind &amp; Co.</span></div><nav style="color:#475569;display:flex;flex-wrap:wrap;font-size:0.95rem;gap:1.5rem;"><a href="#" style="color:#475569;text-decoration:none;">New in</a><a href="#" style="color:#475569;text-decoration:none;">Home</a><a href="#" style="color:#475569;text-decoration:none;">Kitchen</a><a href="#" style="color:#475569;text-decoration:none;">Gifts</a><a href="#" style="color:#475569;text-decoration:none;">Sale</a></nav><div style="align-items:center;color:#0f172a;display:flex;font-size:0.92rem;gap:1.1rem;"><span>Search</span><span>Account</span><span style="font-weight:700;">Bag (2)</span></div></header>',
	name: 'Shop Header',
};

export const FOOTER_FRAGMENT = {
	html: '<footer style="background:#0f172a;color:#94a3b8;font-family:system-ui,-apple-system,Segoe UI,sans-serif;padding:2.5rem 2rem 2rem;"><div style="display:flex;flex-wrap:wrap;gap:2rem;justify-content:space-between;margin:0 auto;max-width:1100px;"><div style="max-width:18rem;"><p style="color:#f8fafc;font-size:1.05rem;font-weight:800;margin:0 0 0.5rem;">Northwind &amp; Co.</p><p style="font-size:0.9rem;line-height:1.6;margin:0;">Homeware and gifts, made to be kept. Family run since 1987.</p></div><div><p style="color:#e2e8f0;font-size:0.85rem;font-weight:700;letter-spacing:0.1em;margin:0 0 0.6rem;text-transform:uppercase;">Shop</p><p style="font-size:0.9rem;line-height:1.9;margin:0;">New in<br/>Best sellers<br/>Gift cards</p></div><div><p style="color:#e2e8f0;font-size:0.85rem;font-weight:700;letter-spacing:0.1em;margin:0 0 0.6rem;text-transform:uppercase;">Help</p><p style="font-size:0.9rem;line-height:1.9;margin:0;">Delivery<br/>Returns<br/>Contact us</p></div></div><p style="border-top:1px solid #1e293b;font-size:0.82rem;margin:2rem auto 0;max-width:1100px;padding-top:1.2rem;text-align:center;">&copy; 2026 Northwind &amp; Co. &middot; All prices include VAT</p></footer>',
	name: 'Shop Footer',
};

export const PROMO_RIBBON_FRAGMENT_NAME = 'Promo Ribbon';

export const PROMO_RIBBON_VERSIONS = [
	'<div class="fragment_101" style="background:#0b5fff;color:#fff;padding:24px;font-size:20px;text-align:center;">PUBLISHED RIBBON &mdash; Free shipping all year</div>',
	'<div style="background:linear-gradient(90deg,#1e293b 0%,#334155 100%);font-family:system-ui,-apple-system,Segoe UI,sans-serif;padding:0.95rem 1.5rem;text-align:center;"><span style="background:rgba(226,232,240,0.16);border-radius:999px;color:#f8fafc;display:inline-block;font-size:0.78rem;font-weight:700;letter-spacing:0.08em;padding:0.3rem 0.9rem;text-transform:uppercase;">Northwind &amp; Co.</span><span style="color:#f8fafc;display:inline-block;font-size:0.98rem;margin:0 1.1rem;">Free shipping over &euro;50 &middot; 30-day returns &middot; Members earn 2&times; points</span><a href="#" style="background:#f8fafc;border-radius:999px;color:#1e293b;display:inline-block;font-size:0.85rem;font-weight:700;letter-spacing:0.03em;padding:0.45rem 1.15rem;text-decoration:none;">Shop all</a></div>',
	'<div style="background:linear-gradient(90deg,#1e293b 0%,#334155 100%);font-family:system-ui,-apple-system,Segoe UI,sans-serif;padding:0.95rem 1.5rem;text-align:center;"><span style="background:rgba(226,232,240,0.16);border-radius:999px;color:#f8fafc;display:inline-block;font-size:0.78rem;font-weight:700;letter-spacing:0.08em;padding:0.3rem 0.9rem;text-transform:uppercase;">Northwind &amp; Co.</span><span style="color:#f8fafc;display:inline-block;font-size:0.98rem;margin:0 1.1rem;">Free shipping over &euro;50 &middot; 30-day returns &middot; Members earn 2&times; points</span><a href="#" style="background:#f8fafc;border-radius:999px;color:#1e293b;display:inline-block;font-size:0.85rem;font-weight:700;letter-spacing:0.03em;padding:0.45rem 1.15rem;text-decoration:none;">Shop all</a></div>',
];

export const CHRISTMAS_RIBBON_HTML =
	'<div style="background:linear-gradient(90deg,#7f1d1d 0%,#b91c1c 45%,#14532d 100%);font-family:system-ui,-apple-system,Segoe UI,sans-serif;padding:0.95rem 1.5rem;text-align:center;"><span style="background:#fbbf24;border-radius:999px;color:#7f1d1d;display:inline-block;font-size:0.78rem;font-weight:700;letter-spacing:0.08em;padding:0.3rem 0.9rem;text-transform:uppercase;">&#127876; Christmas Sale</span><span style="color:#f8fafc;display:inline-block;font-size:0.98rem;margin:0 1.1rem;">Up to <strong>40% off</strong> &middot; Free gift wrapping &middot; Order by 20 Dec for delivery</span><a href="#" style="background:#fbbf24;border-radius:999px;color:#7f1d1d;display:inline-block;font-size:0.85rem;font-weight:700;letter-spacing:0.03em;padding:0.45rem 1.15rem;text-decoration:none;">Shop gifts</a></div>';

export const HALLOWEEN_RIBBON_HTML =
	'<div style="background:linear-gradient(90deg,#0b0a10 0%,#2e1065 45%,#7c2d12 100%);font-family:system-ui,-apple-system,Segoe UI,sans-serif;padding:0.95rem 1.5rem;text-align:center;"><span style="background:#fb923c;border-radius:999px;color:#1c1917;display:inline-block;font-size:0.78rem;font-weight:700;letter-spacing:0.08em;padding:0.3rem 0.9rem;text-transform:uppercase;">&#127875; Halloween Sale</span><span style="color:#fff7ed;display:inline-block;font-size:0.98rem;margin:0 1.1rem;">One night only &middot; <strong style="color:#fed7aa;">31% off everything</strong> &middot; Free treats tucked into every order</span><a href="#" style="background:#fb923c;border-radius:999px;color:#1c1917;display:inline-block;font-size:0.85rem;font-weight:700;letter-spacing:0.03em;padding:0.45rem 1.15rem;text-decoration:none;">Shop the haunt</a></div>';

export const SEASONAL_BANNER_TITLE = 'Seasonal Banner';

export const SEASONAL_BANNER_VERSIONS = [
	'<p>PUBLISHED BANNER: Shop our regular collection.</p>',
	'<div style="background:linear-gradient(135deg,#0f172a 0%,#1e293b 55%,#334155 100%);border-radius:18px;font-family:system-ui,-apple-system,Segoe UI,sans-serif;overflow:hidden;padding:3.25rem 2.5rem;text-align:center;"><p style="color:#94a3b8;font-size:0.8rem;font-weight:700;letter-spacing:0.22em;margin:0 0 0.9rem;text-transform:uppercase;">New Season Essentials</p><h2 style="color:#fff7ed;font-size:2.7rem;font-weight:800;letter-spacing:-0.02em;line-height:1.1;margin:0 0 1rem;">Shop the Collection</h2><p style="color:#cbd5e1;font-size:1.08rem;line-height:1.6;margin:0 auto 1.9rem;max-width:32rem;">Everyday pieces, built to last. Free shipping over &euro;50 and 30-day returns on everything.</p><a href="#" style="background:#f8fafc;border-radius:999px;color:#0f172a;display:inline-block;font-size:0.95rem;font-weight:700;letter-spacing:0.03em;padding:0.8rem 2.1rem;text-decoration:none;">Browse the store</a></div>',
	'<div style="background:linear-gradient(135deg,#0f172a 0%,#1e293b 55%,#334155 100%);font-family:system-ui,-apple-system,Segoe UI,sans-serif;overflow:hidden;padding:3.25rem 2.5rem;text-align:center;"><p style="color:#94a3b8;font-size:0.8rem;font-weight:700;letter-spacing:0.22em;margin:0 0 0.9rem;text-transform:uppercase;">New Season Essentials</p><h2 style="color:#fff7ed;font-size:2.7rem;font-weight:800;letter-spacing:-0.02em;line-height:1.1;margin:0 0 1rem;">Shop the Collection</h2><p style="color:#cbd5e1;font-size:1.08rem;line-height:1.6;margin:0 auto 1.9rem;max-width:32rem;">Everyday pieces, built to last. Free shipping over &euro;50 and 30-day returns on everything.</p><a href="#" style="background:#f8fafc;border-radius:999px;color:#0f172a;display:inline-block;font-size:0.95rem;font-weight:700;letter-spacing:0.03em;padding:0.8rem 2.1rem;text-decoration:none;">Browse the store</a></div>',
];

export const CHRISTMAS_BANNER_HTML =
	'<div style="background:linear-gradient(135deg,#450a0a 0%,#7f1d1d 40%,#14532d 100%);font-family:system-ui,-apple-system,Segoe UI,sans-serif;overflow:hidden;padding:3.25rem 2.5rem;text-align:center;"><p style="color:#fbbf24;font-size:0.8rem;font-weight:700;letter-spacing:0.22em;margin:0 0 0.9rem;text-transform:uppercase;">&#127876; Christmas Collection</p><h2 style="color:#fff7ed;font-size:2.7rem;font-weight:800;letter-spacing:-0.02em;line-height:1.1;margin:0 0 1rem;">Wrapped, Ready, Delivered</h2><p style="color:#fde68a;font-size:1.08rem;line-height:1.6;margin:0 auto 1.9rem;max-width:32rem;">Up to <strong style="color:#fff7ed;">40% off</strong> the gift edit. Free gift wrapping on every order, and guaranteed delivery when you order by 20 December.</p><a href="#" style="background:#fbbf24;border-radius:999px;color:#7f1d1d;display:inline-block;font-size:0.95rem;font-weight:700;letter-spacing:0.03em;padding:0.8rem 2.1rem;text-decoration:none;">Shop Christmas gifts</a></div>';

export const HALLOWEEN_BANNER_HTML =
	'<div style="background:linear-gradient(135deg,#0b0a10 0%,#2e1065 45%,#7c2d12 100%);font-family:system-ui,-apple-system,Segoe UI,sans-serif;overflow:hidden;padding:3.25rem 2.5rem;text-align:center;"><p style="color:#fb923c;font-size:0.8rem;font-weight:700;letter-spacing:0.22em;margin:0 0 0.9rem;text-transform:uppercase;">&#127875; Halloween Collection</p><h2 style="color:#fff7ed;font-size:2.7rem;font-weight:800;letter-spacing:-0.02em;line-height:1.1;margin:0 0 1rem;">Treat Yourself. We Insist.</h2><p style="color:#fed7aa;font-size:1.08rem;line-height:1.6;margin:0 auto 1.9rem;max-width:32rem;"><strong style="color:#fff7ed;">31% off everything</strong> for one night only. Costumes, candles and things that go bump &mdash; free treats tucked into every order.</p><a href="#" style="background:#fb923c;border-radius:999px;color:#1c1917;display:inline-block;font-size:0.95rem;font-weight:700;letter-spacing:0.03em;padding:0.8rem 2.1rem;text-decoration:none;">Enter if you dare</a></div>';

export const CAMPAIGN_LANDING_PAGE_TITLE = 'Campaign Landing';

export const LIVE_RIBBON_TEXT = 'Free shipping over €50 · 30-day returns';

export const LIVE_BANNER_TEXT = 'Shop the Collection';

export const CHRISTMAS_RIBBON_TEXT = 'Christmas Sale';

export const CHRISTMAS_BANNER_TEXT = 'Wrapped, Ready, Delivered';

export const HALLOWEEN_RIBBON_TEXT = 'Halloween Sale';

export const HALLOWEEN_BANNER_TEXT = 'Treat Yourself. We Insist.';

export const HEADER_TEXT = 'Bag (2)';

export const FOOTER_TEXT = 'All prices include VAT';
