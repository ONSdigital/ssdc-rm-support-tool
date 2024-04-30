package uk.gov.ons.ssdc.supporttool.security;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class UserIdentityInterceptor implements HandlerInterceptor {
  private final AuthUser authUser;

  private static final Logger log = LoggerFactory.getLogger(UserIdentityInterceptor.class);

  public UserIdentityInterceptor(AuthUser authUser) {
    this.authUser = authUser;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    if ("/api/upload".equals(request.getRequestURI())) {
      // Don't bother with identity for file uploads because the JWT claim expires after 10 mins
      return true;
    }
    String jwtToken = request.getHeader("x-goog-iap-jwt-assertion");
    String userEmail = authUser.getUserEmail(jwtToken);
    request.setAttribute("userEmail", userEmail);
    return true;
  }

  @Override
  public void postHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      ModelAndView modelAndView)
      throws Exception {
    log.with("audit", true)
        .with("userEmail", request.getAttribute("userEmail"))
        .with("requestURI", request.getRequestURI())
        .with("requestMethod", request.getMethod())
        .with("responseStatus", response.getStatus())
        .info("API Audit");
  }
}
