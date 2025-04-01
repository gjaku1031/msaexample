package com.example.authserver.controller;

import com.example.authserver.dto.UserRegistrationDto;
import com.example.authserver.entity.User;
import com.example.authserver.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * 사용자 관리 컨트롤러
 * 사용자 등록 및 관리와 관련된 HTTP 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 새로운 사용자를 등록합니다.
     * 
     * @param registrationDto 사용자 등록 정보
     * @return ResponseEntity<Void> 생성된 사용자의 URI를 포함한 201 Created 응답
     */
    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        User user = userService.registerUser(registrationDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    /**
     * 사용자 계정을 활성화/비활성화합니다.
     * 관리자 권한이 필요합니다.
     * 
     * @param email   대상 사용자의 이메일
     * @param enabled 활성화 여부
     * @return ResponseEntity<Void> 200 OK 응답
     */
    @PutMapping("/{email}/enable")
    public ResponseEntity<Void> setUserEnabled(
            @PathVariable String email,
            @RequestParam boolean enabled) {
        userService.setUserEnabled(email, enabled);
        return ResponseEntity.ok().build();
    }

    /**
     * 사용자 계정을 잠금/잠금해제합니다.
     * 관리자 권한이 필요합니다.
     * 
     * @param email  대상 사용자의 이메일
     * @param locked 잠금 여부
     * @return ResponseEntity<Void> 200 OK 응답
     */
    @PutMapping("/{email}/lock")
    public ResponseEntity<Void> setAccountLocked(
            @PathVariable String email,
            @RequestParam boolean locked) {
        userService.setAccountLocked(email, locked);
        return ResponseEntity.ok().build();
    }

    /**
     * 사용자 정보를 조회합니다.
     * 
     * @param email 조회할 사용자의 이메일
     * @return ResponseEntity<User> 조회된 사용자 정보
     */
    @GetMapping("/{email}")
    public ResponseEntity<User> getUser(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }
}