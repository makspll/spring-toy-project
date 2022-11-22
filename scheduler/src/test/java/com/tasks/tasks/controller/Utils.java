package com.tasks.tasks.controller;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

import com.tasks.tasks.entities.Account;
import com.tasks.tasks.service.Permissions;
import com.tasks.tasks.service.data.AuthRequest;
import com.tasks.tasks.service.data.AuthResponse;

import reactor.core.publisher.Flux;

public class Utils {
    /// Makes a principal with the given data
    public static Account makePrincipal(Long id, String username, Stream<Permissions> permissions) {
        return Account.builder()
                .id(id)
                .username(username)
                .permissions(Permissions.toRepresentation(permissions))
                .build();
    }

    /*
     * Calls the register endpoints with the given credentials and returns token if
     */
    public static String registerUserAssertSuccess(WebTestClient client,
            String username,
            String password) {
        return registerUserAssertable(true, client, username, password).get();
    }

    /*
     * Calls the register endpoints with the given credentials and returns token if
     */
    public static Optional<String> registerUser(WebTestClient client,
            String username,
            String password) {
        return registerUserAssertable(false, client, username, password);
    }

    private static Optional<String> registerUserAssertable(
            boolean assert_success,
            WebTestClient client,
            String username,
            String password) {

        ResponseSpec response = client.post()
                .uri("/auth/register")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new AuthRequest(username, password))
                .exchange();

        if (assert_success) {
            response = response.expectStatus().isOk();
        }

        Flux<AuthResponse> token = response
                .returnResult(AuthResponse.class)
                .getResponseBody();

        return Optional.ofNullable(token.blockFirst()).map((t) -> {
            System.out.println(t);
            return t.token;
        });
    }

    /*
     * Calls the login endpoints with the given credentials and returns token if
     */
    public static String loginUserAssertSuccess(WebTestClient client,
            String username,
            String password) {
        return loginUserAssertable(true, client, username, password).get();
    }

    /*
     * Calls the login endpoints with the given credentials and returns token if
     */
    public static Optional<String> loginUser(WebTestClient client,
            String username,
            String password) {
        return loginUserAssertable(false, client, username, password);
    }

    /*
     * Calls the login endpoints with the given credentials and returns token if
     * succesfull
     */
    public static Optional<String> loginUserAssertable(boolean assert_success, WebTestClient client, String username,
            String password) {

        ResponseSpec response = client.post()
                .uri("/auth/login")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new AuthRequest(username, password))
                .exchange();

        if (assert_success) {
            response = response.expectStatus().isOk();
        }

        Flux<AuthResponse> token = response
                .returnResult(AuthResponse.class)
                .getResponseBody();

        return Optional.ofNullable(token.blockFirst()).map((t) -> {
            System.out.println(t);
            return t.token;
        });
    }
}
