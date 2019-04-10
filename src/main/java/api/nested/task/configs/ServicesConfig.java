package api.nested.task.configs;

import api.nested.task.configs.listeners.ApplicationRequestContextListenerContainer;
import api.nested.task.services.NestedTaskService;
import api.nested.task.services.NestedTaskInterruptionService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.concurrent.Executor;

@Configuration
@Import({ConcurrentConfig.class})
public class ServicesConfig {
    @Bean
    public NestedTaskService nestedTaskService(
            @Qualifier("request-context-aware-executor") Executor executor,
            ApplicationRequestContextListenerContainer requestContextListenerContainer
    ) {
        return new NestedTaskInterruptionService(executor, requestContextListenerContainer);
    }
}
