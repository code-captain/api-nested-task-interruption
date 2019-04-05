package api.test.services;

import api.test.models.TestView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

public class ExplicitTaskCancellingTestService implements TestService {
    private final Logger LOGGER = LogManager.getLogger(getClass().getName());
    private Executor executor;

    public ExplicitTaskCancellingTestService(Executor executor) {
        this.executor = executor;
    }

    @Override
    public CompletableFuture<TestView> getView(TestServiceContext context) {
        TestView view = new TestView();
        view.setId(String.valueOf(context.getRequestId()));
        return createTaskIdWithDelay(view.getId(), false)
                .thenCompose(rootStr -> {
                    List<CompletableFuture<String>> innerTasks = new ArrayList<>();
                    innerTasks.add(
                            getTaskIdWithDelay(view, rootStr, false)
                                    .thenCompose(rootStr1 -> getTaskIdWithDelay(view, rootStr1, false))
                    );

                    innerTasks.add(
                        getTaskIdWithDelay(view, rootStr, true)
                            .thenCompose(rootStr1 -> getTaskIdWithDelay(view, rootStr1, false))
                    );

                    innerTasks.add(
                            getTaskIdWithDelay(view, rootStr, false)
                                    .thenCompose(rootStr1 -> getTaskIdWithDelay(view, rootStr1, false))
                    );
                    return CompletableFuture.allOf(innerTasks.toArray(new CompletableFuture[]{}));
                })
                .thenApply(none -> view);
    }

    private CompletableFuture<String> getTaskIdWithDelay(TestView view, Object rootId, boolean isThrowingEx) {
        return createTaskIdWithDelay(rootId, isThrowingEx)
                .thenApply(str -> {
                    view.getDescendantTaskIds().add(str);
                    LOGGER.info("Current added id to descendantTaskIds {}", str);
                    return str;
                });
    }

    private CompletableFuture<String> createTaskIdWithDelay(Object rootId, boolean isThrowingEx) {
        return CompletableFuture.supplyAsync(() -> {
            checkThrowingException(isThrowingEx);
            try {
                Thread.sleep(350);
            } catch (InterruptedException e) {
                LOGGER.error("Thread was interrupted", e);
            }
            return getCreatedDescendentTaskId(rootId);
        }, executor);
    }

    private void checkThrowingException(boolean isThrowingEx) {
        if (isThrowingEx) {
            LOGGER.error("Task was rejected for execute");
            throw new RuntimeException(
                    new InterruptedException("Task was rejected for execute")
            );
        }
    }

    private String getCreatedDescendentTaskId(Object rootId) {
        int descendId = new Random().nextInt(100);
        return createDescendentTaskId(rootId, descendId);
    }

    private String createDescendentTaskId(Object rootId, Object objectId) {
        return String.format("%s----%s", rootId, objectId);
    }
}