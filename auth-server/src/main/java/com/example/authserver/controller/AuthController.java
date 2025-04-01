package com.example.authserver.controller;

import com.example.authserver.dto.JwtResponse;
import com.example.authserver.dto.LoginRequest;
import com.example.authserver.dto.UserRegistrationDto;
import com.example.authserver.dto.TokenRefreshRequest;
import com.example.authserver.entity.User;
import com.example.authserver.security.JwtUtils;
import com.example.authserver.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 인증 컨트롤러
 * 사용자 로그인 및 토큰 관리와 관련된 HTTP 엔드포인트를 제공합니다.
 * 
 * MSA에서의 AuthController 역할:
 * - 중앙 인증 서비스: 모든 마이크로서비스의 인증 요청을 처리하는 중앙 집중식 인증 지점입니다.
 * - 토큰 발급 및 갱신: JWT 토큰을 발급하고 갱신하여 마이크로서비스 간의 인증 상태를 유지합니다.
 * - 사용자 관리: 사용자 등록 및 관리 기능을 제공합니다.
 * 
 * API 게이트웨이와의 관계:
 * - 일반적으로 클라이언트는 API 게이트웨이를 통해 이 컨트롤러의 엔드포인트에 접근합니다.
 * - 게이트웨이는 인증이 필요한 요청을 이 인증 서버로 리디렉션하거나 프록시합니다.
 * - 인증된 토큰은 게이트웨이에 의해 검증되고 필요한 마이크로서비스로 전달됩니다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    /**
     * 사용자 로그인을 처리합니다.
     * 유효한 자격 증명을 제공하면 액세스 토큰과 리프레시 토큰을 발급합니다.
     * 
     * MSA에서의 의미:
     * - 이 엔드포인트는 모든 마이크로서비스에 대한 통합 로그인 지점입니다.
     * - 발급된 토큰은 모든 마이크로서비스에서 사용자 인증에 사용됩니다.
     * - 토큰에는 사용자 식별 정보와 권한이 포함되어 마이크로서비스에서 접근 제어 결정을 내릴 수 있습니다.
     * - 클라이언트 앱은 이 토큰을 저장하고 모든 마이크로서비스 요청에 포함시킵니다.
     * 
     * @param loginRequest 로그인 요청 정보 (이메일, 비밀번호)
     * @return ResponseEntity<JwtResponse> 발급된 토큰 정보를 포함한 응답
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // 사용자 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User userDetails = (User) authentication.getPrincipal();

        // JWT 토큰 생성
        String jwt = jwtUtils.generateJwtToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        // 사용자 역할 정보 추출
        // 이 역할 정보는 마이크로서비스에서의 권한 체크에 사용됩니다
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // 응답 생성
        // 이 응답에는 다른 마이크로서비스에서 사용될 토큰과 사용자 정보가 포함됩니다
        return ResponseEntity.ok(JwtResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken)
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .roles(roles)
                .build());
    }

    /**
     * 사용자 등록을 처리합니다.
     * 
     * MSA에서의 의미:
     * - 중앙 집중식 사용자 관리: 모든 마이크로서비스에서 사용하는 사용자 계정을 한 곳에서 관리합니다.
     * - 사용자 등록 후, 해당 사용자는 모든 마이크로서비스에 접근할 수 있습니다(권한에 따라).
     * - 분산 시스템에서 사용자 관리의 일관성을 유지합니다.
     * - 신규 마이크로서비스가 추가되어도 별도의 사용자 등록 과정이 필요 없습니다.
     * 
     * @param registrationDto 사용자 등록 정보 (이메일, 비밀번호, 이름)
     * @return ResponseEntity<String> 사용자 등록 성공 메시지를 포함한 응답
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        // 사용자 등록
        userService.registerUser(registrationDto);
        return ResponseEntity.ok("사용자가 성공적으로 등록되었습니다!");
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.
     * 
     * MSA에서의 의미:
     * - 토큰 기반 세션 관리: 상태 없는(stateless) 마이크로서비스 환경에서 사용자 세션을 유지합니다.
     * - 보안 강화: 액세스 토큰의 수명을 짧게 유지하면서도 사용자 경험을 저하시키지 않습니다.
     * - 단일 갱신 지점: 모든 마이크로서비스에서 사용하는 토큰을 한 곳에서 갱신합니다.
     * - 토큰 폐기 관리: 중앙에서 리프레시 토큰의 유효성을 검사하고 필요시 폐기할 수 있습니다.
     * 
     * @param request 리프레시 토큰 요청 정보
     * @return ResponseEntity<JwtResponse> 새로 발급된 토큰 정보를 포함한 응답
     */
    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        // 리프레시 토큰 유효성 검증
        if (jwtUtils.validateJwtToken(requestRefreshToken)) {
            String username = jwtUtils.getUserNameFromJwtToken(requestRefreshToken);
            User userDetails = (User) userService.loadUserByUsername(username);

            // 새로운 액세스 토큰 생성
            // 이 토큰은 모든 마이크로서비스에서 사용됩니다
            String newAccessToken = jwtUtils.generateJwtToken(userDetails);

            return ResponseEntity.ok(JwtResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(requestRefreshToken)
                    .id(userDetails.getId())
                    .username(userDetails.getUsername())
                    .email(userDetails.getEmail())
                    .roles(userDetails.getAuthorities().stream()
                            .map(item -> item.getAuthority())
                            .collect(Collectors.toList()))
                    .build());
        }

        return ResponseEntity.badRequest().body("리프레시 토큰이 만료되었습니다!");
    }
}