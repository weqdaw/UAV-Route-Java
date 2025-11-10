package com.example.java_route.core;

import com.example.java_route.dto.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MissionOptimizer {
    
    /**
     * 主优化函数
     */
    public WaypointOptimizeResponse optimize(WaypointOptimizeRequest request) {
        try {
            // 1. 初始化相机模型
            CameraProfile profile = CameraProfile.getProfile(request.getCameraMode());
            CameraModel camera = new CameraModel(profile);
            
            // 2. 验证GSD（如果提供）
            if (request.getGsdCm() != null) {
                double computedHeight = camera.gsdToHeight(request.getGsdCm());
                if (Math.abs(request.getFlightHeightM() - computedHeight) > 10) {
                    System.out.printf("警告：飞行高度 %.1fm 与GSD %.2fcm对应高度 %.1fm 不一致%n",
                        request.getFlightHeightM(), request.getGsdCm(), computedHeight);
                }
            }
            
            // 3. 生成候选点
            List<Map<String, Object>> candidates = CandidateGenerator.generateGridCandidates(
                request.getPolygonCoords(),
                camera,
                request.getFlightHeightM(),
                request.getOverlapFront(),
                request.getOverlapSide(),
                request.getGimbalPitch(),
                request.getMainAngle()
            );
            
            if (candidates.isEmpty()) {
                return createErrorResponse("无法生成候选点", 0, 0);
            }
            
            // 4. 集合覆盖优化（可选）
            List<Map<String, Object>> selectedCandidates = candidates;
            if (request.isUseSetCover() && candidates.size() > 10) {
                try {
                    Map<String, Double> footprint = camera.footprintAtHeight(
                        request.getFlightHeightM(), request.getGimbalPitch()
                    );
                    double cellSizeM = Math.max(
                        footprint.get("width"), footprint.get("height")
                    ) * 0.5;  // 默认cell_size_factor
                    
                    List<Map<String, Object>> cells = CoverageMapper.createCells(
                        request.getPolygonCoords(), cellSizeM
                    );
                    
                    if (!cells.isEmpty()) {
                        Map<Integer, Set<Integer>> coverageMap = 
                            CoverageMapper.computeCoverageMatrix(
                                candidates, cells, camera, request.getFlightHeightM()
                            );
                        
                        List<Integer> selectedIds = SetCoverSolver.greedySetCover(
                            coverageMap, cells
                        );
                        
                        selectedCandidates = candidates.stream()
                            .filter(c -> selectedIds.contains(c.get("id")))
                            .collect(Collectors.toList());
                    }
                } catch (Exception e) {
                    System.err.println("集合覆盖优化失败，使用全部候选点: " + e.getMessage());
                }
            }
            
            // 5. TSP路径优化（可选）
            List<Map<String, Object>> orderedCandidates = selectedCandidates;
            if (request.isUseTsp() && selectedCandidates.size() > 2) {
                try {
                    List<Integer> orderedIndices = TspSolver.solveTsp(selectedCandidates);
                    orderedCandidates = new ArrayList<>();
                    for (Integer idx : orderedIndices) {
                        orderedCandidates.add(selectedCandidates.get(idx));
                    }
                } catch (Exception e) {
                    System.err.println("TSP优化失败，保持原始顺序: " + e.getMessage());
                }
            }
            
            // 6. 转换为最终航点格式
            List<Waypoint> waypoints = orderedCandidates.stream()
                .map(c -> {
                    Waypoint wp = new Waypoint();
                    wp.setLng((Double) c.get("lng"));
                    wp.setLat((Double) c.get("lat"));
                    wp.setHeight(request.getFlightHeightM());
                    wp.setPitch((Double) c.get("pitch"));
                    return wp;
                })
                .collect(Collectors.toList());
            
            // 7. 计算统计信息
            double reductionRate = 0;
            if (candidates.size() > 0) {
                reductionRate = 1.0 - (double) waypoints.size() / candidates.size();
            }
            
            WaypointOptimizeResponse response = new WaypointOptimizeResponse();
            response.setCode(0);
            response.setMessage("success");
            
            WaypointOptimizeResponse.Data data = new WaypointOptimizeResponse.Data();
            data.setWaypoints(waypoints);
            
            WaypointOptimizeResponse.Stats stats = new WaypointOptimizeResponse.Stats();
            stats.setTotalCandidates(candidates.size());
            stats.setSelectedWaypoints(waypoints.size());
            stats.setReductionRate(reductionRate);
            stats.setCoverageRate(1.0);
            
            data.setStats(stats);
            response.setData(data);
            
            return response;
            
        } catch (Exception e) {
            return createErrorResponse("内部错误: " + e.getMessage(), 0, 0);
        }
    }
    
    private WaypointOptimizeResponse createErrorResponse(
            String message, int totalCandidates, int selectedWaypoints) {
        WaypointOptimizeResponse response = new WaypointOptimizeResponse();
        response.setCode(500);
        response.setMessage(message);
        
        WaypointOptimizeResponse.Data data = new WaypointOptimizeResponse.Data();
        data.setWaypoints(new ArrayList<>());
        
        WaypointOptimizeResponse.Stats stats = new WaypointOptimizeResponse.Stats();
        stats.setTotalCandidates(totalCandidates);
        stats.setSelectedWaypoints(selectedWaypoints);
        stats.setReductionRate(0);
        stats.setCoverageRate(0);
        
        data.setStats(stats);
        response.setData(data);
        return response;
    }
}