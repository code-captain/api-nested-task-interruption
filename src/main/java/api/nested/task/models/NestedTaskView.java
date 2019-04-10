package api.nested.task.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NestedTaskView {
    private String rootTaskId;
    private List<String> descendantTaskIds = Collections.synchronizedList(new ArrayList<>());
}
