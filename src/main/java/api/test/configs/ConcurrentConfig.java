package api.test.configs;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class ConcurrentConfig  implements AsyncConfigurer {
    private static final String NAME = "global";

    @Bean
    @Qualifier("custom-executor")
    public Executor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(400);
        executor.setQueueCapacity(20);
        executor.setKeepAliveSeconds(30);
        executor.setThreadNamePrefix("test-pool-" + NAME);
        executor.setThreadGroupName(NAME);
        //executor.setAwaitTerminationSeconds(timeout);
        executor.setAllowCoreThreadTimeOut(true);
        executor.initialize();
        return executor;
    }
}

