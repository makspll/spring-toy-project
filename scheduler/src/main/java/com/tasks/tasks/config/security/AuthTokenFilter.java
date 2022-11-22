package com.tasks.tasks.config.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tasks.tasks.dao.account.AccountRepository;
import com.tasks.tasks.entities.Account;
import com.tasks.tasks.service.CustomUserDetailsService;
import com.tasks.tasks.util.JwtTokenUtil;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // check bearer header exists
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // extract and validate token
            String token = authHeader.split(" ")[1].trim();
            if (jwtUtil.validateAccessToken(token)) {
                setAuthenticationContext(token, request);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthenticationContext(String token, HttpServletRequest request) {

        // extract principal from token
        String username = jwtUtil.getSubject(token);

        // fill out matching user
        UserDetails user = userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,
                user.getAuthorities());

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}