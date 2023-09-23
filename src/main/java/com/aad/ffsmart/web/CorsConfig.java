package com.aad.ffsmart.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * CORS config
 * <p>
 * Allows requests from all origins (for local development only, not for PROD deployment)
 *
 * @author Oliver Wortley
 */
@Configuration
public class CorsConfig implements WebFluxConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**")
                .allowedOrigins("*").allowedMethods("*").allowedHeaders("*");
    }

}
