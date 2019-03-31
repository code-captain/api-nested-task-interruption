package api.test.services;

import api.test.models.TaskTreeTraversalView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.CompletableFuture;

public interface TestService {
    CompletableFuture<TaskTreeTraversalView> getView(HttpServletRequest request, HttpServletResponse response);
}
