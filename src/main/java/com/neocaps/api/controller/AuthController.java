package com.neocaps.api.controller;

import com.neocaps.api.model.dto.LoginRequest;
import com.neocaps.api.model.dto.RegisterRequest;
import com.neocaps.api.model.dto.UserCreateRequest;
import com.neocaps.api.model.dto.UserResponse;
import com.neocaps.api.service.AuditLogService;
import com.neocaps.api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final AuditLogService auditLogService;

    // Delegate repository to persist programmatically created security context
    private final SecurityContextRepository securityContextRepository = new DelegatingSecurityContextRepository(
            new RequestAttributeSecurityContextRepository(),
            new HttpSessionSecurityContextRepository()
    );

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request, 
                                              HttpServletRequest httpRequest, 
                                              HttpServletResponse httpResponse) {
        log.info("Authentication request received for user: {}", request.getUsername());
        
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                request.getUsername(), request.getPassword()
        );

        Authentication authentication = authenticationManager.authenticate(token);

        // Save authentication token to context
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        
        // Persist SecurityContext across HTTP requests (sessions)
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        // Audit log success
        auditLogService.log("USER_LOGIN", "User logged in successfully");

        UserResponse userResponse = userService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for user: {} with role: {}", request.getUsername(), request.getRole());
        
        UserCreateRequest createRequest = new UserCreateRequest();
        createRequest.setUsername(request.getUsername());
        createRequest.setPassword(request.getPassword());
        createRequest.setRole(request.getRole());

        UserResponse response = userService.createUser(createRequest);

        // Audit log success
        auditLogService.log("USER_REGISTER", "Registered new user: " + request.getUsername() + " with role: " + request.getRole());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest, 
                                       HttpServletResponse httpResponse, 
                                       Authentication authentication) {
        String username = "SYSTEM";
        if (authentication != null && authentication.isAuthenticated()) {
            username = authentication.getName();
        }

        log.info("Logout request received for user: {}", username);

        // Audit log success before cleaning authentication state
        auditLogService.log("USER_LOGOUT", "User logged out successfully");

        // Invalidate HttpSession
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Clear Security Context
        SecurityContextHolder.clearContext();

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> profile(HttpServletRequest httpRequest,
                                                HttpServletResponse httpResponse,
                                                Authentication authentication) {
        String username = null;
        if (authentication != null && authentication.isAuthenticated()) {
            username = authentication.getName();
        }

        UserResponse userResponse = userService.getUserByUsername(username);
        return ResponseEntity.ok(userResponse);
    }
}
