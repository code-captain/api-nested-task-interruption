package api.test.configs;

import api.test.services.TestService;
import api.test.services.TestServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.concurrent.Executor;

@Configuration
@Import({ConcurrentConfig.class})
public class ServicesConfig {
    @Bean
    public TestService testService(@Qualifier("request-context-aware-executor") Executor executor) {
        return new TestServiceImpl(executor);
    }
}
