//FALTA ARREGLAR ENDPOINTS
package es.storeapp.web.config;

import es.storeapp.common.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Habilitar CSRF pero ignorar recursos estáticos y WebJars
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/resources/fonts/**",
                    "/fonts/**",
                    "/css/**",             // CSS adicionales
                    "/js/**",               // JS adicionales
                    "/static/**",
                    Constants.EXTERNAL_RESOURCES,    // /resources/**
                    Constants.LIBS_RESOURCES         // /webjars/**
                )
            )

/*
            // Permitir acceso sin autenticación a recursos estáticos, rutas públicas y formularios de login/logout
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    Constants.ROOT_ENDPOINT,
                    Constants.LOGIN_ENDPOINT,
                    Constants.LOGOUT_ENDPOINT,
                    Constants.REGISTRATION_ENDPOINT,
                    Constants.RESET_PASSWORD_ENDPOINT,
                    Constants.SEND_EMAIL_ENDPOINT,
                    Constants.PRODUCTS_ENDPOINT,
                    Constants.PRODUCT_ENDPOINT, 
                    "/resources/fonts/**",
                    "/fonts/**",
                    "/css/**",             // CSS adicionales
                    "/js/**",               // JS adicionales
                    "/static/**",
                    Constants.EXTERNAL_RESOURCES,    // /resources/**
                    Constants.LIBS_RESOURCES         // /webjars/**
                ).permitAll()  // Permite el acceso a estas URLs sin autenticación
                
                // Rutas de perfil de usuario, solo accesibles si el usuario está autenticado
                .requestMatchers(Constants.USER_PROFILE_ALL_ENDPOINTS).authenticated()
                .requestMatchers(Constants.USER_PROFILE_ENDPOINT).authenticated()
                .requestMatchers(Constants.USER_PROFILE_IMAGE_ENDPOINT).authenticated()
                .requestMatchers(Constants.USER_PROFILE_IMAGE_REMOVE_ENDPOINT).authenticated()

                // Rutas de ordenes, solo accesibles si el usuario está autenticado
                .requestMatchers(Constants.ORDERS_ALL_ENDPOINTS).authenticated()
                .requestMatchers(Constants.ORDERS_ENDPOINT).authenticated()
                .requestMatchers(Constants.ORDER_ENDPOINT).authenticated()
                .requestMatchers(Constants.ORDER_CONFIRM_ENDPOINT).authenticated()
                .requestMatchers(Constants.ORDER_PAYMENT_ENDPOINT).authenticated()
                .requestMatchers(Constants.ORDER_CANCEL_ENDPOINT).authenticated()

                // Rutas de productos y carrito, pueden estar accesibles a usuarios no autenticados en algunos casos, o a los autenticados
                .requestMatchers(Constants.CART_ENDPOINT).authenticated()
                .requestMatchers(Constants.CART_ADD_PRODUCT_ENDPOINT).authenticated()
                .requestMatchers(Constants.CART_REMOVE_PRODUCT_ENDPOINT).authenticated()
                .requestMatchers(Constants.COMMENT_PRODUCT_ENDPOINT).authenticated()
                
                // Otras rutas públicas o protegidas según sea necesario
                //.anyRequest().authenticated()  // Para cualquier otra URL, es necesario estar autenticado
                .anyRequest().permitAll()       // DEBE ESTAR EN AUTHENTICATED; ESTO ES PARA TESTEAR
            )

            // Configuración de login
            .formLogin(form -> form
                .loginPage(Constants.LOGIN_ENDPOINT)  // Ruta de login
                .permitAll()  // Permite acceso a la página de login
            )
            
            // Configuración de logout
            .logout(logout -> logout
                .logoutUrl(Constants.LOGOUT_ENDPOINT)  // Ruta de logout
                .permitAll()  // Permite acceso a la página de logout
                .invalidateHttpSession(true) // Invalida la sesión al hacer logout
                .clearAuthentication(true)   // Elimina la autenticación al cerrar sesión
            )*/
            ;

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

