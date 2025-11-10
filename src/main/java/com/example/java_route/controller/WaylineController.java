package com.example.java_route.controller;

import com.example.java_route.dto.WaypointOptimizeRequest;
import com.example.java_route.dto.WaypointOptimizeResponse;
import com.example.java_route.core.MissionOptimizer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WaylineController {
    
    private final MissionOptimizer missionOptimizer;
    
    @PostMapping("/wayline/optimize")
    public ResponseEntity<WaypointOptimizeResponse> optimizeWayline(
            @RequestBody WaypointOptimizeRequest request) {
        try {
            // 验证输入
            if (request.getPolygonCoords() == null || request.getPolygonCoords().size() < 3) {
                WaypointOptimizeResponse errorResponse = new WaypointOptimizeResponse();
                errorResponse.setCode(400);
                errorResponse.setMessage("多边形至少需要3个顶点");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 调用优化器
            WaypointOptimizeResponse response = missionOptimizer.optimize(request);
            
            if (response.getCode() != 0) {
                return ResponseEntity.status(response.getCode()).body(response);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            WaypointOptimizeResponse errorResponse = new WaypointOptimizeResponse();
            errorResponse.setCode(400);
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            WaypointOptimizeResponse errorResponse = new WaypointOptimizeResponse();
            errorResponse.setCode(500);
            errorResponse.setMessage("内部错误: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "ok");
        health.put("service", "wayline-optimizer");
        health.put("version", "1.0.0");
        return ResponseEntity.ok(health);
    }
    
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> info = new HashMap<>();
        info.put("message", "无人机航点优化API");
        info.put("health", "/health");
        info.put("api", "/api/v1/wayline/optimize");
        return ResponseEntity.ok(info);
    }
}