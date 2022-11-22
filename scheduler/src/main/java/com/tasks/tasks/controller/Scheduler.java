package com.tasks.tasks.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.tasks.tasks.dao.account.AccountRepository;
import com.tasks.tasks.dao.task.TaskRepository;
import com.tasks.tasks.entities.Account;
import com.tasks.tasks.entities.Task;
import com.tasks.tasks.entities.WorkerStatus;
import com.tasks.tasks.service.Permissions;

@RestController
@ConditionalOnProperty(value = "application.mode", havingValue = "scheduler")
@RequestMapping("/")
public class Scheduler {

        @Autowired
        private TaskRepository taskRepository;

        @GetMapping("/permissions")
        @ResponseBody
        public ResponseEntity<List<Permissions>> getRoleInfo(
                        @AuthenticationPrincipal Account principal) {
                return ResponseEntity.ok(Permissions.fromRepresentation(principal.getPermissions()));
        }

        @GetMapping("/tasks")
        @PreAuthorize("hasAuthority('OP_READ_TASK')")
        @ResponseBody
        public ResponseEntity<List<Task>> getAllTasks(
                        @AuthenticationPrincipal Account principal) {
                List<Task> list = new ArrayList<Task>();
                taskRepository.findAllByAccountId(principal.getId()).forEach(list::add);
                return new ResponseEntity<>(list, HttpStatus.OK);
        }

        @GetMapping("/tasks/{id}")
        @PreAuthorize("hasAuthority('OP_READ_TASK')")
        @ResponseBody
        public ResponseEntity<Task> getTasksById(
                        @AuthenticationPrincipal Account principal,
                        @PathVariable("id") long id) {
                return taskRepository.findById(id)
                                .filter((task) -> task.getAccountId() == (principal).getId())
                                .map((task) -> new ResponseEntity<>(task, HttpStatus.OK))
                                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }

        @PostMapping("/tasks")
        @PreAuthorize("hasAuthority('OP_WRITE_TASK')")
        public ResponseEntity<Task> postTask(
                        @AuthenticationPrincipal Account principal,
                        @Valid @RequestBody Task task) {

                // otherwise accountId will be null
                task.setAccountId(principal.getId());
                task.setWorkerStatus(WorkerStatus.NOT_SCHEDULED);

                task = taskRepository.save(task);
                return new ResponseEntity<>(task, HttpStatus.CREATED);
        }

        @PutMapping("/tasks/{id}")
        @PreAuthorize("hasAuthority('OP_UPDATE_TASK')")
        public ResponseEntity<Task> putTask(
                        @AuthenticationPrincipal Account principal,
                        @Valid @RequestBody Task task,
                        @PathVariable("id") long id) {

                // otherwise accountId will be null
                task.setAccountId(principal.getId());
                task.setId(id);

                return taskRepository.findById(id)
                                .filter((t) -> t.getAccountId() == id)
                                .map((t) -> {
                                        // don't allow all fields to be changed
                                        t.setExecutionTime(task.getExecutionTime());
                                        t.setScript(task.getScript());
                                        return ResponseEntity.ok().body(taskRepository.save(t));
                                })
                                .orElseGet(() -> ResponseEntity.notFound().build());
        }

        @DeleteMapping("/tasks/{id}")
        @PreAuthorize("hasAuthority('OP_DELETE_TASK')")
        public ResponseEntity<Task> deleteTask(
                        @AuthenticationPrincipal Account principal,
                        @PathVariable("id") long id) {

                taskRepository.findAllByAccountId(principal.getId())
                                .stream()
                                .filter((t) -> t.getId() == id)
                                .findFirst()
                                .ifPresent((t) -> taskRepository.deleteById(id));

                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        /// Handle validation errors
        @ExceptionHandler({ BindException.class,
                        MethodArgumentNotValidException.class })

        public ResponseEntity<Map<String, Object>> handleException(BindException e) {

                List<String> errors = new ArrayList<>();
                e.getFieldErrors()
                                .forEach(err -> errors.add(err.getField() + ": " + err.getDefaultMessage()));
                e.getGlobalErrors()
                                .forEach(err -> errors.add(err.getObjectName() + ": " + err.getDefaultMessage()));

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", errors);

                errorResponse.put("status", HttpStatus.BAD_REQUEST.toString());
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
}
