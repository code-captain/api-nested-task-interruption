package api.test.exceptions;

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
    private final Logger LOGGER = LogManager.getLogger(getClass().getName());

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    protected ModelAndView handleAsyncRequestTimeoutException(
            AsyncRequestTimeoutException ex,
            HttpServletRequest request,
            HttpServletResponse response,
            @Nullable Object handler
    ) throws IOException {
        if (!response.isCommitted()) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            LOGGER.warn("AsyncRequestTimeoutException was handling for request id {}", request.getAttribute("uuid"));
        } else {
            LOGGER.warn("Async request timed out");
        }
        return new ModelAndView();
    }
}
