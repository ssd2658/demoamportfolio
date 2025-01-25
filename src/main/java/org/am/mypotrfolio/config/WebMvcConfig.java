package org.am.mypotrfolio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Map root paths to index view
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/home").setViewName("index");
        registry.addViewController("/index").setViewName("index");
        
        // Explicitly map portfolio views
        registry.addViewController("/view-portfolio").setViewName("portfolio-view");
        registry.addViewController("/portfolio-view").setViewName("portfolio-view");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // WebJars resource handling
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                .resourceChain(false);

        // Static resources
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(false);
    }
}
