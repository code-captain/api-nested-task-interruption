package test.configs;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import test.services.TestService;
import test.services.TestServiceImpl;

import java.util.concurrent.Executor;

@Configuration
@Import({ConcurrentConfig.class})
public class ServicesConfig {
    @Bean
    public TestService testService(
            @Qualifier("custom-executor") Executor executor
    ) {
        return new TestServiceImpl(executor);
    }
}
