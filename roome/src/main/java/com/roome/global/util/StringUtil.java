package com.roome.global.util;

import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public abstract class StringUtil {

    public static List<String> convertStringToList(String values) {
        if (StringUtils.hasText(values)) {
            String decodedValues = URLDecoder.decode(values, StandardCharsets.UTF_8);
            return Arrays.asList(decodedValues.split(","));
        }
        return List.of();
    }
}
