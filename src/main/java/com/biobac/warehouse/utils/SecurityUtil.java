package com.biobac.warehouse.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SecurityUtil {

    public boolean hasPermission(List<String> permissions) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null || permissions == null || permissions.isEmpty()) {
            return false;
        }

        Set<String> userAuthorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return permissions.stream().anyMatch(userAuthorities::contains);
    }

    public boolean hasPermission(String permission) {
        if (permission == null || permission.isBlank()) return false;
        return hasPermission(Collections.singletonList(permission));
    }

    public boolean hasRole(List<String> roles) {
        return hasPermission(roles);
    }

    public boolean hasRole(String role) {
        return hasPermission(role);
    }

    public boolean hasAllPermissions(List<String> permissions) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null || permissions == null || permissions.isEmpty()) {
            return false;
        }

        Set<String> userAuthorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return userAuthorities.containsAll(permissions);
    }

    public boolean hasAllRoles(List<String> roles) {
        return hasAllPermissions(roles);
    }
}
