package api.test.services;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

@Component
@Getter
@Setter
@NoArgsConstructor
public class ExecutorContext {
    private Map<Object, Object> destroyedRequestStatuses = Collections.synchronizedMap(new WeakHashMap<>());
}
