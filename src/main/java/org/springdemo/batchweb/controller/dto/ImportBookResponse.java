package org.springdemo.batchweb.controller.dto;

import java.time.LocalDateTime;

public record ImportBookResponse(
        long jobId,
        long executionId,
        LocalDateTime createTime,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status,
        String exitStatus
        ) {
}
