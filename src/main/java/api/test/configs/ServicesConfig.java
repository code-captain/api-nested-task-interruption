package api.test.configs;

import api.test.models.ExecutorContext;
import api.test.services.TestServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import api.test.services.TestService;

import java.util.concurrent.Executor;

@Configuration
@Import({ConcurrentConfig.class})
public class ServicesConfig {
    @Bean
    public TestService testService(
            @Qualifier("custom-executor") Executor executor,
            ExecutorContext executorContext
    ) {
        return new TestServiceImpl(executor, executorContext);
    }
}
