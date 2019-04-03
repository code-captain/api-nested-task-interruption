package api.test.controllers;

import api.test.models.BaseResult;
import api.test.services.TestServiceContext;
import api.test.models.TestView;
import api.test.services.TestService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
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
        Object requestUuid = request.getAttribute("uuid");
        LOGGER.info("--------Start handle request uuid {}", requestUuid);
        TestServiceContext context = new TestServiceContext(
            10,
            25,
            35
        );
        return service.getView(context)
                .thenApply(BaseResult::new)
                .thenApply(body -> {
                    LOGGER.info("--------End successfully handling request uuid {}", requestUuid);
                    return ResponseEntity.ok(body);
                });
    }

    @GetMapping(
            path = "/fail",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public CompletableFuture<ResponseEntity<BaseResult<TestView>>> getFailureView(HttpServletRequest request) {
        Object requestUuid = request.getAttribute("uuid");
        LOGGER.info("--------Start handling request uuid {}", requestUuid);
        TestServiceContext context = new TestServiceContext(
                1000,
                2000,
                3000
        );
        return service.getView(context)
                .thenApply(BaseResult::new)
                .thenApply(body -> {
                    LOGGER.info("--------End successfully handling request uuid {}", requestUuid);
                    return ResponseEntity.ok(body);
                });
    }
}
