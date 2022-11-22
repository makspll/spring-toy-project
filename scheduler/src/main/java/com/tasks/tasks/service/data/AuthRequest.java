package com.tasks.tasks.service.data;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AuthRequest {
    @NotNull
    @Length(min = 1, max = 256)
    public String username;

    @NotNull
    @Length(min = 1, max = 256)
    public String password;
}
