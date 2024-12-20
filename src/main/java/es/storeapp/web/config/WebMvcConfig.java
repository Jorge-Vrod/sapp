package es.storeapp.web.config;

import es.storeapp.business.services.OrderService;
import es.storeapp.business.services.UserService;
import es.storeapp.common.ConfigurationParameters;
import es.storeapp.common.Constants;
import es.storeapp.web.interceptors.*;

import java.io.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    ConfigurationParameters configurationParameters;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        
        /* AutoLogin detecting the persistent cookie */
        registry.addInterceptor(new AutoLoginInterceptor(userService, passwordEncoder))
                .addPathPatterns(Constants.ALL_ENDPOINTS)
                .excludePathPatterns(Constants.LIBS_RESOURCES)
                .excludePathPatterns(Constants.EXTERNAL_RESOURCES);
        
        /* CSP interceptor */
        registry.addInterceptor(new CSPInterceptor())
                .addPathPatterns(Constants.ALL_ENDPOINTS)
                .excludePathPatterns(Constants.LIBS_RESOURCES)
                .excludePathPatterns(Constants.EXTERNAL_RESOURCES);
        
        /* Request log interceptor */
        registry.addInterceptor(new LoggerInterceptor());
        
        /* Shopping cart guard */
        registry.addInterceptor(new ShoppingCartInterceptor())
                .addPathPatterns(Constants.ALL_ENDPOINTS)
                .excludePathPatterns(Constants.LIBS_RESOURCES)
                .excludePathPatterns(Constants.EXTERNAL_RESOURCES);
        
        /* Interceptor to protect authenticated resources */
        registry.addInterceptor(new AuthenticatedUserInterceptor())
            .addPathPatterns(Constants.ORDERS_ALL_ENDPOINTS)
            .addPathPatterns(Constants.USER_PROFILE_ALL_ENDPOINTS)
            .addPathPatterns(Constants.CHANGE_PASSWORD_ENDPOINT)
            .addPathPatterns(Constants.COMMENT_PRODUCT_ENDPOINT);

        registry.addInterceptor(new ValidOrderInterceptor(orderService))
                .addPathPatterns(Constants.ORDERS_ALL_ENDPOINTS);
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(Constants.EXTERNAL_RESOURCES)
                .addResourceLocations(new File(configurationParameters.getResources()).toURI().toString());
    }

}
