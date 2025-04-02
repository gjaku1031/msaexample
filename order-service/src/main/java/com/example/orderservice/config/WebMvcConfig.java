package com.example.orderservice.config;

import com.example.orderservice.security.AuthorizationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 설정 클래스
 * 
 * 이 클래스는 권한 체크를 위한 인터셉터를 등록합니다.
 * 마이크로서비스 아키텍처에서 컨트롤러 메서드 레벨의 권한 체크를 위해 사용됩니다.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthorizationInterceptor authorizationInterceptor;

    /**
     * 인터셉터 등록
     * 
     * AuthorizationInterceptor를 모든 API 경로에 적용하여 @Secured 어노테이션이 붙은
     * 컨트롤러 메서드의 권한을 체크합니다.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorizationInterceptor)
                .addPathPatterns("/api/**"); // 모든 API 경로에 적용
    }
}