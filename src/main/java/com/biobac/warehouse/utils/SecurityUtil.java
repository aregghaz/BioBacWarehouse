package com.biobac.warehouse.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

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

}
