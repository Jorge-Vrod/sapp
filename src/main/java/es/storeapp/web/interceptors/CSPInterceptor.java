package es.storeapp.web.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class CSPInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                        "img-src 'self' data:; " +
                        "script-src 'self' 'sha256-<jquery-sha256>' 'sha256-<bootstrap-sha256>' 'sha256-<fontawesome-sha256>' 'sha256-<jqueryform-sha256>' 'sha256-<datatables-sha256>' 'sha256-<datatables-responsive-sha256>' 'sha256-<star-rating-sha256>' 'sha256-<inputmask-sha256>' 'sha256-<scripts-sha256>'; " + // Allow specific scripts
                        "style-src 'self' 'sha256-<bootstrap-css-sha256>' 'sha256-<form-validator-css-sha256>' 'sha256-<datatables-css-sha256>' 'sha256-<star-rating-css-sha256>' 'sha256-<styles-sha256>' 'unsafe-inline'; " +  // Allow specific styles
                        "font-src 'self' data:; " +
                        "connect-src 'self'; " +
                        "object-src 'none'; " +
                        "frame-ancestors 'none'; " +
                        "form-action 'self'; " +
                        "base-uri 'self';"
        );
        return true;
    }
    
}
