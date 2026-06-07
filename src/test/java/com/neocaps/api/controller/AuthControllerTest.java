package com.neocaps.api.controller;

import com.neocaps.api.enums.UserRole;
import com.neocaps.api.model.dto.LoginRequest;
import com.neocaps.api.model.dto.RegisterRequest;
import com.neocaps.api.model.dto.UserResponse;
import com.neocaps.api.model.entity.AuditLog;
import com.neocaps.api.repository.AuditLogRepository;
import com.neocaps.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthControllerTest {

    @Autowired
    private AuthController authController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        auditLogRepository.deleteAll();
    }

    @Test
    void testRegisterUserAndAudit() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newoperator");
        request.setPassword("password123");
        request.setRole(UserRole.OPERATOR);

        ResponseEntity<UserResponse> response = authController.register(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("newoperator", response.getBody().getUsername());
        assertEquals(UserRole.OPERATOR, response.getBody().getRole());

        // Verify user is in database
        assertTrue(userRepository.findByUsername("newoperator").isPresent());

        // Verify audit log exists
        List<AuditLog> logs = auditLogRepository.findAllByOrderByTimestampDesc();
        assertFalse(logs.isEmpty());
        AuditLog registerLog = logs.stream()
                .filter(l -> l.getAction().equals("USER_REGISTER"))
                .findFirst()
                .orElse(null);
        assertNotNull(registerLog);
        assertTrue(registerLog.getDescription().contains("newoperator"));
    }

    @Test
    void testLoginUserAndAudit() {
        // Register first
        RegisterRequest register = new RegisterRequest();
        register.setUsername("logintest");
        register.setPassword("pass123");
        register.setRole(UserRole.SUPERVISOR);
        authController.register(register);

        auditLogRepository.deleteAll();

        // Perform login
        LoginRequest login = new LoginRequest();
        login.setUsername("logintest");
        login.setPassword("pass123");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        ResponseEntity<UserResponse> loginResponse = authController.login(login, request, response);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
        assertEquals("logintest", loginResponse.getBody().getUsername());
        
        // Verify login audit log
        List<AuditLog> logs = auditLogRepository.findAllByOrderByTimestampDesc();
        assertFalse(logs.isEmpty());
        AuditLog loginLog = logs.stream()
                .filter(l -> l.getAction().equals("USER_LOGIN"))
                .findFirst()
                .orElse(null);
        assertNotNull(loginLog);
        assertEquals("logintest", loginLog.getUsername());
    }

    @Test
    void testLoginInvalidCredentials() {
        // Perform login for non-existent user
        LoginRequest login = new LoginRequest();
        login.setUsername("nonexistent");
        login.setPassword("wrong");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThrows(BadCredentialsException.class, () -> {
            authController.login(login, request, response);
        });
    }

    @Test
    void testLogoutAndAudit() {
        // Mock authentication context
        Authentication auth = new UsernamePasswordAuthenticationToken("logouttest", "pass", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        ResponseEntity<Void> logoutResponse = authController.logout(request, response, auth);
        assertEquals(HttpStatus.NO_CONTENT, logoutResponse.getStatusCode());

        // Verify logout audit log
        List<AuditLog> logs = auditLogRepository.findAllByOrderByTimestampDesc();
        assertFalse(logs.isEmpty());
        AuditLog logoutLog = logs.stream()
                .filter(l -> l.getAction().equals("USER_LOGOUT"))
                .findFirst()
                .orElse(null);
        assertNotNull(logoutLog);
        assertEquals("logouttest", logoutLog.getUsername());
        
        // Verify security context cleared
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
