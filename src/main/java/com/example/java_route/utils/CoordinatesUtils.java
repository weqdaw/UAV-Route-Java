package com.example.java_route.utils;

public class CoordinatesUtils {
    
    /**
     * 米转换为度（经度方向）
     * @param meters 米
     * @param lat 纬度（用于计算经度转换）
     * @return 度
     */
    public static double metersToDegrees(double meters, double lat) {
        return meters / (111000.0 * Math.cos(Math.toRadians(lat)));
    }
    
    /**
     * 度转换为米（经度方向）
     * @param degrees 度
     * @param lat 纬度
     * @return 米
     */
    public static double degreesToMeters(double degrees, double lat) {
        return degrees * 111000.0 * Math.cos(Math.toRadians(lat));
    }
    
    /**
     * 将经纬度转换为局部米制坐标（近似）
     */
    public static double[] lngLatToMetersApprox(double lng, double lat, double refLng, double refLat) {
        double dy = (lat - refLat) * 111000.0;  // 南北方向（米）
        double dx = (lng - refLng) * 111000.0 * Math.cos(Math.toRadians(refLat));  // 东西方向（米）
        return new double[]{dx, dy};
    }
    
    /**
     * 米制坐标转回经纬度
     */
    public static double[] metersToLngLatApprox(double x, double y, double refLng, double refLat) {
        double lat = refLat + y / 111000.0;
        double lng = refLng + x / (111000.0 * Math.cos(Math.toRadians(refLat)));
        return new double[]{lng, lat};
    }
}