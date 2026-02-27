/*
* Experience API (xAPI) LMS Integration
*
* Need Help or Have Questions?
* Please use the PDN Developer Community at https://community.pdn.pearson.com
*
* @category   LearningStudio Sample Application - xAPI-LMS-Integration
* @author     Wes Williams <wes.williams@pearson.com>
* @author     Pearson Developer Services Team <apisupport@pearson.com>
* @copyright  2015 Pearson Education, Inc.
* @license    http://www.apache.org/licenses/LICENSE-2.0  Apache 2.0
* @version    1.0
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

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.*;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;

import org.imsglobal.lti.BasicLTIUtil;
import org.imsglobal.lti.launch.LtiVerificationResult;

// Performs LTI before generating the Launch Link for provided Activity Provider
@SuppressWarnings("serial")
public class SSOServlet extends HttpServlet {

	private static final Logger LOGGER = Logger.getLogger(SSOServlet.class.getName());

	/**
	 * Allowed redirect domains. In production, this should be configured
	 * externally (e.g., via init-param or environment variable).
	 */
	private static final Set<String> ALLOWED_REDIRECT_DOMAINS = new HashSet<>(Arrays.asList(
			// Add trusted activity provider domains here
	));

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			// verify consumer key
			String ltiKey = this.getInitParameter("ltiConsumerKey");
			if(!ltiKey.equals(request.getParameter("oauth_consumer_key"))) {
				// TODO - consider redirecting to launch_presentation_return_url if present
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Unauthorized");
				return;
			}
					
			// verify SSO with Basic LTI
			String ssoEndpoint = request.getRequestURL().toString(); // TODO - better to use parameter?
			String ltiSecret = this.getInitParameter("ltiSharedSecret");
			LtiVerificationResult ltiResult = BasicLTIUtil.validateMessage(request,ssoEndpoint,ltiSecret);
			if(!ltiResult.getSuccess()) {
				// TODO - consider redirecting to launch_presentation_return_url if present
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Unauthorized");
				return;
			}
			
			// load the parameters 
			String activityProvider = request.getParameter("custom_xapi_ap_url");
			String email =  request.getParameter("lis_person_contact_email_primary");
			String fullName = request.getParameter("lis_person_name_full");	
			String userId = request.getParameter("user_id");
			
			// validate the incoming data has the expected data
			if(activityProvider==null || activityProvider.trim().length()==0 ||
					email==null || email.trim().length()==0 ||
					fullName==null || fullName.trim().length()==0 ||
					userId==null || userId.trim().length()==0) {
				// TODO - consider redirecting to launch_presentation_return_url if present
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Missing Data");
				return;
			}
			
			// the parameter is passed double encoded, so decode it once more.
			activityProvider = URLDecoder.decode(activityProvider,"UTF-8");

			// Security: validate redirect URL to prevent open redirect attacks
			try {
				URI redirectUri = new URI(activityProvider);
				String host = redirectUri.getHost();
				if (host == null || (!ALLOWED_REDIRECT_DOMAINS.isEmpty() && !ALLOWED_REDIRECT_DOMAINS.contains(host.toLowerCase()))) {
					LOGGER.log(Level.WARNING, "Blocked redirect to untrusted domain: {0}", host);
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Untrusted redirect target");
					return;
				}
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Invalid activity provider URL", e);
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid redirect URL");
				return;
			}

			// validate the incoming data is valid
			try {
				// userId is expected to be numeric for LearningStudio (TODO - change accordingly)
				Long.parseLong(userId);
		
				// activity provider url must be valid
				UrlValidator urlValidator = new UrlValidator(new String[]{"http","https"});
				if(!urlValidator.isValid(activityProvider)) throw new RuntimeException();
				
				// learner email must be valid
				EmailValidator emailValidator = EmailValidator.getInstance();
				if(!emailValidator.isValid(email)) throw new RuntimeException();
	
				// simple name validation (TODO - handle more complex names)
				if(!fullName.matches("[a-zA-Z .,-]+")) throw new RuntimeException();
			}
			catch(Exception e) {
				// TODO - consider redirecting to launch_presentation_return_url if present
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Invalid Data");
				return;
			}
			
			// generate and save secret for session
			String sessionSecret = UUID.randomUUID().toString();
			SessionDatabase.save(userId, sessionSecret);
			
			// prepare auth for launch link
			String basicAuth = new String(Base64.encodeBase64((userId + ":" + sessionSecret).getBytes("UTF-8")),"UTF-8");
			basicAuth = URLEncoder.encode("Basic " + basicAuth, "UTF-8");
				
			// prepare endpoint for launch link
			String xapiEndpoint = ssoEndpoint.substring(0,ssoEndpoint.length() - request.getServletPath().length()) + "/xapi";
			xapiEndpoint = URLEncoder.encode(xapiEndpoint, "UTF-8");
			
			// prepare actor for launch link
			String actor = "{\"mbox\":\"mailto:"+email+"\",\"name\":\""+fullName+"\",\"objectType\":\"Agent\"}";
			actor = URLEncoder.encode(actor, "UTF-8");
			
			// append the appropriate first delimiter
			if(activityProvider.indexOf("?") == -1) {
				activityProvider += "?";
			}
			else {
				activityProvider += "&";
			}
			
			// add launch link parameters
			activityProvider += "auth=" + basicAuth;
			activityProvider += "&endpoint=" + xapiEndpoint;
			activityProvider += "&actor=" + actor;
				
			response.sendRedirect(activityProvider);
		}
		catch(Exception e) {
			LOGGER.log(Level.SEVERE, "Unexpected error processing SSO request", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Server Error");
		}
	}
}
