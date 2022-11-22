package com.tasks.tasks.entities;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import com.tasks.tasks.validationGroups.Strict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(groups = { Strict.class })
    private Long accountId;

    @NotNull()
    private String script;

    @NotNull()
    private Timestamp executionTime;

    @Enumerated(EnumType.STRING)
    @NotNull(groups = { Strict.class })
    private WorkerStatus workerStatus;

    private Integer statusCode;
}
