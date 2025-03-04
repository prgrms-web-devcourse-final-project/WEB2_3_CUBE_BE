package com.roome.domain.furniture.entity;

import java.util.Map;
import java.util.HashMap;
import org.springframework.stereotype.Component;

@Component
public class FurnitureCapacity {

  private static final Map<FurnitureType, Map<Integer, Integer>> capacityMap = new HashMap<>();

  static {
    capacityMap.put(FurnitureType.BOOKSHELF, Map.of(
        1, 20,
        2, 40,
        3, 100
    ));
    capacityMap.put(FurnitureType.CD_RACK, Map.of(
        1, 20,
        2, 40,
        3, 100
    ));
  }

  public static int getCapacity(FurnitureType type, int level) {
    return capacityMap.getOrDefault(type, Map.of()).getOrDefault(level, 0);
  }

  public static int getMaxCdCapacity(int level) {
    return switch (level) {
      case 1 -> 20;
      case 2 -> 40;
      case 3 -> 60;
      default -> throw new IllegalArgumentException("잘못된 CD_RACK 레벨입니다: " + level);
    };
  }

}
