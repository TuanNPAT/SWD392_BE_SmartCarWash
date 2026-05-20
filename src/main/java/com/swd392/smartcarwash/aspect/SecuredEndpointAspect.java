package com.swd392.smartcarwash.aspect;

import com.swd392.smartcarwash.annotation.SecuredEndpoint;
import com.swd392.smartcarwash.exception.exceptions.ForbiddenException;
import com.swd392.smartcarwash.exception.exceptions.InvalidTokenException;
import com.swd392.smartcarwash.exception.exceptions.UnauthorizedException;
import com.swd392.smartcarwash.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Set;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class SecuredEndpointAspect {

    private final HttpServletRequest request;
    private final JwtUtil jwtUtil;

    @Around("@within(com.swd392.smartcarwash.annotation.SecuredEndpoint) || " +
            "@annotation(com.swd392.smartcarwash.annotation.SecuredEndpoint)")
    public Object checkSecuredEndpoint(ProceedingJoinPoint joinPoint) throws Throwable {

        SecuredEndpoint securedEndpoint = resolveAnnotation(joinPoint);

        if (securedEndpoint == null) {
            throw new ForbiddenException("Access denied");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Authentication required");
        }

        String token = extractTokenFromRequest();

        try {
            String requiredPermission = securedEndpoint.value();
            String username = jwtUtil.extractUsername(token);
            Set<String> userPermissions = jwtUtil.extractPermissions(token);

            if (!userPermissions.contains(requiredPermission)) {
                log.warn("Permission denied. User: {}, Required: {}, Available: {}",
                        username,
                        requiredPermission,
                        userPermissions
                );

                throw new ForbiddenException("Insufficient permissions");
            }

            return joinPoint.proceed();

        } catch (ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            throw new InvalidTokenException("Invalid or expired token");
        }
    }

    private SecuredEndpoint resolveAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        SecuredEndpoint annotation = method.getAnnotation(SecuredEndpoint.class);

        if (annotation == null) {
            annotation = method.getDeclaringClass().getAnnotation(SecuredEndpoint.class);
        }

        return annotation;
    }

    private String extractTokenFromRequest() {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Invalid authorization token format");
        }

        return authHeader.substring(7);
    }
}