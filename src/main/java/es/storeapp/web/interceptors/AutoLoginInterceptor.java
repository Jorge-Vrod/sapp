package es.storeapp.web.interceptors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.storeapp.business.entities.User;
import es.storeapp.business.services.UserService;
import es.storeapp.business.utils.ValidationUtils;
import es.storeapp.common.Constants;
import es.storeapp.web.cookies.UserInfo;
import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.HandlerInterceptor;

public class AutoLoginInterceptor implements HandlerInterceptor {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AutoLoginInterceptor(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        HttpSession session = request.getSession(true);
        if (session.getAttribute(Constants.USER_SESSION) != null || request.getCookies() == null) {
            return true;
        }
        for (Cookie c : request.getCookies()) {
            if (Constants.PERSISTENT_USER_COOKIE.equals(c.getName())) {
                String cookieValue = c.getValue();
                if (cookieValue == null) {
                    continue;
                }

                Base64.Decoder decoder = Base64.getDecoder();
                String decodedValue = new String(decoder.decode(cookieValue), StandardCharsets.UTF_8);

                String urlDecodedValue = URLDecoder.decode(decodedValue, StandardCharsets.UTF_8);

                try {
                    // Use a safe JSON parser for deserialization
                    ObjectMapper objectMapper = new ObjectMapper();
                    UserInfo userInfo = objectMapper.readValue(urlDecodedValue, UserInfo.class);

                    /* FIXED: Sanitize user input before using it in a query to prevent SQL injection */
                    if (ValidationUtils.validateEmail(userInfo.getEmail())) {
                        User user = userService.findByEmail(userInfo.getEmail());

                        /* FIXED: Use a constant-time password comparison method to prevent timing attacks */
                        if (user != null && passwordEncoder.matches(userInfo.getPassword(), user.getPassword())) {
                            session.setAttribute(Constants.USER_SESSION, user);
                        }
                    }
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }

}
