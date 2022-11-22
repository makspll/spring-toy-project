package com.tasks.tasks.controller;

import java.util.Base64;
import java.util.stream.Stream;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.tasks.tasks.dao.account.AccountRepository;
import com.tasks.tasks.entities.Account;
import com.tasks.tasks.service.Permissions;
import com.tasks.tasks.service.data.AuthRequest;
import com.tasks.tasks.service.data.AuthResponse;
import com.tasks.tasks.util.JwtTokenUtil;

@RestController
@RequestMapping("/auth")
public class Auth {

    @Autowired
    AuthenticationManager authManager;
    @Autowired
    JwtTokenUtil jwtUtil;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AccountRepository accountRepository;

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username, request.password));

            Account user = (Account) authentication.getPrincipal();
            String accessToken = jwtUtil.generateAccessToken(user);
            AuthResponse response = new AuthResponse(accessToken);

            return ResponseEntity.ok().body(response);

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        // first of all check user does not exists yet
        if (accountRepository.findByUsername(request.username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // if not we're in the clear to generate one
        Account newAccount = Account.builder()
                .permissions(Permissions.toRepresentation(Stream.of(
                        Permissions.DELETE_TASK, Permissions.READ_TASK,
                        Permissions.UPDATE_TASK, Permissions.WRITE_TASK)))
                .password(passwordEncoder.encode(request.password))
                .username(request.username)
                .build();

        String accessToken = jwtUtil.generateAccessToken(newAccount);

        accountRepository.save(newAccount);

        return ResponseEntity.ok().body(new AuthResponse(accessToken));
    }
}
