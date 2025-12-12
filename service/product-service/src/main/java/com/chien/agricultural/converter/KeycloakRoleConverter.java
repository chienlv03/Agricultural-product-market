package com.chien.agricultural.converter;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt source) {
        Map<String, Object> realmAccess = (Map<String, Object>) source.getClaims().get("realm_access");

        if (realmAccess == null || realmAccess.isEmpty()) {
            return new ArrayList<>();
        }

        // Lấy danh sách roles từ JSON
        return ((List<String>) realmAccess.get("roles"))
                .stream()
                // Vì trên Keycloak đã là ROLE_SELLER rồi, nên ta map thẳng luôn
                // Không cần cộng chuỗi "ROLE_" nữa
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}