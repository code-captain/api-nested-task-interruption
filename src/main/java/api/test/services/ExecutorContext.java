package api.test.services;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@Getter
@Setter
@NoArgsConstructor
public class ExecutorContext {
    private Map<Object, Object> destroyedRequestStatuses = Collections.synchronizedMap(new HashMap<>());
}
