package api.test.configs;

import api.test.services.ExecutorContext;
import api.test.services.ExplicitTaskCancellingTestService;
import api.test.services.TestService;
import api.test.services.TimeoutTaskCancellingService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.concurrent.Executor;

@Configuration
@Import({ConcurrentConfig.class})
public class ServicesConfig {
    @Bean
    @Qualifier("timeout-task-cancelling")
    public TestService timeoutTaskCancellingService(@Qualifier("default-executor") Executor executor, ExecutorContext executorContext) {
        return new TimeoutTaskCancellingService(executor, executorContext);
    }

    @Bean
    @Qualifier("explicit-task-cancelling")
    public TestService explicitTaskCancellingTestService(@Qualifier("default-executor") Executor executor) {
        return new ExplicitTaskCancellingTestService(executor);
    }
}
