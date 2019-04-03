package api.test.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TestView {
    private String id;
    private List<String> contents = Collections.synchronizedList(new ArrayList<>());
}
