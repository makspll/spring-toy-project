package com.tasks.tasks.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.tasks.tasks.service.UnauthorisedHandler;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
                // securedEnabled = true,
                // jsr250Enabled = true,
                prePostEnabled = true)
public class SecurityConfig {

        @Autowired
        private UnauthorisedHandler unauthorizedHandler;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http.cors().and().csrf().disable()
                                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                                .authorizeRequests().antMatchers("/auth/**").permitAll()
                                .anyRequest().authenticated();
                http.addFilterBefore(authenticationJwtTokenFilter(),
                                UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
                        throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }

        @Bean
        public static PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthTokenFilter authenticationJwtTokenFilter() {
                return new AuthTokenFilter();
        }

}