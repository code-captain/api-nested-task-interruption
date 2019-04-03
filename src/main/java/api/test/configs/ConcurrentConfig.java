package api.test.configs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.Executor;

import static api.test.utils.RequestAttributesUtils.getRequest;

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
    @Qualifier("request-context-aware-executor")
    public Executor requestContextAwareExecutor() {
        ThreadPoolTaskExecutor executor = new RequestContextAwarePoolExecutor();
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

    public class RequestContextAwarePoolExecutor extends ThreadPoolTaskExecutor {
        private final Logger LOGGER = LogManager.getLogger(getClass().getName());

        @Override
        public void execute(Runnable task) {
            //try {
                HttpServletRequest innerRequest = getRequest(RequestContextHolder.getRequestAttributes());
                Object requestUuid = innerRequest.getAttribute("uuid");
                if (requestUuid != null) {
                    super.execute(new RequestContextAwareRunnable(task, RequestContextHolder.currentRequestAttributes()));
                    LOGGER.info("Task was added for execute for request requestId {}", requestUuid);
                } else {
                    LOGGER.warn("Task was rejected for execute for closing request");
                    throw new RuntimeException(new InterruptedException("Task was rejected for execute for closing request"));
                }
/*            } catch (Exception ex) {
                super.execute(task);
                LOGGER.warn("Was throwing exception before task was added for pool", ex);
            }*/
        }
    }

    public class RequestContextAwareRunnable implements Runnable {
        private Runnable task;
        private RequestAttributes context;

        RequestContextAwareRunnable(Runnable task, RequestAttributes context) {
            this.task = task;
            this.context = context;
        }

        @Override
        public void run()  {
            RequestAttributes innerRequestAttributes = RequestContextHolder.getRequestAttributes();
            if (context != null && innerRequestAttributes == null) {
                RequestContextHolder.setRequestAttributes(context, true);
            }

            task.run();
            //RequestContextHolder.resetRequestAttributes();
        }
    }
}

