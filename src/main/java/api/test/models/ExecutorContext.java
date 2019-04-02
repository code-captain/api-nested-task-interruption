package api.test.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
public class ExecutorContext {
    private volatile boolean isFinished = false;
}
