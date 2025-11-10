package com.example.java_route.core;

import java.util.*;

public class SetCoverSolver {
    
    /**
     * 贪心集合覆盖算法
     */
    public static List<Integer> greedySetCover(
            Map<Integer, Set<Integer>> coverageMap,
            List<Map<String, Object>> cells) {
        
        Set<Integer> uncovered = new HashSet<>();
        for (Map<String, Object> cell : cells) {
            uncovered.add((Integer) cell.get("id"));
        }
        
        List<Integer> selected = new ArrayList<>();
        Map<Integer, Set<Integer>> coverageMapCopy = new HashMap<>();
        for (Map.Entry<Integer, Set<Integer>> entry : coverageMap.entrySet()) {
            coverageMapCopy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        
        int iteration = 0;
        int maxIterations = cells.size();
        
        while (!uncovered.isEmpty() && iteration < maxIterations) {
            Integer bestCandidate = null;
            int bestGain = 0;
            
            for (Map.Entry<Integer, Set<Integer>> entry : coverageMapCopy.entrySet()) {
                Integer candidateId = entry.getKey();
                if (selected.contains(candidateId)) {
                    continue;
                }
                
                Set<Integer> coveredCells = entry.getValue();
                Set<Integer> intersection = new HashSet<>(coveredCells);
                intersection.retainAll(uncovered);
                int gain = intersection.size();
                
                if (gain > bestGain) {
                    bestGain = gain;
                    bestCandidate = candidateId;
                }
            }
            
            if (bestCandidate == null || bestGain == 0) {
                break;
            }
            
            selected.add(bestCandidate);
            uncovered.removeAll(coverageMapCopy.get(bestCandidate));
            iteration++;
        }
        
        return selected;
    }
}