package com.roome.domain.furniture.entity;

import java.util.Map;
import java.util.HashMap;

public class FurnitureCapacity {

    private static final Map<FurnitureType, Map<Integer, Integer>> capacityMap = new HashMap<>();

    static {
        capacityMap.put(FurnitureType.BOOKSHELF, Map.of(
                1, 5,
                2, 10,
                3, 15
        ));
        capacityMap.put(FurnitureType.CD_RACK, Map.of(
                1, 5,
                2, 10,
                3, 15
        ));
    }

    public static int getCapacity(FurnitureType type, int level) {
        return capacityMap.getOrDefault(type, Map.of()).getOrDefault(level, 0);
    }

}
