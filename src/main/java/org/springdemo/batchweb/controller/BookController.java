package org.springdemo.batchweb.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdemo.batchweb.controller.dto.ImportBookResponse;
import org.springdemo.batchweb.exception.JobNotFoundException;
import org.springdemo.batchweb.model.Book;
import org.springdemo.batchweb.service.BookService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.springdemo.batchweb.job.ImportBooksJobConfig.IMPORT_BOOKS_CORRELATION_ID_PARAMETER;
import static org.springdemo.batchweb.job.ImportBooksJobConfig.IMPORT_BOOKS_FILE_PATH_PARAMETER;
import static org.springdemo.batchweb.job.ImportBooksJobConfig.IMPORT_BOOKS_JOB_NAME;

@RestController
@RequestMapping(path = "/api/v1/books")
public class BookController {

    private static final Logger log = LoggerFactory.getLogger(BookController.class);
    public static final String X_CORRELATION_ID = "X-Correlation-ID";

    private final JobLauncher jobLauncher;

    private final JobExplorer jobExplorer;

    private final Job job;

    private final BookService bookService;

    @Autowired
    public BookController(JobLauncher jobLauncher, JobExplorer jobExplorer, @Qualifier(IMPORT_BOOKS_JOB_NAME) Job job,
                          BookService bookService) {
        this.jobLauncher = jobLauncher;
        this.jobExplorer = jobExplorer;
        this.job = job;
        this.bookService = bookService;
    }

    @Operation(method = "POST",
            operationId = "import-book-start",
            summary = "Launches a JOB to import the specified file",
            description = """
                    Launches the job to import the specified file into the books table
                    """
    )
    @Parameters({
        @Parameter(name = X_CORRELATION_ID, in = ParameterIn.HEADER),
        @Parameter(name = "file", description = """
                File to import received in a MultipartFile parameter.
                
                Must be a CSV file with the following fields:
                    ISBN
                    TITLE
                    AUTHORS
                    PUBLISHING YEAR
                """,
                allowEmptyValue = true,
                schema = @Schema(implementation = MultipartFile.class)),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Accepted",
                    content = @Content(schema = @Schema(implementation = ImportBookResponse.class)))
    })
    @SuppressWarnings("ReassignedVariable")
    @PostMapping(path = "/import/start")
    public ResponseEntity<ImportBookResponse> importBook(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = X_CORRELATION_ID, defaultValue = "") String correlationId)
            throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException, IOException {
        log.info("Import book started");
        if (Strings.isEmpty(correlationId)) {
            correlationId = UUID.randomUUID().toString();
            log.info("Generated Correlation ID: {}", correlationId);
        }
        var tempFile = File.createTempFile(IMPORT_BOOKS_JOB_NAME + "-" + correlationId, ".tmp");
        log.info("Generated temp file: {}, transferring uploaded file", tempFile.getAbsolutePath());
        file.transferTo(tempFile);
        log.info("Uploaded file is onboard");
        final var jobParameters = new JobParametersBuilder()
            .addString(IMPORT_BOOKS_CORRELATION_ID_PARAMETER, correlationId, true)
            .addString(IMPORT_BOOKS_FILE_PATH_PARAMETER, tempFile.getAbsolutePath(), false)
            .toJobParameters();
        final var jobExecution = jobLauncher.run(job, jobParameters);
        final var importBookResponse = new ImportBookResponse(
                correlationId,
                jobExecution.getJobId(),
                jobExecution.getId(),
                jobExecution.getCreateTime(),
                jobExecution.getStartTime(),
                jobExecution.getEndTime(),
                jobExecution.getStatus().name(),
                jobExecution.getExitStatus().getExitDescription());
        return ResponseEntity.accepted().header(X_CORRELATION_ID, correlationId).body(importBookResponse);
    }

    @Operation(method = "GET",
        operationId = "import-books-status",
        summary = "Gets status of the import job with the assigned correlation id",
        description = """
                Retrieves status of the given jon
                """
    )
    @Parameters({
            @Parameter(name = "correlationId", in = ParameterIn.PATH, required = true),
    })
    @GetMapping(path = "/import/status/{correlationId}")
    public ResponseEntity<ImportBookResponse> getBook(@PathVariable("correlationId") final String correlationId) {
        var jobParameters = new JobParametersBuilder()
                .addString(IMPORT_BOOKS_CORRELATION_ID_PARAMETER, correlationId, true)
                .toJobParameters();
        var jobInstance = jobExplorer.getJobInstance("importBooksJob", jobParameters);
        var jobExecution = jobInstance == null ? null : jobExplorer.getJobExecution(jobInstance.getId());
        if (jobExecution == null) {
            throw new JobNotFoundException("Job with correlation id (%s) not found".formatted(correlationId));
        }
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

    @Operation(method = "GET", operationId = "book-index", description = """
            Gets the list of books
            """)
    @GetMapping
    public ResponseEntity<List<Book>> index() {
        var books = bookService.findAll().stream().toList();
        return ResponseEntity.ok(books);
    }
}
