package api.test.configs;

import api.test.models.ExecutorContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

@Configuration
@EnableAsync
public class ConcurrentConfig  implements AsyncConfigurer {
    private static final String NAME = "global";

    @Bean
    @Qualifier("custom-executor")
    public Executor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(0);
        executor.setMaxPoolSize(400);
        executor.setQueueCapacity(0);
        executor.setKeepAliveSeconds(30);
        executor.setThreadNamePrefix("custom-pool-" + NAME);
        executor.setThreadGroupName(NAME);
        executor.setAllowCoreThreadTimeOut(true);
        executor.initialize();
        return executor;
    }

    @Bean
    @Qualifier("context-aware-executor")
    public Executor contextAwareExecutor(ExecutorContext executorContext, HttpServletResponse response) {
        ThreadPoolTaskExecutor executor = new ContextAwarePoolExecutor(executorContext, response);
        executor.setCorePoolSize(0);
        executor.setMaxPoolSize(400);
        executor.setQueueCapacity(0);
        executor.setKeepAliveSeconds(30);
        executor.setThreadNamePrefix("context-aware-pool-" + NAME);
        executor.setThreadGroupName(NAME);
        executor.setAllowCoreThreadTimeOut(true);
        executor.initialize();

        return executor;
    }

    public class ContextAwarePoolExecutor extends ThreadPoolTaskExecutor {
        private final ExecutorContext executorContext;
        private final HttpServletResponse response;
        private final Logger LOGGER = LogManager.getLogger(getClass().getName());

        public ContextAwarePoolExecutor(ExecutorContext executorContext, HttpServletResponse response) {
            this.executorContext = executorContext;
            this.response = response;
        }

        @Override
        public void execute(Runnable task) {
            try {
                LOGGER.warn("Response status before execute: {}", response.getStatus());
/*                RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                if (requestAttributes != null) {
                    HttpServletResponse innerResponse = getResponse(requestAttributes);
                    LOGGER.warn("Inner Response status before execute: {}", innerResponse.getStatus());
                    LOGGER.warn("Response status before execute: {}", response.getStatus());
                }*/
            } catch (Exception ex) {
                LOGGER.warn("Was throwing exception during getting Response status before execute: {}");
            }
            super.execute(new ContextAwareRunnable(task, RequestContextHolder.currentRequestAttributes()));
        }
    }

    public class ContextAwareRunnable implements Runnable {
        private Runnable task;
        private RequestAttributes context;

        public ContextAwareRunnable(Runnable task, RequestAttributes context) {
            this.task = task;
            this.context = context;
        }

        @Override
        public void run()  {
            if (context != null) {
                RequestContextHolder.setRequestAttributes(context);
            }

            task.run();
            RequestContextHolder.resetRequestAttributes();
        }
    }
}

