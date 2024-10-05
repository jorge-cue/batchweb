package org.springdemo.batchweb.controller;

import org.apache.logging.log4j.util.Strings;
import org.springdemo.batchweb.controller.dto.ImportBookResponse;
import org.springdemo.batchweb.exception.JobNotFoundException;
import org.springdemo.batchweb.job.JobParameterNames;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/books")
public class BookController {

    private final JobLauncher jobLauncher;

    private final JobExplorer jobExplorer;

    private final Job job;

    @Autowired
    public BookController(JobLauncher jobLauncher, JobExplorer jobExplorer, @Qualifier("importBooksJob") Job job) {
        this.jobLauncher = jobLauncher;
        this.jobExplorer = jobExplorer;
        this.job = job;
    }

    @SuppressWarnings("ReassignedVariable")
    @PostMapping(path = "/import")
    public ResponseEntity<ImportBookResponse> importBook(@RequestParam("file") MultipartFile file, @RequestHeader(value = "X-CORRELATION-ID") String correlationId) throws
            JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException,
            JobRestartException {
        final var jobParametersBuilder = new JobParametersBuilder();
        if (Strings.isEmpty(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }
        jobParametersBuilder.addString(JobParameterNames.CORRELATION_ID, correlationId, true);
        jobParametersBuilder.addJobParameter(JobParameterNames.FILE_RESOURCE, file.getResource(), Resource.class, false);
        final var jobExecution = jobLauncher.run(job, jobParametersBuilder.toJobParameters());
        final var importBookResponse = new ImportBookResponse(
                correlationId,
                jobExecution.getJobId(),
                jobExecution.getId(),
                jobExecution.getCreateTime(),
                jobExecution.getStartTime(),
                jobExecution.getEndTime(),
                jobExecution.getStatus().name(),
                jobExecution.getExitStatus().getExitDescription());
        final var location = UriComponentsBuilder.fromPath("/api/v1/books/import/execution/{executionId}").build(jobExecution.getId());
        return ResponseEntity.created(location).body(importBookResponse);
    }

    @GetMapping(path = "/import/execution/{executionId:\\d+}")
    public ResponseEntity<ImportBookResponse> getBook(@PathVariable("executionId") Long executionId) {
        final var jobExecution = jobExplorer.getJobExecution(executionId);
        if (jobExecution == null) {
            throw new JobNotFoundException("Job Execution not found (%d)".formatted(executionId));
        }
        String correlationId = jobExecution.getJobParameters().getString(JobParameterNames.CORRELATION_ID);
        final var importBookResponse = new ImportBookResponse(
                correlationId,
                jobExecution.getJobId(),
                jobExecution.getId(),
                jobExecution.getCreateTime(),
                jobExecution.getStartTime(),
                jobExecution.getEndTime(),
                jobExecution.getStatus().name(),
                jobExecution.getExitStatus().getExitDescription());
        return ResponseEntity.ok(importBookResponse);
    }
}
