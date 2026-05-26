package com.r2p.neocaps.aop;

import com.r2p.neocaps.entity.AuditLog;
import com.r2p.neocaps.entity.User;
import com.r2p.neocaps.repository.UserRepository;
import com.r2p.neocaps.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Around("@annotation(auditable)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object proceed = joinPoint.proceed();

        String username = "System";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            username = authentication.getName();
        }

        String ipAddress = "Unknown";
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            ipAddress = request.getRemoteAddr();
        }

        User user = null;
        if (!"System".equals(username)) {
            user = userRepository.findByUsername(username).orElse(null);
        }

        AuditLog log = AuditLog.builder()
                .user(user)
                .action(auditable.action())
                .description(auditable.description())
                .timestamp(LocalDateTime.now())
                .ipAddress(ipAddress)
                .build();

        auditLogRepository.save(log);

        return proceed;
    }
}
