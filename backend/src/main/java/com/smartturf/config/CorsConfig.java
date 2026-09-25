package com.smartturf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Cross-Origin Resource Sharing (CORS) & Static Resource Configuration.
 * Uses a CorsFilter bean so it runs at filter level BEFORE any handler,
 * ensuring all origins (including Live Server on 5500, 127.0.0.1, etc.)
 * can reach the API without a 403.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // Allow ALL origins explicitly — safe for local-dev / no auth tokens
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serves all frontend files directly from the frontend directory
        registry.addResourceHandler("/**")
                .addResourceLocations("file:../frontend/", "file:frontend/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Forward root URL to index.html
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}
