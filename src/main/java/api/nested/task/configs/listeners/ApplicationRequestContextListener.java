package api.nested.task.configs.listeners;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletRequestEvent;
import javax.servlet.annotation.WebListener;
import java.util.UUID;

@Configuration
@WebListener
public class ApplicationRequestContextListener extends RequestContextListener {
    private final ApplicationRequestContextListenerContainer container;
    private static final String REQUEST_ATTRIBUTES_ATTRIBUTE =
            RequestContextListener.class.getName() + ".REQUEST_ATTRIBUTES";

    public ApplicationRequestContextListener(ApplicationRequestContextListenerContainer container) {
        this.container = container;
    }

    @Override
    public void requestInitialized(ServletRequestEvent requestEvent) {
        super.requestInitialized(requestEvent);
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            requestAttributes.setAttribute(ApplicationRequestContextListenerContainer.REQUEST_ATTRIBUTES_UUID, UUID.randomUUID(), 0);
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent requestEvent) {
        ServletRequestAttributes attributes = null;
        Object reqAttr = requestEvent.getServletRequest().getAttribute(REQUEST_ATTRIBUTES_ATTRIBUTE);
        if (reqAttr instanceof ServletRequestAttributes) {
            attributes = (ServletRequestAttributes) reqAttr;
        }
        RequestAttributes threadAttributes = RequestContextHolder.getRequestAttributes();
        if (threadAttributes != null) {
            // We're assumably within the original request thread...
            LocaleContextHolder.resetLocaleContext();
            RequestContextHolder.resetRequestAttributes();
            if (attributes == null && threadAttributes instanceof ServletRequestAttributes) {
                attributes = (ServletRequestAttributes) threadAttributes;
            }
        }
        if (attributes != null) {
            //Object requestUuid = attributes.getAttribute(ApplicationRequestContextListenerContainer.REQUEST_ATTRIBUTES_UUID, 0);
            //Object requestErrorUuid = attributes.getAttribute(ApplicationRequestContextListenerContainer.REQUEST_ATTRIBUTES_ERROR_CODE, 0);
            //if (requestUuid != null && requestErrorUuid != null) {
            //    container.getDestroyedRequestErrorCodes().putIfAbsent(requestUuid, requestErrorUuid);
            //}
            attributes.requestCompleted();
        }
    }
}
