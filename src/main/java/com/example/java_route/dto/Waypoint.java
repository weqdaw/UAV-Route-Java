package com.example.java_route.dto;

import lombok.Data;

@Data
public class Waypoint {
    private double lng;
    private double lat;
    private double height;
    private double pitch;
}