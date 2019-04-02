package api.test.services;

import api.test.models.TestView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class TestServiceImpl implements TestService {
    private final Logger LOGGER = LogManager.getLogger(getClass().getName());
    private final Executor executor;

    public TestServiceImpl(Executor executor) {
        this.executor = executor;
    }

    public CompletableFuture<TestView> getView(HttpServletRequest request, HttpServletResponse response) {
        TestView view = new TestView();
        return CompletableFuture.supplyAsync(() -> {
            view.setId(new Random().nextLong());
            return view;
        }, executor).thenCompose(none -> {
            List<CompletableFuture<Void>> descendentTasksForLevel1 = new ArrayList<>();

            CompletableFuture<Void> getLevel1DescendentTask1 = getDescendentTaskWithTimeout(request, response, view, "1", 800, false)
                        .thenCompose(rootStr -> {
                            List<CompletableFuture<String>> level2DescendentTasks = new ArrayList<>();
                            CompletableFuture<String> descendentTaskWithTimeout1 = getDescendentTaskWithTimeout(request, response, view, rootStr, 2000, false)
                                    .thenCompose(rootStr1 -> getDescendentTaskWithTimeout(request, response, view, rootStr1, 2000, false));
                            level2DescendentTasks.add(descendentTaskWithTimeout1);
                            level2DescendentTasks.add(getDescendentTaskWithTimeout(request, response, view, rootStr, 2000, false));

                            return CompletableFuture.allOf(level2DescendentTasks.toArray(new CompletableFuture[]{}));
                        });
                descendentTasksForLevel1.add(getLevel1DescendentTask1);

                CompletableFuture<Void> getDescendentTask2 = getDescendentTaskWithTimeout(request, response, view, "2", 800, false)
                        .thenCompose(rootStr -> {
                            List<CompletableFuture<String>> level2DescendentTasks = new ArrayList<>();
                            level2DescendentTasks.add(getDescendentTaskWithTimeout(request, response, view, rootStr, 500, false));
                            level2DescendentTasks.add(getDescendentTaskWithTimeout(request, response, view, rootStr, 500, false));

                            return CompletableFuture.allOf(level2DescendentTasks.toArray(new CompletableFuture[]{}));
                        });
                descendentTasksForLevel1.add(getDescendentTask2);

            CompletableFuture<Void> getDescendentTask3 = getDescendentTaskWithTimeout(request, response, view, "3", 800, false)
                    .thenCompose(rootStr -> {
                        List<CompletableFuture<String>> level2DescendentTasks = new ArrayList<>();
                        level2DescendentTasks.add(getDescendentTaskWithTimeout(request, response, view, rootStr, 3000, false));
                        level2DescendentTasks.add(getDescendentTaskWithTimeout(request, response, view, rootStr, 3000, false));

                        return CompletableFuture.allOf(level2DescendentTasks.toArray(new CompletableFuture[]{}));
                    });
            descendentTasksForLevel1.add(getDescendentTask3);

                return CompletableFuture.allOf(descendentTasksForLevel1.toArray(new CompletableFuture[]{}));
            })
            .thenApply(nope -> view);
    }

    private CompletableFuture<String> getDescendentTaskWithTimeout(
            HttpServletRequest request,
            HttpServletResponse response,
            TestView rootView,
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
                    return createNodePath(rootId);
                }, executor);
    }

    private String createNodePath(String root) {
        int descendId = new Random().nextInt(100);
        LOGGER.info("Add id={} to path", descendId);

        return String.format("%s.%s", root, descendId);
    }
}