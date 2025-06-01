package ir.isiran.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class CamundaRestConfig implements WebMvcConfigurer {
    // No additional configuration needed for Spring Boot 2.7.x
} 