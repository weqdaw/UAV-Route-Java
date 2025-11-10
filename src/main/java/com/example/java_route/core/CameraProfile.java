package com.example.java_route.core;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CameraProfile {
    private double sensorWidthMm;
    private double sensorHeightMm;
    private int resX;
    private int resY;
    private double focalLengthMm;
    
    // Matrice 4T 相机配置
    public static final CameraProfile WIDE = new CameraProfile(
        9.6, 7.2, 8064, 6048, 24
    );
    
    public static final CameraProfile MEDIUM_TELE = new CameraProfile(
        9.6, 7.2, 8064, 6048, 70
    );
    
    public static final CameraProfile TELE = new CameraProfile(
        12.8, 9.6, 8192, 6144, 168
    );
    
    public static CameraProfile getProfile(String mode) {
        return switch (mode.toLowerCase()) {
            case "wide" -> WIDE;
            case "medium_tele" -> MEDIUM_TELE;
            case "tele" -> TELE;
            default -> WIDE;
        };
    }
}