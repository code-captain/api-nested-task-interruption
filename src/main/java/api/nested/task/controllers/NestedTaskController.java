package api.nested.task.controllers;

import api.nested.task.models.BaseResult;
import api.nested.task.models.NestedTaskView;
import api.nested.task.services.NestedTaskService;
import api.nested.task.services.NestedTaskServiceContext;
import api.nested.task.configs.listeners.ApplicationRequestContextListenerContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("api/v1/test")
public class NestedTaskController {
    private final NestedTaskService service;
    private final Logger logger = LogManager.getLogger(getClass().getName());

    public NestedTaskController(NestedTaskService service) {
        this.service = service;
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public CompletableFuture<ResponseEntity<BaseResult<NestedTaskView>>> getView(HttpServletRequest request) {
        Object requestUuid = request.getAttribute(ApplicationRequestContextListenerContainer.REQUEST_ATTRIBUTES_UUID);
        logger.info("--------Start handling request rootTaskId {}", requestUuid);

        NestedTaskServiceContext context = new NestedTaskServiceContext();
        context.setRequestId(requestUuid);
        context.setFirstLevelDescendentTaskDelay(
            new Random().nextInt((850 - 250) + 1) + 250
        );
        context.setSecondLevelDescendentTaskDelay(
            new Random().nextInt((450 - 100) + 1) + 100
        );
        context.setThirdLevelDescendentTaskDelay(
            new Random().nextInt((650 - 185) + 1) + 185
        );

        return service.getView(context)
                .thenApply(BaseResult::new)
                .thenApply(body -> {
                    logger.info("--------End handling request requestId {}", requestUuid);
                    return ResponseEntity.ok(body);
                });
    }
}
