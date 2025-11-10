package com.example.java_route.dto;

import lombok.Data;
import java.util.List;

@Data
public class WaypointOptimizeRequest {
    private List<PolygonPoint> polygonCoords;
    private double flightHeightM;
    private Double gsdCm;
    private double overlapFront = 0.6;
    private double overlapSide = 0.4;
    private double gimbalPitch = -90;
    private double mainAngle = 0;
    private String cameraMode = "wide";
    private boolean useSetCover = true;
    private boolean useTsp = true;
}