package api.nested.task.configs.listeners;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

@Component
@Getter
@Setter
public class ApplicationRequestContextListenerContainer {
    private static final String REQUEST_ATTRIBUTES_PREFIX = ApplicationRequestContextListenerContainer.class.getName();
    public static final String REQUEST_ATTRIBUTES_UUID = REQUEST_ATTRIBUTES_PREFIX + ".UUID";
    public static final String REQUEST_ATTRIBUTES_ERROR_CODE = REQUEST_ATTRIBUTES_PREFIX + ".ERROR_CODE";

    private Map<Object, Object> destroyedRequestErrorCodes = Collections.synchronizedMap(new WeakHashMap<>());
}
