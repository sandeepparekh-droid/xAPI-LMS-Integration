/*
* Experience API (xAPI) LMS Integration
*
* @category   LearningStudio Sample Application - xAPI-LMS-Integration
* @copyright  2015 Pearson Education, Inc.
* @license    http://www.apache.org/licenses/LICENSE-2.0  Apache 2.0
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.pearson.developer.xapi.proxy;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;

/**
 * Adds security-related HTTP response headers to all responses.
 * Mitigates common web vulnerabilities including XSS, clickjacking,
 * MIME-type sniffing, and content injection attacks.
 */
public class SecurityHeadersFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// No initialization required
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws java.io.IOException, ServletException {

		HttpServletResponse httpResponse = (HttpServletResponse) response;

		// Prevent clickjacking attacks
		httpResponse.setHeader("X-Frame-Options", "DENY");

		// Enable browser XSS protection
		httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

		// Prevent MIME-type sniffing
		httpResponse.setHeader("X-Content-Type-Options", "nosniff");

		// Content Security Policy
		httpResponse.setHeader("Content-Security-Policy",
				"default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self'; frame-ancestors 'none'");

		// Referrer policy
		httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

		// Permissions policy
		httpResponse.setHeader("Permissions-Policy", "geolocation=(), camera=(), microphone=()");

		// Cache control for sensitive data
		httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
		httpResponse.setHeader("Pragma", "no-cache");

		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// No cleanup required
	}
}
