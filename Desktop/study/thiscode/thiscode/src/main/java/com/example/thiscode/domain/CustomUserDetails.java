package com.example.thiscode.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
//Spring Security 인증용 CustomUserDetails
public class CustomUserDetails implements UserDetails {
    private String email;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(String email, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    // Spring Security가 주요 식별자로 사용
    @Override
    public String getUsername() {
        return this.email;
    }

    public String getRealUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
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
