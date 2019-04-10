package api.nested.task.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NestedTaskServiceContext {
    private Object requestId;
    private long firstLevelDescendentTaskDelay;
    private long secondLevelDescendentTaskDelay;
    private long thirdLevelDescendentTaskDelay;
}
