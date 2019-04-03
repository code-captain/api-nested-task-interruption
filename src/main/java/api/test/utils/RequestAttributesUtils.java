package api.test.utils;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public final class RequestAttributesUtils {
    public static HttpServletRequest getRequest(RequestAttributes requestAttributes) {
        HttpServletRequest request = null;
        if(requestAttributes != null) {
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
            request = servletRequestAttributes.getRequest();
        }
        return request;
    }
}
