package com.adnanumar.task_manager.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * JWT principal stored in the SecurityContext.
 * Implements UserDetails so @AuthenticationPrincipal UserDetails works in controllers.
 * getUsername() returns the user's email (used as the unique identifier throughout the app).
 */
public record JwtUserPrincipal(
        Long userId,
        String username,          // email
        List<GrantedAuthority> authorities
) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;   // not needed — JWT-based auth
    }

    @Override
    public String getUsername() {
        return username;   // returns email
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
