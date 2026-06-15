package dev.pedroreis.rest_skeleton_java.user.adapters.inbound;

import dev.pedroreis.rest_skeleton_java.user.adapters.inbound.dto.UserRequest;
import dev.pedroreis.rest_skeleton_java.user.adapters.inbound.dto.UserResponse;
import dev.pedroreis.rest_skeleton_java.user.domain.User;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.CreateUserUseCase;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.DeleteUserUseCase;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.FindUserUseCase;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.UpdateUserUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final CreateUserUseCase createUserUseCase;
    private final FindUserUseCase findUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody UserRequest request
    ) {
        User createUser = createUserUseCase.execute(
                request.name(),
                request.email(),
                request.password()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponse.fromDomain(createUser));
    }

}
