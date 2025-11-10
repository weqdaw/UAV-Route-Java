package com.example.java_route.dto;

import lombok.Data;
import java.util.List;

@Data
public class WaypointOptimizeResponse {
    private int code;
    private String message;
    private Data data;
    
    @lombok.Data
    public static class Data {
        private List<Waypoint> waypoints;
        private Stats stats;
    }
    
    @lombok.Data
    public static class Stats {
        private int totalCandidates;
        private int selectedWaypoints;
        private double reductionRate;
        private double coverageRate;
    }
}