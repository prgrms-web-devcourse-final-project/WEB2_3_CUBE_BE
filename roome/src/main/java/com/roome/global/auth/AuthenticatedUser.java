package com.roome.global.auth;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)  // 메서드 파라미터에만 적용
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthenticatedUser {
}
