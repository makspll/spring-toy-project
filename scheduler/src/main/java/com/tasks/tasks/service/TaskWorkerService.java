package com.tasks.tasks.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tasks.tasks.dao.task.TaskRepository;
import com.tasks.tasks.entities.Task;
import com.tasks.tasks.entities.WorkerStatus;

@Service
public class TaskWorkerService {

    @Value("${application.worker.delay.no_new_task}")
    /// poll delay if there are no new tasks available (seconds)
    int no_task_delay;

    static Task scheduledTask;

    @Autowired
    TaskRepository taskRepository;

    Logger logger = LoggerFactory.getLogger(TaskWorkerService.class);

    /// Inspects tasks to figure out next worker execution point, if null will
    /// schedule at arbitrary date to check for new tasks. Remembers next task as
    /// the scheduled task and also sets it status as scheduled so other workers
    /// don't pick it up
    public Date setNextScheduledTask() {
        logger.info("Polling next task");
        return taskRepository
                .findFirstByWorkerStatusOrderByExecutionTimeDesc(WorkerStatus.NOT_SCHEDULED)
                .map((task) -> {
                    // keep the task as next and change status
                    scheduledTask = task;
                    // TODO: status might have changed here
                    scheduledTask.setWorkerStatus(WorkerStatus.SCHEDULED);
                    taskRepository.save(scheduledTask);

                    // if task is before now, schedule for now
                    Timestamp executionTime;
                    if (task.getExecutionTime().before(Timestamp.from(Instant.now()))) {
                        executionTime = new Timestamp(System.currentTimeMillis() + 1000);
                    } else {
                        executionTime = new Timestamp(System.currentTimeMillis() + (1000 * no_task_delay));
                    }

                    logger.info("Found task, scheduling for: " + executionTime.toString());

                    return executionTime;
                })
                .orElse(new Timestamp(System.currentTimeMillis() + (1000 * no_task_delay)));

    }

    /// If nextExecutionDate was called beforehand will execute the task
    public void executeScheduledTask() {
        // no task available
        if (scheduledTask == null) {
            logger.info("No task available, waiting...");
            return;
        }

        try {
            logger.info("Scheduling task with ID: " + scheduledTask.getId().toString());
            // create new tempfile for the script
            File scriptFile = File.createTempFile("wrk_tasks", ".py");

            // write script into the file
            FileWriter writer = new FileWriter(scriptFile);
            writer.write(scheduledTask.getScript());
            writer.flush();
            writer.close();

            // execute the script
            ProcessBuilder processBuilder = new ProcessBuilder("python3", scriptFile.getAbsolutePath());
            processBuilder.redirectErrorStream(true);

            // update task
            Process process = processBuilder.start();

            // read status, block on the process (yes infinite processes will deadlock the
            // worker)
            int status = process.waitFor();

            // read stdout
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            logger.info("Script standard output:");
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                logger.info(s);
            }

            // read stdio
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            logger.info("Script standard error:");
            s = null;
            while ((s = stdInput.readLine()) != null) {
                logger.info(s);
            }

            scheduledTask.setStatusCode(status);
            scheduledTask.setWorkerStatus(WorkerStatus.EXECUTED);
            taskRepository.save(scheduledTask);

        } catch (Exception e) {
            // update task
            scheduledTask.setStatusCode(1);
            scheduledTask.setWorkerStatus(WorkerStatus.FAILED);
            taskRepository.save(scheduledTask);

            // bubble up error
            logger.error("An error occurred while executing task.");
            e.printStackTrace();
        }
        scheduledTask = null;
    }
}
