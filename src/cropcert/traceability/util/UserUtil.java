package cropcert.traceability.util;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import org.pac4j.core.profile.CommonProfile;

import cropcert.traceability.filter.SecurityInterceptor;

public class UserUtil {
	private UserUtil() {

	}

	public static CommonProfile getUserDetails(HttpServletRequest request) {
		String authorizationHeader = (request).getHeader(HttpHeaders.AUTHORIZATION);
		String token = authorizationHeader.substring("Bearer".length()).trim();
		return SecurityInterceptor.jwtAuthenticator.validateToken(token);
	}
}
