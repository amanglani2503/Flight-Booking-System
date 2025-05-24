package com.example.user_service.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {
    private final UserRegistration userRegistration;

    public UserPrincipal(UserRegistration userRegistration) {
        this.userRegistration = userRegistration;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userRegistration.getRole().name()));
    }

    @Override
    public String getPassword() {
        return userRegistration.getPassword();
    }

    @Override
    public String getUsername() {  // Here, we return email instead of username
        return userRegistration.getEmail();
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
