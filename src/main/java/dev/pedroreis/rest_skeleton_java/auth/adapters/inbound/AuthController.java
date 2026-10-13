package dev.pedroreis.rest_skeleton_java.auth.adapters.inbound;

import dev.pedroreis.rest_skeleton_java.auth.adapters.inbound.dto.LoginRequest;
import dev.pedroreis.rest_skeleton_java.auth.adapters.inbound.dto.LoginResponse;
import dev.pedroreis.rest_skeleton_java.auth.adapters.inbound.dto.RefreshRequest;
import dev.pedroreis.rest_skeleton_java.auth.adapters.inbound.dto.RefreshResponse;
import dev.pedroreis.rest_skeleton_java.auth.ports.inbound.LoginUseCase;
import dev.pedroreis.rest_skeleton_java.auth.ports.inbound.RefreshTokenUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/public/auth")
@RequiredArgsConstructor
public class AuthController {
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(LoginResponse.fromDomain(
                loginUseCase.login(request.email(), request.password())));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(new RefreshResponse(
                refreshTokenUseCase.refresh(request.refreshToken())));
    }
}
