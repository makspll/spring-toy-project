package com.tasks.tasks.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.tasks.tasks.dao.account.AccountRepository;
import com.tasks.tasks.dao.task.TaskRepository;
import com.tasks.tasks.entities.Account;
import com.tasks.tasks.entities.Task;
import com.tasks.tasks.entities.WorkerStatus;
import com.tasks.tasks.service.Permissions;

import static com.tasks.tasks.controller.Utils.makePrincipal;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
public class AuthTest {

    private JdbcTemplate jdbcTemplateAccount;
    private JdbcTemplate jdbcTemplateTask;

    @Autowired
    @Qualifier("accountDataSource")
    private DataSource accountDataSource;

    @Autowired
    @Qualifier("taskDataSource")
    private DataSource taskDataSource;

    @Autowired
    private WebTestClient client;

    @PostConstruct
    public void initJdbcTemplate() {
        jdbcTemplateAccount = new JdbcTemplate(accountDataSource);
        jdbcTemplateTask = new JdbcTemplate(taskDataSource);
    }

    @AfterEach
    public void delete() {
        JdbcTestUtils.deleteFromTables(jdbcTemplateAccount, "account");
        JdbcTestUtils.deleteFromTables(jdbcTemplateTask, "task");
    }

    @Test
    public void testRegisterNewUserThenLogin() {
        Utils.registerUserAssertSuccess(client, "user", "password");
        Utils.loginUserAssertSuccess(client, "user", "password");
    }

    @Test
    public void testRegisterNewUserExistingFails() {
        Utils.registerUserAssertSuccess(client, "user", "password");
        assertFalse(Utils.registerUser(client, "user", "password").isPresent());
    }

    @Test
    public void testLoginWrongPasswordFails() {
        Utils.registerUserAssertSuccess(client, "user", "password");
        assertFalse(Utils.loginUser(client, "user", "passwor").isPresent());
    }

    @Test
    public void testLoginWrongUsernameFails() {
        Utils.registerUserAssertSuccess(client, "user", "password");
        assertFalse(Utils.loginUser(client, "use", "password").isPresent());
    }

    @Test
    public void testGetAllTasks() {
        String token = Utils.registerUserAssertSuccess(client, "user", "password");
        client.get()
                .uri("/tasks")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void testGetAllTasksWithoutReadRole() {
        String token = Utils.registerUserAssertSuccess(client, "user", "password");
        jdbcTemplateAccount.execute("UPDATE account SET permissions=0");

        client.get()
                .uri("/tasks")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    public void testGetTaskByIdWithoutReadRole() {
        String token = Utils.registerUserAssertSuccess(client, "user", "password");
        jdbcTemplateAccount.execute("UPDATE account SET permissions=0");

        client.get()
                .uri("/tasks/0")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    public void testGetTaskById() {
        String token = Utils.registerUserAssertSuccess(client, "user", "password");

        client.get()
                .uri("/tasks/0")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void testPutTaskByIdWithoutWriteRole() {
        String token = Utils.registerUserAssertSuccess(client, "user", "password");
        jdbcTemplateAccount.execute("UPDATE account SET permissions=0");

        client.put()
                .uri("/tasks/{id}", 0)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue(Task.builder().script("").executionTime(new Timestamp(12312312L)).build())
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void testPutTaskById() {
        String token = Utils.registerUserAssertSuccess(client, "user", "password");

        client.put()
                .uri("/tasks/{id}", 0)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue(Task.builder().script("").executionTime(new Timestamp(12312312L)).build())
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void testDeleteTaskByIdWithoutWriteRole() {
        String token = Utils.registerUserAssertSuccess(client, "user", "password");
        jdbcTemplateAccount.execute("UPDATE account SET permissions=0");

        client.delete()
                .uri("/tasks/{id}", 0)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void testDeleteTaskById() {
        String token = Utils.registerUserAssertSuccess(client, "user", "password");

        client.delete()
                .uri("/tasks/{id}", 0)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

}
