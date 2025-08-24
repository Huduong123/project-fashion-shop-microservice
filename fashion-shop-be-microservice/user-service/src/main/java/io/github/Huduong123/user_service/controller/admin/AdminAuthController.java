package io.github.Huduong123.user_service.controller.admin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.Huduong123.user_service.dto.auth.AdminLoginDTO;
import io.github.Huduong123.user_service.dto.auth.AdminLoginResponseDTO;
import io.github.Huduong123.user_service.security.JwtUtil;
import io.github.Huduong123.user_service.service.auth.UserAuthService;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final UserAuthService userAuthService;

    private final JwtUtil jwtUtil;

    @Autowired
    public AdminAuthController(UserAuthService userAuthService, JwtUtil jwtUtil) {
        this.userAuthService = userAuthService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponseDTO> loginAdmin(@RequestBody @Valid AdminLoginDTO adminLoginDTO) {
        try {
            AdminLoginResponseDTO response = userAuthService.loginAdmin(adminLoginDTO);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw e; // Let GlobalExceptionHandler handle this
        }
    }

    @GetMapping("/verify-token")
    // <<< SỬA ĐỔI ĐỂ TRẢ VỀ CẢ ROLES
    public ResponseEntity<?> verifyToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            String token = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            } else if (authHeader != null) {
                token = authHeader;
            }

            if (token != null && jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                // Lấy roles từ token
                List<String> roles = jwtUtil.extractRoles(token);

                Map<String, Object> response = new HashMap<>();
                response.put("valid", true);
                response.put("username", username);
                response.put("roles", roles); // Thêm roles vào response
                response.put("message", "Token is valid");

                return ResponseEntity.ok(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Invalid or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Token verification failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

}
