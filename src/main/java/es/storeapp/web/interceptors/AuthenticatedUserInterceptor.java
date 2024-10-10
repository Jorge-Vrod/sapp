package es.storeapp.web.interceptors;

import es.storeapp.common.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthenticatedUserInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                             HttpServletResponse response, 
                             Object handler)
            throws Exception {

        HttpSession session = request.getSession();
        if(session.getAttribute(Constants.USER_SESSION) == null) {
            /* VULN : Unsanitized input from the request URL flows into sendRedirect, where it is used as an URL to redirect the user.
            This may result in an Open Redirect vulnerability. */
            response.sendRedirect(request.getContextPath() + Constants.LOGIN_ENDPOINT +
                    Constants.PARAMS + Constants.NEXT_PAGE + Constants.PARAM_VALUE + request.getRequestURL());
            return false;
        }
        return true;
    }
}
