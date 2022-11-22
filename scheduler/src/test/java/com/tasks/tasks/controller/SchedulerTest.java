package com.tasks.tasks.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.tasks.tasks.dao.task.TaskRepository;
import com.tasks.tasks.entities.Account;
import com.tasks.tasks.entities.Task;
import com.tasks.tasks.entities.WorkerStatus;
import com.tasks.tasks.service.Permissions;
import static com.tasks.tasks.controller.Utils.makePrincipal;

@ExtendWith(MockitoExtension.class)
public class SchedulerTest {

    @InjectMocks
    Scheduler scheduler;

    @Mock
    TaskRepository taskRepository;

    @Test
    public void testGetAllTasks() {
        when(taskRepository.findAllByAccountId(0l)).thenReturn(Arrays.asList(new Task(), new Task()));
        Account principal = makePrincipal(0l, "user", Stream.empty());

        ResponseEntity<List<Task>> output = scheduler.getAllTasks(principal);

        assertEquals(HttpStatus.OK, output.getStatusCode());
        assertEquals(2, output.getBody().size(), "wrong number of tasks");
    }

    @Test
    public void testGetAllTasksEmpty() {
        when(taskRepository.findAllByAccountId(0l)).thenReturn(Arrays.asList());
        Account principal = makePrincipal(0l, "user", Stream.empty());

        ResponseEntity<List<Task>> output = scheduler.getAllTasks(principal);

        assertEquals(HttpStatus.OK, output.getStatusCode());
        assertEquals(0, output.getBody().size(), "wrong number of tasks");
    }

    @Test
    public void testGetTaskById() {
        when(taskRepository.findById(1l)).thenReturn(
            Optional.of(Task.builder()
                .id(1l)
                .accountId(0l)
                .build()));
        Account principal = makePrincipal(0l, "user", Stream.empty());

        ResponseEntity<Task> output = scheduler.getTasksById(principal, 1l);
        assertEquals(HttpStatus.OK,output.getStatusCode());
        assertEquals(1l, output.getBody().getId(), "wrong id");
    }

    @Test
    public void testGetTaskByIdNotFound() {
        when(taskRepository.findById(1l)).thenReturn(Optional.empty());
        Account principal = makePrincipal(0l, "user", Stream.empty());

        ResponseEntity<Task> output = scheduler.getTasksById(principal, 1l);
        assertEquals(HttpStatus.NOT_FOUND, output.getStatusCode());
        assertNull(output.getBody());
    }

    @Test
    public void testPostTask() {
        Task validTask = new Task(null, null, "", Timestamp.valueOf("1111-11-11 11:11:11"), WorkerStatus.SCHEDULED,
                null);
        when(taskRepository.save(validTask)).thenReturn(validTask);
        Account principal = makePrincipal(0l, "user", Stream.empty());

        ResponseEntity<Task> output = scheduler.postTask(principal, validTask);
        assertEquals(HttpStatus.CREATED, output.getStatusCode());
        assertEquals(validTask, output.getBody());
    }

    @Test
    public void testPutTask() {
        Task validTask = new Task(0L, 0L, "", Timestamp.valueOf("1111-11-11 11:11:11"), WorkerStatus.SCHEDULED, null);
        Task newTask = new Task(0L, 0L, "a", Timestamp.valueOf("1112-11-11 11:11:11"), WorkerStatus.SCHEDULED, null);

        when(taskRepository.findById(0l)).thenReturn(Optional.of(validTask));
        // save is called on initial object but its fields are new
        // so save returns updated content
        when(taskRepository.save(validTask)).thenReturn(newTask);
        Account principal = makePrincipal(0l, "user", Stream.empty());

        ResponseEntity<Task> output = scheduler.putTask(principal, newTask, 0L);
        assertEquals(HttpStatus.OK, output.getStatusCode());
        assertEquals(newTask, output.getBody());
    }

    @Test
    public void testPutTaskNonExisting() {
        Task newTask = new Task(0L, 0L, "a", Timestamp.valueOf("1112-11-11 11:11:11"), WorkerStatus.SCHEDULED, null);

        when(taskRepository.findById(0l)).thenReturn(Optional.empty());
        Account principal = makePrincipal(0l, "user", Stream.empty());

        ResponseEntity<Task> output = scheduler.putTask(principal, newTask, 0L);
        assertEquals(HttpStatus.NOT_FOUND, output.getStatusCode());
        assertEquals(null, output.getBody());
    }

    @Test
    public void testDeleteTask() {
        Task validTask = new Task(0L, 0L, "", Timestamp.valueOf("1111-11-11 11:11:11"), WorkerStatus.SCHEDULED, null);
        when(taskRepository.findAllByAccountId(0L)).thenReturn(Arrays.asList(validTask));
        Account principal = makePrincipal(0l, "user", Stream.empty());

        ResponseEntity<Task> output = scheduler.deleteTask(principal, 0);
        assertEquals(HttpStatus.NO_CONTENT, output.getStatusCode());
        assertEquals(null, output.getBody());
    }

    @Test
    public void testDeleteTaskNonExisting() {
        when(taskRepository.findAllByAccountId(0L)).thenReturn(Arrays.asList());
        Account principal = makePrincipal(0l, "user", Stream.empty());

        ResponseEntity<Task> output = scheduler.deleteTask(principal, 0);
        assertEquals(HttpStatus.NO_CONTENT, output.getStatusCode());
        assertEquals(null, output.getBody());
    }
}
