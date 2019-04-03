package api.test.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TestServiceContext {
    private final long FirstLevelDescendentTaskDelay;
    private final long SecondLevelDescendentTaskDelay;
    private final long ThirdLevelDescendentTaskDelay;
}
