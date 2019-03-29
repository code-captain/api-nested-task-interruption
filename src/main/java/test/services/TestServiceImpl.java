package test.services;

import org.apache.logging.log4j.LogManager;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import test.models.TestView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class TestServiceImpl implements TestService {
    private final Executor executor;

    public TestServiceImpl(Executor executor) {
        this.executor = executor;
    }

    public CompletableFuture<TestView> getView() {
        TestView testView = new TestView();
        return CompletableFuture.supplyAsync(() -> {
            testView.setId(new Random().nextLong());
            return testView;
        }, executor)
            .thenCompose(nope -> {
                List<CompletableFuture<Void>> level1DescendentTasks = new ArrayList<>();

                CompletableFuture<Void> getLevel1DescendentTask1 = getDescendentTaskWithTimeout(testView, "1", 1000, false)
                    .thenCompose(rootStr -> {
                        List<CompletableFuture<String>> level2DescendentTasks = new ArrayList<>();
                        CompletableFuture<String> descendentTaskWithTimeout1 = getDescendentTaskWithTimeout(testView, rootStr, 500, false)
                                .thenCompose(rootStr1 -> getDescendentTaskWithTimeout(testView, rootStr1, 10000, false));
                        level2DescendentTasks.add(descendentTaskWithTimeout1);
                        level2DescendentTasks.add(getDescendentTaskWithTimeout(testView, rootStr, 2000, false));

                        return CompletableFuture.allOf(level2DescendentTasks.toArray(new CompletableFuture[]{}));
                    });
                level1DescendentTasks.add(getLevel1DescendentTask1);

                CompletableFuture<Void> getDescendentTask2 = getDescendentTaskWithTimeout(testView, "2", 10, true)
                    .thenCompose(rootStr -> {
                        List<CompletableFuture<String>> level2DescendentTasks = new ArrayList<>();
                        level2DescendentTasks.add(getDescendentTaskWithTimeout(testView, rootStr, 500, false));
                        level2DescendentTasks.add(getDescendentTaskWithTimeout(testView, rootStr, 500, false));

                        return CompletableFuture.allOf(level2DescendentTasks.toArray(new CompletableFuture[]{}));
                    });
                level1DescendentTasks.add(getDescendentTask2);

                return CompletableFuture.allOf(level1DescendentTasks.toArray(new CompletableFuture[]{}));
            })
            .thenApply(nope -> testView);
    }

    private CompletableFuture<String> getDescendentTaskWithTimeout(TestView rootView, String rootId, long millis, boolean isThrowingException) {
        return createDescendentTaskWithTimeout(rootId, millis, isThrowingException)
                .thenApply(str -> {
                    rootView.getContents().add(str);
                    LogManager.getLogger(getClass().getName()).info("Current str={}", str);
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
                        e.printStackTrace();
                    }
                    String formatStr = String.format("%s.%s", rootId, new Random().nextInt(100));
                    LogManager.getLogger(getClass().getName()).info("Generate str={} to result", formatStr);
                    return formatStr;
                }, executor);
    }
}