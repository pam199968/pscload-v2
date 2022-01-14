/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * The Class AsynchronousConfiguration.
 */
@Configuration
@Slf4j
public class AsynchronousConfiguration {

	/**
	 * Work executor.
	 *
	 * @return the task executor
	 */
	@Bean(name="processExecutor")
	public TaskExecutor workExecutor() {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setThreadNamePrefix("Async-");
		threadPoolTaskExecutor.setCorePoolSize(3);
		threadPoolTaskExecutor.setMaxPoolSize(3);
		threadPoolTaskExecutor.setQueueCapacity(600);
		threadPoolTaskExecutor.afterPropertiesSet();
		log.info("ThreadPoolTaskExecutor set");
		return threadPoolTaskExecutor;
	}
}
