package api.nested.task.exceptions;

import api.nested.task.configs.listeners.ApplicationRequestContextListenerContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ControllerAdvice
public class AsyncRequestTimeoutExceptionResolver {
    private final Logger logger = LogManager.getLogger(getClass().getName());

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    protected ModelAndView handleAsyncRequestTimeoutException(
            AsyncRequestTimeoutException ex,
            HttpServletRequest request,
            HttpServletResponse response,
            @Nullable Object handler
    ) throws IOException {
        request.setAttribute(ApplicationRequestContextListenerContainer.REQUEST_ATTRIBUTES_ERROR_CODE, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        Object requestUuid = request.getAttribute(ApplicationRequestContextListenerContainer.REQUEST_ATTRIBUTES_UUID);
        logger.warn("Async request timed out error for request {}",requestUuid);
        if (!response.isCommitted()) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        } else {
            logger.warn("Async request timed out");
        }
        return new ModelAndView();
    }
}
