package com.roome.domain.furniture.entity;

import java.util.HashMap;
import java.util.Map;

public class FurnitureUpgradePrice {

    private static final Map<FurnitureType, Map<Integer, Integer>> capacityMap = new HashMap<>();

    static {
        capacityMap.put(FurnitureType.BOOKSHELF, Map.of(
                1, 500,
                2, 1500
        ));
        capacityMap.put(FurnitureType.CD_RACK, Map.of(
                1, 500,
                2, 1500
        ));
    }

    public static int getPrice(FurnitureType type, int level) {
        return capacityMap.getOrDefault(type, Map.of()).getOrDefault(level, 0);
    }
}
