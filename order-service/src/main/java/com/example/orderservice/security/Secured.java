package com.example.orderservice.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드 레벨 권한 체크를 위한 어노테이션
 * 
 * 이 어노테이션은 인터셉터를 통해 처리되며, 지정된 권한이 있는 사용자만 메서드에 접근할 수 있도록 합니다.
 * MSA 환경에서 일관된 권한 체크를 위해 JWT 토큰의 roles 클레임을 확인합니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Secured {

    /**
     * 필요한 권한 목록을 지정합니다.
     * 
     * 여러 권한을 지정하면 OR 조건으로 처리됩니다.
     * 즉, 사용자가 지정된 권한 중 하나라도 가지고 있으면 메서드 접근이 허용됩니다.
     * 
     * 권한은 "ROLE_XXX" 형식의 역할 또는 "RESOURCE:ACTION" 형식의 권한으로 지정할 수 있습니다.
     * 
     * @return 필요한 권한 목록
     */
    String[] value();
}