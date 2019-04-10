package api.nested.task.configs;

import api.nested.task.configs.listeners.ApplicationRequestContextListenerContainer;
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
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class ConcurrentConfig implements AsyncConfigurer {

    @Bean
    @Qualifier("default-executor")
    public Executor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(0);
        executor.setMaxPoolSize(400);
        executor.setQueueCapacity(0);
        executor.setKeepAliveSeconds(30);
        executor.setThreadNamePrefix("default-pool-");
        executor.setThreadGroupName("default-");
        executor.setAwaitTerminationSeconds(30);
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
        executor.setThreadNamePrefix("context-aware-pool-");
        executor.setThreadGroupName("context-aware");
        executor.setAwaitTerminationSeconds(30);
        executor.setAllowCoreThreadTimeOut(true);
        executor.initialize();

        return executor;
    }

/*    @Override
    public Executor getAsyncExecutor() {
        return requestContextAwareExecutor();
    }*/

    public class RequestContextAwarePoolExecutor extends ThreadPoolTaskExecutor {
        @Override
        public void execute(Runnable task) {
            super.execute(new RequestContextAwareRunnable(task, RequestContextHolder.currentRequestAttributes()));
        }
    }

    public class RequestContextAwareRunnable implements Runnable {
        private Runnable task;
        private RequestAttributes context;
        private final Logger logger = LogManager.getLogger(getClass().getName());

        RequestContextAwareRunnable(Runnable task, RequestAttributes context) {
            this.task = task;
            this.context = context;
        }

        @Override
        public void run()  {
            if (context != null) {
                RequestContextHolder.setRequestAttributes(context, true);
            }
            Object uuid = getRequestUuid(RequestContextHolder.getRequestAttributes());
            Object errorCode = getRequestErrorCode(RequestContextHolder.getRequestAttributes());
/*            List<String> requestAttributes = new ArrayList<>();
            String attributeName = null;
            while ((attributeName = getRequest(RequestContextHolder.getRequestAttributes()).getAttributeNames().nextElement()) != null) {
                requestAttributes.add(attributeName);
            }*/
            long threadId = Thread.currentThread().getId();
            logger.info("Request uuid {} with status {} before task was runned, threadId {}", uuid, errorCode, threadId);
            //logger.info("Request attributes {} before task was runned, threadId {}", Arrays.toString(requestAttributes.toArray()), threadId);
            task.run();
        }
    }

    private static Object getRequestUuid(RequestAttributes requestAttributes) {
        HttpServletRequest request = getRequest(requestAttributes);
        return  request.getAttribute(ApplicationRequestContextListenerContainer.REQUEST_ATTRIBUTES_UUID);
    }

    private static Object getRequestErrorCode(RequestAttributes requestAttributes) {
        HttpServletRequest request = getRequest(requestAttributes);
        Object attribute1 = request.getAttribute(ApplicationRequestContextListenerContainer.REQUEST_ATTRIBUTES_ERROR_CODE);
        if (Objects.isNull(attribute1)) {
            attribute1 = request.getAttribute("javax.servlet.error.status_code");
        }
        return attribute1;
    }

    private static HttpServletRequest getRequest(RequestAttributes requestAttributes) {
        HttpServletRequest request = null;
        if (requestAttributes != null) {
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
            request = servletRequestAttributes.getRequest();
        }
        return request;
    }
}

