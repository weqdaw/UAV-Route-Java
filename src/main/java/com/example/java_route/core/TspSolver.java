package com.example.java_route.core;

import java.util.*;

public class TspSolver {
    
    /**
     * 计算距离矩阵（度）
     */
    public static double[][] computeDistanceMatrix(List<Map<String, Object>> points) {
        int n = points.size();
        double[][] distanceMatrix = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double lng1 = (Double) points.get(i).get("lng");
                double lat1 = (Double) points.get(i).get("lat");
                double lng2 = (Double) points.get(j).get("lng");
                double lat2 = (Double) points.get(j).get("lat");
                
                double dx = lng1 - lng2;
                double dy = lat1 - lat2;
                double dist = Math.sqrt(dx * dx + dy * dy);
                
                distanceMatrix[i][j] = dist;
                distanceMatrix[j][i] = dist;
            }
        }
        
        return distanceMatrix;
    }
    
    /**
     * TSP求解入口
     */
    public static List<Integer> solveTsp(List<Map<String, Object>> points) {
        if (points.size() <= 2) {
            List<Integer> result = new ArrayList<>();
            for (int i = 0; i < points.size(); i++) {
                result.add(i);
            }
            return result;
        }
        
        try {
            return solveTspOrtools(points);
        } catch (Exception e) {
            System.err.println("OR-Tools求解失败，使用最近邻算法: " + e.getMessage());
            return nearestNeighborTsp(points);
        }
    }
    
    /**
     * 使用OR-Tools求解TSP
     */
    private static List<Integer> solveTspOrtools(List<Map<String, Object>> points) {
        // 这里需要导入OR-Tools的类
        // 由于OR-Tools Java API比较复杂，这里提供一个简化版本
        // 实际使用时需要正确配置OR-Tools依赖
        
        // 如果OR-Tools不可用，回退到最近邻
        return nearestNeighborTsp(points);
    }
    
    /**
     * 最近邻TSP（简单启发式）
     */
    public static List<Integer> nearestNeighborTsp(List<Map<String, Object>> points) {
        int n = points.size();
        if (n <= 1) {
            List<Integer> result = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                result.add(i);
            }
            return result;
        }
        
        boolean[] visited = new boolean[n];
        List<Integer> route = new ArrayList<>();
        route.add(0);
        visited[0] = true;
        
        for (int step = 0; step < n - 1; step++) {
            int current = route.get(route.size() - 1);
            int nearest = -1;
            double minDist = Double.MAX_VALUE;
            
            double lng1 = (Double) points.get(current).get("lng");
            double lat1 = (Double) points.get(current).get("lat");
            
            for (int i = 0; i < n; i++) {
                if (!visited[i]) {
                    double lng2 = (Double) points.get(i).get("lng");
                    double lat2 = (Double) points.get(i).get("lat");
                    
                    double dx = lng1 - lng2;
                    double dy = lat1 - lat2;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    
                    if (dist < minDist) {
                        minDist = dist;
                        nearest = i;
                    }
                }
            }
            
            if (nearest != -1) {
                route.add(nearest);
                visited[nearest] = true;
            }
        }
        
        return route;
    }
}