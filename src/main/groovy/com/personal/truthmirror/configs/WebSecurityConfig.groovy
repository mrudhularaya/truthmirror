package com.personal.truthmirror.configs

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
class WebSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        /*This is where we configure the security required for our endpoints and setup our app to serve as
                an OAuth2 Resource Server, using JWT validation. */
        return http
                .csrf( csrf ->
                    csrf.disable()
                )
                .authorizeHttpRequests ((authorize) ->
                    authorize
                        .requestMatchers("/api/journals/**").authenticated()
                )
                .cors(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )
                .build()
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration()
        configuration.addAllowedOrigin("http://localhost:5173") // Replace with your UI's URL
        configuration.addAllowedMethod("*") // Allow all HTTP methods
        configuration.addAllowedHeader("*") // Allow all headers
        configuration.setAllowCredentials(true) // Allow credentials (e.g., cookies)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
