package com.campus.activity.auth;

import com.campus.activity.common.AuthContext;
import com.campus.activity.common.AuthService;
import com.campus.activity.common.CurrentUser;
import com.campus.activity.common.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        CurrentUser user = authService.authenticate(request.username(), request.password());
        return Result.success(new LoginResponse(authService.issueToken(user), user));
    }

    @GetMapping("/me")
    public Result<CurrentUser> me() {
        return Result.success(AuthContext.get());
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record LoginResponse(String token, CurrentUser user) {
    }
}
