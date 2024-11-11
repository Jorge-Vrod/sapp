//FALTA ARREGLAR ENDPOINTS
package es.storeapp.web.config;

import es.storeapp.common.Constants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;

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
            );
        return http.build();
    }
}

