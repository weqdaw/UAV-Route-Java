package com.example.java_route.utils;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import java.util.List;

public class GeometryUtils {
    
    private static final GeometryFactory geometryFactory = new GeometryFactory();
    
    /**
     * 判断点是否在多边形内
     */
    public static boolean pointInPolygon(double x, double y, Polygon polygon) {
        Point point = geometryFactory.createPoint(new Coordinate(x, y));
        return polygon.contains(point) || polygon.touches(point);
    }
    
    /**
     * 从坐标列表创建多边形
     */
    public static Polygon createPolygon(List<com.example.java_route.dto.PolygonPoint> coords) {
        Coordinate[] coordinates = new Coordinate[coords.size() + 1];
        for (int i = 0; i < coords.size(); i++) {
            coordinates[i] = new Coordinate(coords.get(i).getLongitude(), coords.get(i).getLatitude());
        }
        // 闭合多边形
        coordinates[coords.size()] = coordinates[0];
        return geometryFactory.createPolygon(coordinates);
    }
    
    /**
     * 计算多边形包围盒
     */
    public static double[] getPolygonBounds(Polygon polygon) {
        Envelope envelope = polygon.getEnvelopeInternal();
        return new double[]{
            envelope.getMinX(),  // min_lng
            envelope.getMinY(),  // min_lat
            envelope.getMaxX(),  // max_lng
            envelope.getMaxY()   // max_lat
        };
    }
    
    /**
     * 计算两点间距离（度）
     */
    public static double distanceDegrees(double lng1, double lat1, double lng2, double lat2) {
        double dx = lng1 - lng2;
        double dy = lat1 - lat2;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * 计算两点间距离（米，近似）
     */
    public static double distanceMeters(double lng1, double lat1, double lng2, double lat2) {
        double lat = (lat1 + lat2) / 2.0;
        double distanceDeg = distanceDegrees(lng1, lat1, lng2, lat2);
        return degreesToMeters(distanceDeg, lat);
    }
    
    private static double degreesToMeters(double degrees, double lat) {
        return degrees * 111000.0 * Math.cos(Math.toRadians(lat));
    }
}