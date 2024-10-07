package org.springdemo.batchweb.job;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdemo.batchweb.entity.BookEntity;
import org.springdemo.batchweb.model.Book;
import org.springdemo.batchweb.repository.BookRepository;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.separator.DefaultRecordSeparatorPolicy;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

@Configuration
public class ImportBooksJobConfig {

    private static final Logger log = LoggerFactory.getLogger(ImportBooksJobConfig.class);

    public static final String IMPORT_BOOKS_JOB_NAME = "importBooksJob";
    public static final String IMPORT_BOOKS_FILE_PATH_PARAMETER = "file-path";
    public static final String IMPORT_BOOKS_CORRELATION_ID_PARAMETER = "correlation-id";

    private static final String FIELD_NAME_ISBN = "ISBN";
    private static final String FIELD_NAME_TITLE = "TITLE";
    private static final String FIELD_NAME_AUTHORS = "AUTHORS";
    private static final String FIELD_NAME_YEAR_PUBLISHED = "YEAR_PUBLISHED";

    @Bean
    public Job importBooksJob(JobRepository jobRepository, Step importBooksStart, JobExecutionListener jobListener,
                              MeterRegistry meterRegistry) {
        return new JobBuilder("importBooks", jobRepository)
                .listener(jobListener)
                .start(importBooksStart)
                .meterRegistry(meterRegistry)
                .build();
    }

    @Bean
    public Step importBooksStart(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                 StepExecutionListener listener,
                                 ItemReader<Book> bookReader,
                                 ItemProcessor<Book, BookEntity> bookProcessor,
                                 ItemWriter<BookEntity> bookWriter) {
        return new StepBuilder("importBooksStart", jobRepository)
                .<Book, BookEntity>chunk(100, transactionManager)
                .listener(listener)
                .reader(bookReader)
                .processor(bookProcessor)
                .writer(bookWriter)
                .build();
    }


    @Bean(name = "importBooksStartReader")
    @StepScope
    public FlatFileItemReader<Book> bookReader(@Value("#{jobParameters}") Map<String, Object> jobParameters, LineMapper<Book> lineMapper) {
        String filePath = (String) jobParameters.get(IMPORT_BOOKS_FILE_PATH_PARAMETER);
        var path = Path.of(filePath);
        Resource resource = new PathResource(path);
        String correlationId = (String) jobParameters.get(IMPORT_BOOKS_CORRELATION_ID_PARAMETER);
        return new FlatFileItemReaderBuilder<Book>()
                .name("importBooksStartReader." + correlationId)
                .linesToSkip(1) // Skip title line
                .recordSeparatorPolicy(new DefaultRecordSeparatorPolicy())
                .resource(resource)
                .strict(true)
                .lineMapper(lineMapper)
                .build();
    }

    @Bean
    public LineMapper<Book> bookLineMapper(LineTokenizer lineTokenizer, FieldSetMapper<Book> fieldSetMapper) {
        var lineMapper = new DefaultLineMapper<Book>();
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        lineMapper.afterPropertiesSet();
        return lineMapper;
    }

    @Bean
    LineTokenizer lineTokenizer() throws Exception {
        var lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setNames(FIELD_NAME_ISBN, FIELD_NAME_TITLE, FIELD_NAME_AUTHORS, FIELD_NAME_YEAR_PUBLISHED);
        lineTokenizer.setQuoteCharacter('"');
        lineTokenizer.setStrict(true);
        lineTokenizer.afterPropertiesSet();
        return lineTokenizer;
    }

    @Bean
    FieldSetMapper<Book> fieldSetMapper() {
        return fieldSet -> new Book(null,
                fieldSet.readString(FIELD_NAME_ISBN), fieldSet.readString(FIELD_NAME_TITLE),
                fieldSet.readString(FIELD_NAME_AUTHORS), fieldSet.readInt(FIELD_NAME_YEAR_PUBLISHED));
    }
    @Bean
    public ItemProcessor<Book, BookEntity> bookProcessor(Function<Book, BookEntity> mapper) {
        return mapper::apply;
    }

    @Bean
    public ItemWriter<BookEntity> bookWriter(BookRepository bookRepository) {
        return chunk -> {
            log.info("About to save chunk of {} books: items %n{}", chunk.getItems().size(), chunk.getItems());
            var items = bookRepository.saveAll(chunk.getItems());
            log.info("Books saved {}", items);
        };
    }

    @Bean
    public JobExecutionListener jobExecutionListener(JobRepository jobRepository) {
        return new JobExecutionListener() {

            private static final Logger log = LoggerFactory.getLogger("org.springdemo.batchweb.job.ImportBooksJobExecutionListener");

            @Override
            public void beforeJob(final JobExecution jobExecution) {
                jobRepository.updateExecutionContext(jobExecution);
                log.info("Before Job: {} Starting at {}",
                        jobExecution.getJobInstance().getJobName(),
                        jobExecution.getStartTime());
            }

            @Override
            public void afterJob(final JobExecution jobExecution) {
                jobRepository.updateExecutionContext(jobExecution);
                log.info("Finishing Job: {} Ending at {} with exit status {}",
                        jobExecution.getJobInstance().getJobName(),
                        jobExecution.getStartTime(),
                        jobExecution.getExitStatus().getExitDescription());
            }
        };
    }

    @Bean
    public StepExecutionListener stepExecutionListener(final JobRepository jobRepository) {
        return new StepExecutionListener() {

            private static final Logger log = LoggerFactory.getLogger("org.springdemo.batchweb.job.ImportBooksStepExecutionListener");

            @Override
            public void beforeStep(final StepExecution stepExecution) {
                jobRepository.updateExecutionContext(stepExecution);
                log.info("Step {} started at {}", stepExecution.getStepName(), stepExecution.getStartTime());
            }

            @Override
            public ExitStatus afterStep(final StepExecution stepExecution) {
                jobRepository.updateExecutionContext(stepExecution);
                log.info("Step {} finished at {} with status {}",
                        stepExecution.getStepName(),
                        stepExecution.getEndTime(),
                        stepExecution.getExitStatus().getExitDescription());
                return ExitStatus.COMPLETED;
            }
        };
    }

    @Bean
    public Function<Book, BookEntity> bookMapper() {
        return dto -> {
            if (dto == null) {
                return null;
            }
            return new BookEntity(dto.id(), dto.isbn(), dto.title(), dto.authors(), dto.yearPublished());
        };
    }
}
