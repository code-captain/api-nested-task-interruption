package api.nested.task.services;

import api.nested.task.models.NestedTaskView;

import java.util.concurrent.CompletableFuture;

public interface NestedTaskService {
    CompletableFuture<NestedTaskView> getView(NestedTaskServiceContext context);
}
