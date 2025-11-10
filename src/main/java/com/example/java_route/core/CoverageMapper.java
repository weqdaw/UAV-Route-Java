package com.example.java_route.core;

import com.example.java_route.dto.PolygonPoint;
import com.example.java_route.utils.CoordinatesUtils;
import com.example.java_route.utils.GeometryUtils;
import org.locationtech.jts.geom.Polygon;
import java.util.*;

public class CoverageMapper {
    
    /**
     * 将多边形离散化为cells
     */
    public static List<Map<String, Object>> createCells(
            List<PolygonPoint> polygonCoords,
            double cellSizeM) {
        
        Polygon poly = GeometryUtils.createPolygon(polygonCoords);
        double[] bounds = GeometryUtils.getPolygonBounds(poly);
        
        double refLat = (bounds[1] + bounds[3]) / 2;
        double cellSizeDeg = CoordinatesUtils.metersToDegrees(cellSizeM, refLat);
        
        List<Map<String, Object>> cells = new ArrayList<>();
        int cellId = 0;
        
        double minX = bounds[0];
        double minY = bounds[1];
        double maxX = bounds[2];
        double maxY = bounds[3];
        
        for (double x = minX; x <= maxX; x += cellSizeDeg) {
            for (double y = minY; y <= maxY; y += cellSizeDeg) {
                if (GeometryUtils.pointInPolygon(x, y, poly)) {
                    Map<String, Object> cell = new HashMap<>();
                    cell.put("id", cellId);
                    cell.put("centerLng", x);
                    cell.put("centerLat", y);
                    cell.put("area", cellSizeM * cellSizeM);
                    cells.add(cell);
                    cellId++;
                }
            }
        }
        
        return cells;
    }
    
    /**
     * 计算覆盖矩阵：每个候选点覆盖哪些cells
     */
    public static Map<Integer, Set<Integer>> computeCoverageMatrix(
            List<Map<String, Object>> candidates,
            List<Map<String, Object>> cells,
            CameraModel cameraModel,
            double heightM) {
        
        Map<String, Double> footprint = cameraModel.footprintAtHeight(heightM, -90);
        double footprintRadius = Math.sqrt(
            Math.pow(footprint.get("width") / 2, 2) + 
            Math.pow(footprint.get("height") / 2, 2)
        );
        
        Map<Integer, Set<Integer>> coverageMap = new HashMap<>();
        
        for (Map<String, Object> candidate : candidates) {
            int candidateId = (Integer) candidate.get("id");
            double candidateLng = (Double) candidate.get("lng");
            double candidateLat = (Double) candidate.get("lat");
            Set<Integer> coveredCells = new HashSet<>();
            
            for (Map<String, Object> cell : cells) {
                double cellLng = (Double) cell.get("centerLng");
                double cellLat = (Double) cell.get("centerLat");
                
                double distanceDeg = GeometryUtils.distanceDegrees(
                    candidateLng, candidateLat, cellLng, cellLat
                );
                double distanceM = CoordinatesUtils.degreesToMeters(distanceDeg, cellLat);
                
                if (distanceM <= footprintRadius) {
                    coveredCells.add((Integer) cell.get("id"));
                }
            }
            
            coverageMap.put(candidateId, coveredCells);
        }
        
        return coverageMap;
    }
}