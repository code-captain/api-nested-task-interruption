package api.test.configs;

import api.test.models.ExecutorContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

@Configuration
public class RequestScopeConfig {
    @Bean
    @RequestScope
    public ExecutorContext executorContext() {
        return new ExecutorContext();
    }
}
