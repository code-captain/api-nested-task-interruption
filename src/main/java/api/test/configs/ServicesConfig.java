package api.test.configs;

import api.test.configs.listeners.ApplicationRequestContextListenerContainer;
import api.test.services.TestService;
import api.test.services.TaskInterruptByTimeoutService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.concurrent.Executor;

@Configuration
@Import({ConcurrentConfig.class})
public class ServicesConfig {
    @Bean
    public TestService timeoutTaskCancellingService(
            @Qualifier("default-executor") Executor executor,
            ApplicationRequestContextListenerContainer requestContextListenerContainer
    ) {
        return new TaskInterruptByTimeoutService(executor, requestContextListenerContainer);
    }
}
