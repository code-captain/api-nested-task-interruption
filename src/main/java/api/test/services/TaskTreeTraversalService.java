package api.test.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import api.test.models.TaskTreeTraversalView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class TaskTreeTraversalService implements TestService {
    private final Logger LOGGER = LogManager.getLogger(getClass().getName());
    private final Executor executor;

    public TaskTreeTraversalService(Executor executor) {
        this.executor = executor;
    }

    public CompletableFuture<TaskTreeTraversalView> getView(HttpServletRequest request, HttpServletResponse response) {
        TaskTreeTraversalView view = new TaskTreeTraversalView();
        return CompletableFuture.supplyAsync(() -> {
            view.setId(new Random().nextLong());
            return view;
        }, executor)
                .thenCompose(nope -> {
                    List<CompletableFuture<Void>> level1DescendentTasks = new ArrayList<>();

                    CompletableFuture<Void> getLevel1DescendentTask1 = getDescendentTaskWithTimeout(request, response, view, "1", 500, false)
                            .thenCompose(rootStr -> {
                                List<CompletableFuture<String>> level2DescendentTasks = new ArrayList<>();
                                CompletableFuture<String> descendentTaskWithTimeout1 = getDescendentTaskWithTimeout(request, response, view, rootStr, 500, false)
                                        .thenCompose(rootStr1 -> getDescendentTaskWithTimeout(request, response, view, rootStr1, 10000, false));
                                level2DescendentTasks.add(descendentTaskWithTimeout1);
                                level2DescendentTasks.add(getDescendentTaskWithTimeout(request, response, view, rootStr, 2000, false));

                                return CompletableFuture.allOf(level2DescendentTasks.toArray(new CompletableFuture[]{}));
                            });
                    level1DescendentTasks.add(getLevel1DescendentTask1);

                    CompletableFuture<Void> getDescendentTask2 = getDescendentTaskWithTimeout(request, response, view, "2", 10, true)
                            .thenCompose(rootStr -> {
                                List<CompletableFuture<String>> level2DescendentTasks = new ArrayList<>();
                                level2DescendentTasks.add(getDescendentTaskWithTimeout(request, response, view, rootStr, 500, false));
                                level2DescendentTasks.add(getDescendentTaskWithTimeout(request, response, view, rootStr, 500, false));

                                return CompletableFuture.allOf(level2DescendentTasks.toArray(new CompletableFuture[]{}));
                            });
                    level1DescendentTasks.add(getDescendentTask2);

                    return CompletableFuture.allOf(level1DescendentTasks.toArray(new CompletableFuture[]{}));
                })
                .thenApply(nope -> view);
    }

    private CompletableFuture<String> getDescendentTaskWithTimeout(
            HttpServletRequest request,
            HttpServletResponse response,
            TaskTreeTraversalView rootView,
            String rootId,
            long millis,
            boolean isThrowingException
    ) {
        return createDescendentTaskWithTimeout(rootId, millis, isThrowingException)
                .thenApply(str -> {
                    rootView.getContents().add(str);
                    LOGGER.info("Current id={}", str);
                    LOGGER.info("Current response status={}", response.getStatus());
                    return str;
                });
    }

    private CompletableFuture<String> createDescendentTaskWithTimeout(String rootId, long millis, boolean isThrowingException) {
        return CompletableFuture.supplyAsync(
                () -> {
                    if (isThrowingException) {
                        throw new AsyncRequestTimeoutException();
                    }
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                        LOGGER.error("Thread was interrupted", e);
                    }
                    return createTraversalNode(rootId);
                }, executor);
    }

    private String createTraversalNode(String root) {
        int descendId = new Random().nextInt(100);
        LOGGER.info("Add id={} to path", descendId);

        return String.format("%s.%s", root, descendId);
    }
}