package test.services;

import org.apache.logging.log4j.LogManager;
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

                CompletableFuture<Void> getLevel1DescendentTask1 = getDescendentTask("1")
                    .thenApply(rootStr -> {
                        testView.getContents().add(rootStr);
                        LogManager.getLogger(getClass().getName()).info("Add str={} to result", rootStr);
                        return rootStr;
                    })
                    .thenCompose(rootStr -> {
                        List<CompletableFuture<Void>> level2DescendentTasks = new ArrayList<>();

                        CompletableFuture<Void> getLevel2DescendentTask1 = getDescendentTask(rootStr)
                                .thenAccept(str1 -> {
                                    testView.getContents().add(str1);
                                    LogManager.getLogger(getClass().getName()).info("Add str={} to result", str1);
                                });
                        level2DescendentTasks.add(getLevel2DescendentTask1);

                        CompletableFuture<Void> getLevel2DescendentTask2 = getDescendentTask(rootStr)
                                .thenAccept(str1 -> {
                                    testView.getContents().add(str1);
                                    LogManager.getLogger(getClass().getName()).info("Add str={} to result", str1);
                                });
                        level2DescendentTasks.add(getLevel2DescendentTask2);

                        return CompletableFuture.allOf(level2DescendentTasks.toArray(new CompletableFuture[]{}));
                    });
                level1DescendentTasks.add(getLevel1DescendentTask1);

                CompletableFuture<Void> getDescendentTask2 = getDescendentTask("2")
                    .thenApply(rootStr -> {
                        testView.getContents().add(rootStr);
                        LogManager.getLogger(getClass().getName()).info("Add str={} to result", rootStr);
                        return rootStr;
                    })
                    .thenCompose(rootStr -> {
                        List<CompletableFuture<Void>> level2DescendentTasks = new ArrayList<>();

                        CompletableFuture<Void> getLevel2DescendentTask1 = getDescendentTask(rootStr)
                                .thenAccept(str1 -> {
                                    testView.getContents().add(str1);
                                    LogManager.getLogger(getClass().getName()).info("Add str={} to result", str1);
                                });
                        level2DescendentTasks.add(getLevel2DescendentTask1);

                        CompletableFuture<Void> getLevel2DescendentTask2 = getDescendentTask(rootStr)
                                .thenAccept(str1 -> {
                                    testView.getContents().add(str1);
                                    LogManager.getLogger(getClass().getName()).info("Add str={} to result", str1);
                                });
                        level2DescendentTasks.add(getLevel2DescendentTask2);

                        return CompletableFuture.allOf(level2DescendentTasks.toArray(new CompletableFuture[]{}));
                    });
                level1DescendentTasks.add(getDescendentTask2);

                return CompletableFuture.allOf(level1DescendentTasks.toArray(new CompletableFuture[]{}));
            })
            .thenApply(nope -> testView);
    }

    private CompletableFuture<String> getDescendentTask(String id) {
        return CompletableFuture.supplyAsync(
            () -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return String.format("%s.%s", id, new Random().nextInt(100));
            }, executor);
    }

    private CompletableFuture<String> getDescendentFailedByTimeoutTask(String id) {
        return CompletableFuture.supplyAsync(
            () -> {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return String.format("%s.%s", id, new Random().nextInt(100));
            }, executor);
    }
}