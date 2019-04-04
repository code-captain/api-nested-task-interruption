package api.test.configs;

import api.test.services.ExecutorContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletRequestEvent;
import javax.servlet.annotation.WebListener;
import java.util.UUID;

@Configuration
@WebListener
public class CustomRequestContextListener extends RequestContextListener {
    private final ExecutorContext executorContext;
    private static final String REQUEST_ATTRIBUTES_ATTRIBUTE =
            RequestContextListener.class.getName() + ".REQUEST_ATTRIBUTES";

    public CustomRequestContextListener(ExecutorContext executorContext) {
        this.executorContext = executorContext;
    }

    @Override
    public void requestInitialized(ServletRequestEvent requestEvent) {
        super.requestInitialized(requestEvent);
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            requestAttributes.setAttribute("api.test.uuid", UUID.randomUUID(), 0);
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent requestEvent) {
        ServletRequestAttributes attributes = null;
        Object reqAttr = requestEvent.getServletRequest().getAttribute(REQUEST_ATTRIBUTES_ATTRIBUTE);
        if (reqAttr instanceof ServletRequestAttributes) {
            attributes = (ServletRequestAttributes) reqAttr;
        }
        if (attributes != null) {
            Object requestUuid = attributes.getAttribute("api.test.uuid", 0);
            Object requestErrorUuid = attributes.getAttribute("api.test.error-code", 0);
            if (requestUuid != null && requestErrorUuid != null) {
                executorContext.getDestroyedRequestStatuses().putIfAbsent(requestUuid, requestErrorUuid);
            }
        }
        super.requestDestroyed(requestEvent);
    }
}
