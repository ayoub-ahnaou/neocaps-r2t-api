package com.neocaps.api.service;

import com.neocaps.api.model.entity.AuditLog;
import com.neocaps.api.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Records an action performed in the system. Resolves the current user and IP address automatically if available.
     *
     * @param action      The high-level action name (e.g., "CREATE_LOT")
     * @param description A descriptive summary of what happened (e.g., "Created lot LOT001 with total activity 500 mCi")
     */
    public void log(String action, String description) {
        String username = "SYSTEM";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            username = auth.getName();
        }

        String ipAddress = "N/A";
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ipAddress = request.getRemoteAddr();
                // Check if behind reverse proxy
                String forwardedFor = request.getHeader("X-Forwarded-For");
                if (forwardedFor != null && !forwardedFor.isEmpty()) {
                    ipAddress = forwardedFor.split(",")[0].trim();
                }
            }
        } catch (Exception e) {
            log.warn("Could not determine client IP address: {}", e.getMessage());
        }

        AuditLog auditLog = AuditLog.builder()
                .username(username)
                .action(action)
                .description(description)
                .timestamp(LocalDateTime.now())
                .ipAddress(ipAddress)
                .build();

        auditLogRepository.save(auditLog);
        log.info("AUDIT - [{}] User: {}, Action: {}, IP: {}, Desc: {}", 
                auditLog.getTimestamp(), username, action, ipAddress, description);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}
