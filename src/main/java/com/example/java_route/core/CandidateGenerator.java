package com.example.java_route.core;

import com.example.java_route.dto.PolygonPoint;
import com.example.java_route.utils.CoordinatesUtils;
import com.example.java_route.utils.GeometryUtils;
import org.locationtech.jts.geom.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CandidateGenerator {
    
    public static List<Map<String, Object>> generateGridCandidates(
            List<PolygonPoint> polygonCoords,
            CameraModel cameraModel,
            double heightM,
            double overlapFront,
            double overlapSide,
            double gimbalPitch,
            double mainAngle) {
        
        // 1. 计算覆盖范围
        Map<String, Double> footprint = cameraModel.footprintAtHeight(heightM, gimbalPitch);
        double Wg = footprint.get("width");   // 侧向覆盖宽度（米）
        double Hg = footprint.get("height");  // 前向覆盖高度（米）
        
        // 2. 计算步长（米）
        double stepForward = Hg * (1 - overlapFront);
        double stepSide = Wg * (1 - overlapSide);
        
        // 3. 构建多边形
        Polygon poly = GeometryUtils.createPolygon(polygonCoords);
        double[] bounds = GeometryUtils.getPolygonBounds(poly);
        
        // 4. 计算参考点
        double refLng = (bounds[0] + bounds[2]) / 2;
        double refLat = (bounds[1] + bounds[3]) / 2;
        
        // 5. 将步长转换为度
        double stepForwardDeg = CoordinatesUtils.metersToDegrees(stepForward, refLat);
        double stepSideDeg = CoordinatesUtils.metersToDegrees(stepSide, refLat);
        
        // 6. 确定扫描方向
        double widthX = bounds[2] - bounds[0];
        double widthY = bounds[3] - bounds[1];
        boolean alongX = widthX >= widthY;
        
        List<Map<String, Object>> candidates = new ArrayList<>();
        int candidateId = 0;
        
        if (alongX) {
            // 沿X轴（经度）扫描
            double y = bounds[1];
            int dir = 1;
            
            while (y <= bounds[3]) {
                double xStart = dir > 0 ? bounds[0] : bounds[2];
                double xEnd = dir > 0 ? bounds[2] : bounds[0];
                double step = dir > 0 ? stepSideDeg : -stepSideDeg;
                
                double x = xStart;
                while ((dir > 0 && x <= xEnd) || (dir < 0 && x >= xEnd)) {
                    if (GeometryUtils.pointInPolygon(x, y, poly)) {
                        Map<String, Object> candidate = new HashMap<>();
                        candidate.put("id", candidateId);
                        candidate.put("lng", x);
                        candidate.put("lat", y);
                        candidate.put("pitch", gimbalPitch);
                        candidates.add(candidate);
                        candidateId++;
                    }
                    x += step;
                }
                y += stepForwardDeg;
                dir *= -1;  // 蛇形扫描
            }
        } else {
            // 沿Y轴（纬度）扫描
            double x = bounds[0];
            int dir = 1;
            
            while (x <= bounds[2]) {
                double yStart = dir > 0 ? bounds[1] : bounds[3];
                double yEnd = dir > 0 ? bounds[3] : bounds[1];
                double step = dir > 0 ? stepForwardDeg : -stepForwardDeg;
                
                double y = yStart;
                while ((dir > 0 && y <= yEnd) || (dir < 0 && y >= yEnd)) {
                    if (GeometryUtils.pointInPolygon(x, y, poly)) {
                        Map<String, Object> candidate = new HashMap<>();
                        candidate.put("id", candidateId);
                        candidate.put("lng", x);
                        candidate.put("lat", y);
                        candidate.put("pitch", gimbalPitch);
                        candidates.add(candidate);
                        candidateId++;
                    }
                    y += step;
                }
                x += stepSideDeg;
                dir *= -1;
            }
        }
        
        return candidates;
    }
}