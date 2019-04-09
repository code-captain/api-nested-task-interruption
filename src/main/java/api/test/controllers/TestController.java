package api.test.controllers;

import api.test.configs.listeners.ApplicationRequestContextListenerContainer;
import api.test.models.BaseResult;
import api.test.models.TestView;
import api.test.services.TestService;
import api.test.services.TestServiceContext;
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
public class TestController {
    private final TestService service;
    private final Logger LOGGER = LogManager.getLogger(getClass().getName());

    public TestController(TestService service) {
        this.service = service;
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public CompletableFuture<ResponseEntity<BaseResult<TestView>>> getView(HttpServletRequest request) {
        Object requestUuid = request.getAttribute(ApplicationRequestContextListenerContainer.REQUEST_ATTRIBUTES_UUID);
        LOGGER.info("--------Start handling request id {}", requestUuid);

        TestServiceContext context = new TestServiceContext();
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
                    LOGGER.info("--------End handling request requestId {}", requestUuid);
                    return ResponseEntity.ok(body);
                });
    }
}
