package api.test.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TaskTreeTraversalView {
    private long id;
    private  List<String> contents = new ArrayList<>();
}
