package com.tasks.tasks.dao.task;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasks.tasks.entities.Task;
import com.tasks.tasks.entities.WorkerStatus;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByAccountId(Long accountId);

    Optional<Task> findFirstByWorkerStatusOrderByExecutionTimeDesc(WorkerStatus status);
}
