package org.springdemo.batchweb.config;

import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class BatchConfig extends DefaultBatchConfiguration {

    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository, TaskExecutor taskExecutor) throws Exception {
        var jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(taskExecutor);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Bean
    @Primary
    public TaskExecutor taskExecutor() {
        var taskExecutor = new SimpleAsyncTaskExecutor();
        // taskExecutor.setVirtualThreads(true); // For Java Version >= 21
        return taskExecutor;
    }
}
