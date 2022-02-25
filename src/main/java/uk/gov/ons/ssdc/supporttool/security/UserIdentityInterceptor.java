package uk.gov.ons.ssdc.supporttool.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class UserIdentityInterceptor implements HandlerInterceptor {
  private final UserIdentity userIdentity;

  public UserIdentityInterceptor(UserIdentity userIdentity) {
    this.userIdentity = userIdentity;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    if ("/api/upload".equals(request.getRequestURI())) {
      // Don't bother with identity for file uploads because the JWT claim expires after 10 mins
      return true;
    }
    String jwtToken = request.getHeader("x-goog-iap-jwt-assertion");
    String userEmail = userIdentity.getUserEmail(jwtToken);
    request.setAttribute("userEmail", userEmail);
    return true;
  }
}
