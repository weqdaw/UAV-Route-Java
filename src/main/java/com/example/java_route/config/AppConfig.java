package com.example.java_route.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class AppConfig {
    @Value("${app.api.title:航点优化API}")
    private String apiTitle;
    
    @Value("${app.api.version:1.0.0}")
    private String apiVersion;
    
    @Value("${app.cors.allowed-origins:*}")
    private String corsAllowedOrigins;
    
    @Value("${app.algorithm.default-overlap-front:0.6}")
    private double defaultOverlapFront;
    
    @Value("${app.algorithm.default-overlap-side:0.4}")
    private double defaultOverlapSide;
    
    @Value("${app.algorithm.default-camera-mode:wide}")
    private String defaultCameraMode;
    
    @Value("${app.algorithm.default-cell-size-factor:0.5}")
    private double defaultCellSizeFactor;
}