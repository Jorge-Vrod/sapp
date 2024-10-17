package es.storeapp.web.interceptors;

import es.storeapp.common.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AuthenticatedUserInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler)
            throws Exception {

        HttpSession session = request.getSession();
        if (session.getAttribute(Constants.USER_SESSION) == null) {
            /* FIXED: Sanitize the input to prevent open redirect vulnerability */
            String nextPage = URLEncoder.encode(request.getRequestURI(), StandardCharsets.UTF_8);

            response.sendRedirect(request.getContextPath() + Constants.LOGIN_ENDPOINT +
                    Constants.PARAMS + Constants.NEXT_PAGE + Constants.PARAM_VALUE + nextPage);
            return false;
        }
        return true;
    }

}
