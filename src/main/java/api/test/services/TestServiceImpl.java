package api.test.services;

import api.test.models.TestView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class TestServiceImpl implements TestService {
    private final Logger LOGGER = LogManager.getLogger(getClass().getName());
    private final Executor executor;
    private final ExecutorContext executorContext;

    public TestServiceImpl(Executor executor, ExecutorContext executorContext) {
        this.executor = executor;
        this.executorContext = executorContext;
    }

    public CompletableFuture<TestView> getView(TestServiceContext context) {
        TestView view = new TestView();
        return CompletableFuture.runAsync(() -> {
            view.setId(String.valueOf(context.getRequestId()));
        }, executor).thenCompose(none -> {
            List<CompletableFuture<Void>> descendentTasksForLevel1 = new ArrayList<>();

            //Add first task for level-1
            CompletableFuture<Void> getLevel1DescendentTask1 =
                    getDescendentTaskWithDelay(context, view, context.getRequestId(), context.getFirstLevelDescendentTaskDelay())
                        .thenCompose(rootStr -> {
                            List<CompletableFuture<String>> descendentTasksForLevel2 = new ArrayList<>();

                            //Add first task for level-2 first task
                            descendentTasksForLevel2.add(
                                getDescendentTaskWithDelay(context, view, rootStr, context.getSecondLevelDescendentTaskDelay())
                                    .thenCompose(rootStr1 ->
                                            getDescendentTaskWithDelay(context, view, rootStr1, context.getThirdLevelDescendentTaskDelay())
                                    )
                            );

                            //Add second task for level-2 first task
                            descendentTasksForLevel2.add(
                                getDescendentTaskWithDelay(context, view, rootStr, context.getSecondLevelDescendentTaskDelay())
                            );

                            return CompletableFuture.allOf(descendentTasksForLevel2.toArray(new CompletableFuture[]{}));
                        });
            descendentTasksForLevel1.add(getLevel1DescendentTask1);

            //Add second task for level-1
            CompletableFuture<Void> getDescendentTask2 =
                    getDescendentTaskWithDelay(context, view, context.getRequestId(), context.getFirstLevelDescendentTaskDelay())
                        .thenCompose(rootStr -> {
                            List<CompletableFuture<String>> descendentTasksForLevel2 = new ArrayList<>();

                            //Add first task for level-2 second task
                            descendentTasksForLevel2.add(
                                getDescendentTaskWithDelay(context, view, rootStr, context.getSecondLevelDescendentTaskDelay())
                            );

                            //Add second task for level-2 second task
                            descendentTasksForLevel2.add(
                                getDescendentTaskWithDelay(context, view, rootStr, context.getSecondLevelDescendentTaskDelay())
                            );

                            return CompletableFuture.allOf(descendentTasksForLevel2.toArray(new CompletableFuture[]{}));
                        });
            descendentTasksForLevel1.add(getDescendentTask2);

            //Add third task for level 1
            CompletableFuture<Void> getDescendentTask3 =
                    getDescendentTaskWithDelay(context, view, context.getRequestId(), context.getFirstLevelDescendentTaskDelay())
                        .thenCompose(rootStr -> {
                            List<CompletableFuture<String>> descendentTasksForLevel2 = new ArrayList<>();

                            //Add first task for level-2 third task
                            descendentTasksForLevel2.add(
                                getDescendentTaskWithDelay(context, view, rootStr, context.getSecondLevelDescendentTaskDelay())
                            );

                            //Add second task for level-2 third task
                            descendentTasksForLevel2.add(
                                getDescendentTaskWithDelay(context, view, rootStr, context.getSecondLevelDescendentTaskDelay())
                            );

                            return CompletableFuture.allOf(descendentTasksForLevel2.toArray(new CompletableFuture[]{}));
                        });
            descendentTasksForLevel1.add(getDescendentTask3);

                return CompletableFuture.allOf(descendentTasksForLevel1.toArray(new CompletableFuture[]{}));
            })
            .thenApply(none -> view);
    }

    private CompletableFuture<String> getDescendentTaskWithDelay(TestServiceContext context, TestView rootView, Object rootId, long delayMs) {
        return createDescendentTaskWithDelay(context, rootId, delayMs)
                .thenApply(str -> {
                    checkRequestIsExist(context);
                    rootView.getContents().add(str);
                    LOGGER.info("Current id={}", str);
                    return str;
                });
    }

    private CompletableFuture<String> createDescendentTaskWithDelay(TestServiceContext context, Object rootId, long delayMs) {
        return CompletableFuture.supplyAsync(
                () -> {
                    checkRequestIsExist(context);
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException e) {
                        LOGGER.error("Thread was interrupted", e);
                    }
                    return getCreatedDescendentTaskId(rootId);
                }, executor);
    }

    private void checkRequestIsExist(TestServiceContext context) {
        Object requestStatus = executorContext.getDestroyedRequestStatuses().get(context.getRequestId());
        if (requestStatus != null) {
            LOGGER.error("Task was rejected for execute for closing request {}", context.getRequestId());
            throw new RuntimeException(
                new InterruptedException(String.format("Task was rejected for execute for closing request %s", context.getRequestId()))
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