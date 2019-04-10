package api.nested.task.services;

import api.nested.task.models.NestedTaskView;
import api.nested.task.configs.listeners.ApplicationRequestContextListenerContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

public class NestedTaskInterruptionService implements NestedTaskService {
    private final Logger logger = LogManager.getLogger(getClass().getName());
    private final Executor executor;
    private final ApplicationRequestContextListenerContainer requestContextListenerContainer;

    public NestedTaskInterruptionService(Executor executor, ApplicationRequestContextListenerContainer requestContextListenerContainer) {
        this.executor = executor;
        this.requestContextListenerContainer = requestContextListenerContainer;
    }

    public CompletableFuture<NestedTaskView> getView(NestedTaskServiceContext context) {
        NestedTaskView view = new NestedTaskView();
        return createTaskId(context, view).thenCompose(none -> {
            List<CompletableFuture<Void>> descendentTasksForLevel1 = new ArrayList<>();

            //Add first task for level-1
            CompletableFuture<Void> getLevel1DescendentTask1 =
                    getDescendentTaskIdWithDelay(context, view, context.getRequestId(), context.getFirstLevelDescendentTaskDelay())
                        .thenCompose(rootStr -> {
                            List<CompletableFuture<String>> descendentTasksForLevel2 = new ArrayList<>();

                            //Add first task for level-2 first task
                            descendentTasksForLevel2.add(
                                getDescendentTaskIdWithDelay(context, view, rootStr, context.getSecondLevelDescendentTaskDelay())
                                    .thenCompose(rootStr1 ->
                                            getDescendentTaskIdWithDelay(context, view, rootStr1, context.getThirdLevelDescendentTaskDelay())
                                    )
                            );

                            //Add second task for level-2 first task
                            descendentTasksForLevel2.add(
                                getDescendentTaskIdWithDelay(context, view, rootStr, context.getSecondLevelDescendentTaskDelay())
                            );

                            return CompletableFuture.allOf(descendentTasksForLevel2.toArray(new CompletableFuture[]{}));
                        });
            descendentTasksForLevel1.add(getLevel1DescendentTask1);

            //Add second task for level-1
            CompletableFuture<Void> getDescendentTask2 =
                    getDescendentTaskIdWithDelay(context, view, context.getRequestId(), context.getFirstLevelDescendentTaskDelay())
                        .thenCompose(rootStr -> {
                            List<CompletableFuture<String>> descendentTasksForLevel2 = new ArrayList<>();

                            //Add first task for level-2 second task
                            descendentTasksForLevel2.add(
                                getDescendentTaskIdWithDelay(context, view, rootStr, context.getSecondLevelDescendentTaskDelay())
                            );

                            //Add second task for level-2 second task
                            descendentTasksForLevel2.add(
                                getDescendentTaskIdWithDelay(context, view, rootStr, context.getSecondLevelDescendentTaskDelay())
                            );

                            return CompletableFuture.allOf(descendentTasksForLevel2.toArray(new CompletableFuture[]{}));
                        });
            descendentTasksForLevel1.add(getDescendentTask2);

            //Add third task for level 1
            CompletableFuture<Void> getDescendentTask3 =
                    getDescendentTaskIdWithDelay(context, view, context.getRequestId(), context.getFirstLevelDescendentTaskDelay())
                        .thenCompose(rootStr -> {
                            List<CompletableFuture<String>> descendentTasksForLevel2 = new ArrayList<>();

                            //Add first task for level-2 third task
                            descendentTasksForLevel2.add(
                                getDescendentTaskIdWithDelay(context, view, rootStr, context.getSecondLevelDescendentTaskDelay())
                            );

                            //Add second task for level-2 third task
                            descendentTasksForLevel2.add(
                                getDescendentTaskIdWithDelay(context, view, rootStr, context.getSecondLevelDescendentTaskDelay())
                            );

                            return CompletableFuture.allOf(descendentTasksForLevel2.toArray(new CompletableFuture[]{}));
                        });
            descendentTasksForLevel1.add(getDescendentTask3);

                return CompletableFuture.allOf(descendentTasksForLevel1.toArray(new CompletableFuture[]{}));
            })
            .thenApply(none -> view);
    }

    private CompletableFuture<String> getDescendentTaskIdWithDelay(NestedTaskServiceContext context, NestedTaskView rootView, Object rootId, long delayMs) {
        return createDescendentTaskIdWithDelay(context, rootId, delayMs)
                .thenApply(str -> {
                    rootView.getDescendantTaskIds().add(str);
                    logger.info("Current added rootTaskId to descendantTaskIds {}", str);
                    return str;
                });
    }

    private CompletableFuture<String> createTaskId(NestedTaskServiceContext context, NestedTaskView rootView) {
        return CompletableFuture.supplyAsync(() -> {
            long threadId = Thread.currentThread().getId();
            logger.info("Start task for {}, threadId {}", context.getRequestId(), threadId);
            //checkRequestIsExist(context);
            rootView.setRootTaskId(String.valueOf(context.getRequestId()));
            logger.info("Current added rootTaskId {}", rootView.getRootTaskId());
            return rootView.getRootTaskId();
        }, executor);
    }

    private CompletableFuture<String> createDescendentTaskIdWithDelay(NestedTaskServiceContext context, Object rootId, long delayMs) {
        return CompletableFuture.supplyAsync(() -> {
            long threadId = Thread.currentThread().getId();
            logger.info("Start task for {}, threadId {}", context.getRequestId(), threadId);
            //checkRequestIsExist(context);
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                logger.error("Thread was interrupted", e);
            }
            return getCreatedDescendentTaskId(rootId);
        }, executor);
    }

    private void checkRequestIsExist(NestedTaskServiceContext context) {
        Object requestStatus = requestContextListenerContainer.getDestroyedRequestErrorCodes().get(context.getRequestId());
        if (requestStatus != null) {
            logger.error("Task was rejected for execute for closing request {}", context.getRequestId());
            throw new CompletionException(
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