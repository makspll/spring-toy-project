package com.tasks.tasks.entities;

import java.sql.Timestamp;
import java.util.Base64;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.tasks.tasks.service.Permissions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Account implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank()
    @Column(length = 256)
    @Size(min = 0, max = 256)
    private String username;

    @NotEmpty()
    private String password;

    @NotNull()
    private Short permissions;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Permissions.fromRepresentation(Optional.ofNullable(permissions).orElse((short) 0));
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

    public String getPassword() {
        return password;
    }

}
