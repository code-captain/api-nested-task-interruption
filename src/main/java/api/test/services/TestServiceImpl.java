package api.test.services;

import api.test.models.TestView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static api.test.utils.RequestAttributesUtils.getRequest;

public class TestServiceImpl implements TestService {
    private final Logger LOGGER = LogManager.getLogger(getClass().getName());
    private final Executor executor;

    public TestServiceImpl(Executor executor) {
        this.executor = executor;
    }

    public CompletableFuture<TestView> getView(TestServiceContext context) {
        TestView view = new TestView();
        return CompletableFuture.supplyAsync(() -> {
            HttpServletRequest request = getRequest(RequestContextHolder.getRequestAttributes());
            Object uuid = request.getAttribute("uuid");
            view.setId(String.valueOf(uuid));
            return uuid;
        }, executor).thenCompose(uuid -> {
            List<CompletableFuture<Void>> descendentTasksForLevel1 = new ArrayList<>();

            //Add first task for level-1
            CompletableFuture<Void> getLevel1DescendentTask1 =
                    getDescendentTaskWithDelay(view, uuid, context.getFirstLevelDescendentTaskDelay())
                        .thenCompose(rootStr -> {
                            List<CompletableFuture<String>> descendentTasksForLevel2 = new ArrayList<>();

                            //Add first task for level-2 first task
                            descendentTasksForLevel2.add(
                                getDescendentTaskWithDelay(view, rootStr, context.getSecondLevelDescendentTaskDelay())
                                    .thenCompose(rootStr1 ->
                                            getDescendentTaskWithDelay(view, rootStr1, context.getThirdLevelDescendentTaskDelay())
                                    )
                            );

                            //Add second task for level-2 first task
                            descendentTasksForLevel2.add(
                                getDescendentTaskWithDelay(view, rootStr, context.getSecondLevelDescendentTaskDelay())
                            );

                            return CompletableFuture.allOf(descendentTasksForLevel2.toArray(new CompletableFuture[]{}));
                        });
            descendentTasksForLevel1.add(getLevel1DescendentTask1);

            //Add second task for level-1
            CompletableFuture<Void> getDescendentTask2 =
                    getDescendentTaskWithDelay(view, uuid, context.getFirstLevelDescendentTaskDelay())
                        .thenCompose(rootStr -> {
                            List<CompletableFuture<String>> descendentTasksForLevel2 = new ArrayList<>();

                            //Add first task for level-2 second task
                            descendentTasksForLevel2.add(
                                getDescendentTaskWithDelay(view, rootStr, context.getSecondLevelDescendentTaskDelay())
                            );

                            //Add second task for level-2 second task
                            descendentTasksForLevel2.add(
                                getDescendentTaskWithDelay(view, rootStr, context.getSecondLevelDescendentTaskDelay())
                            );

                            return CompletableFuture.allOf(descendentTasksForLevel2.toArray(new CompletableFuture[]{}));
                        });
            descendentTasksForLevel1.add(getDescendentTask2);

            //Add third task for level 1
            CompletableFuture<Void> getDescendentTask3 =
                    getDescendentTaskWithDelay(view, uuid, context.getFirstLevelDescendentTaskDelay())
                        .thenCompose(rootStr -> {
                            List<CompletableFuture<String>> descendentTasksForLevel2 = new ArrayList<>();

                            //Add first task for level-2 third task
                            descendentTasksForLevel2.add(
                                getDescendentTaskWithDelay(view, rootStr, context.getSecondLevelDescendentTaskDelay())
                            );

                            //Add second task for level-2 third task
                            descendentTasksForLevel2.add(
                                getDescendentTaskWithDelay(view, rootStr, context.getSecondLevelDescendentTaskDelay())
                            );

                            return CompletableFuture.allOf(descendentTasksForLevel2.toArray(new CompletableFuture[]{}));
                        });
            descendentTasksForLevel1.add(getDescendentTask3);

                return CompletableFuture.allOf(descendentTasksForLevel1.toArray(new CompletableFuture[]{}));
            })
            .thenApply(none -> view);
    }

    private CompletableFuture<String> getDescendentTaskWithDelay(TestView rootView, Object rootId, long delayMs) {
        return createDescendentTaskWithDelay(rootId, delayMs)
                .thenApply(str -> {
                    rootView.getContents().add(str);
                    LOGGER.info("Current id={}", str);
                    return str;
                });
    }

    private CompletableFuture<String> createDescendentTaskWithDelay(Object rootId, long delayMs) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException e) {
                        LOGGER.error("Thread was interrupted", e);
                    }
                    return getCreatedDescendentTaskId(rootId);
                }, executor);
    }

    private String getCreatedDescendentTaskId(Object rootId) {
        int descendId = new Random().nextInt(100);
        return createDescendentTaskId(rootId, descendId);
    }

    private String createDescendentTaskId(Object rootId, Object objectId) {
        return String.format("%s.%s", rootId, objectId);
    }
}