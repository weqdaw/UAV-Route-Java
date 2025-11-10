package com.example.java_route.core;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
public class CameraModel {
    private CameraProfile profile;
    
    public CameraModel(CameraProfile profile) {
        this.profile = profile;
    }
    
    /**
     * 计算像素物理尺寸（µm）
     */
    public double computePixelSizeUm(String axis) {
        if ("width".equals(axis)) {
            return (profile.getSensorWidthMm() * 1000.0) / profile.getResX();
        } else {
            return (profile.getSensorHeightMm() * 1000.0) / profile.getResY();
        }
    }
    
    /**
     * 从GSD计算飞行高度（m）
     */
    public double gsdToHeight(double desiredGsdCm) {
        double pixelSizeUm = computePixelSizeUm("width");
        return (desiredGsdCm / 0.1) * (profile.getFocalLengthMm() / pixelSizeUm);
    }
    
    /**
     * 从高度计算GSD（cm/pixel）
     */
    public double heightToGsd(double heightM) {
        double pixelSizeUm = computePixelSizeUm("width");
        return heightM * (pixelSizeUm / profile.getFocalLengthMm()) * 0.1;
    }
    
    /**
     * 计算地面覆盖范围（m）
     */
    public Map<String, Double> footprintAtHeight(double heightM, double pitchDeg) {
        double hfovRad = 2 * Math.atan(profile.getSensorWidthMm() / (2 * profile.getFocalLengthMm()));
        double vfovRad = 2 * Math.atan(profile.getSensorHeightMm() / (2 * profile.getFocalLengthMm()));
        
        double width, height;
        if (Math.abs(pitchDeg + 90) < 1) {
            width = 2 * heightM * Math.tan(hfovRad / 2);
            height = 2 * heightM * Math.tan(vfovRad / 2);
        } else {
            double pitchRad = Math.toRadians(pitchDeg);
            width = 2 * heightM * Math.tan(hfovRad / 2) / Math.abs(Math.cos(pitchRad));
            height = 2 * heightM * Math.tan(vfovRad / 2) / Math.abs(Math.cos(pitchRad));
        }
        
        Map<String, Double> result = new HashMap<>();
        result.put("width", width);
        result.put("height", height);
        return result;
    }
}