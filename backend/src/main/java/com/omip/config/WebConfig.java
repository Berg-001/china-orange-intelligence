package com.omip.config;
import org.springframework.beans.factory.annotation.Value; import org.springframework.context.annotation.*; import org.springframework.web.cors.*; import org.springframework.web.servlet.config.annotation.*;
@Configuration public class WebConfig implements WebMvcConfigurer {
 @Value("${omip.cors.allowed-origin:http://localhost:5173}") String origin;
 public void addCorsMappings(CorsRegistry r){r.addMapping("/**").allowedOrigins(origin).allowedMethods("GET","POST");}
}
