package com.smartturf.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cross-Origin Resource Sharing (CORS) & Static Resource Configuration.
 * 1. Allows frontend HTML/JS running on localhost servers (e.g. port 3000, 5500)
 *    to communicate seamlessly with the Spring Boot REST backend.
 * 2. Also serves the 'frontend' folder directly via Spring Boot at http://localhost:8080/
 *    so developers do not need to install Python or Node.js to run the web interface!
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
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
