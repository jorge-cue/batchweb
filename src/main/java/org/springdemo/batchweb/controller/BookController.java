package org.springdemo.batchweb.controller;

import org.springdemo.batchweb.controller.dto.ImportBookRequest;
import org.springdemo.batchweb.controller.dto.ImportBookResponse;
import org.springdemo.batchweb.exception.JobNotFoundException;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping(path = "/api/v1/books")
public class BookController {

    public static final String INPUT_FILE_NAME = "inputFileName";
    public static final String EFFECTIVE_DATE = "effectiveDate";

    private final JobLauncher jobLauncher;

    private final JobExplorer jobExplorer;

    private final Job job;

    @Autowired
    public BookController(JobLauncher jobLauncher, JobExplorer jobExplorer, @Qualifier("importBooksJob") Job job) {
        this.jobLauncher = jobLauncher;
        this.jobExplorer = jobExplorer;
        this.job = job;
    }

    @PostMapping(path = "/import")
    public ResponseEntity<ImportBookResponse> importBook(@RequestBody ImportBookRequest request) throws
            JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException,
            JobRestartException {
        final var jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addString(INPUT_FILE_NAME, request.inputFileName());
        jobParametersBuilder.addLocalDate(EFFECTIVE_DATE, request.effectiveDate(), true);
        final var jobExecution = jobLauncher.run(job, jobParametersBuilder.toJobParameters());
        final var importBookResponse = new ImportBookResponse(
                jobExecution.getJobId(),
                jobExecution.getId(),
                jobExecution.getCreateTime(),
                jobExecution.getStartTime(),
                jobExecution.getEndTime(),
                jobExecution.getStatus().name(),
                jobExecution.getExitStatus().getExitDescription());
        final var location = UriComponentsBuilder.fromPath("/api/v1/books/execution/{executionId}").build(jobExecution.getId());
        return ResponseEntity.created(location).body(importBookResponse);
    }

    @GetMapping(path = "/execution/{executionId:\\d+}")
    public ResponseEntity<ImportBookResponse> getBook(@PathVariable("executionId") Long executionId) {
        final var jobExecution = jobExplorer.getJobExecution(executionId);
        if (jobExecution == null) {
            throw new JobNotFoundException("Job Execution not found (%d)".formatted(executionId));
        }
        final var importBookResponse = new ImportBookResponse(
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
