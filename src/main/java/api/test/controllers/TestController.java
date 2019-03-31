package api.test.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import api.test.models.BaseResult;
import api.test.models.TaskTreeTraversalView;
import api.test.services.TestService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.CompletableFuture;

@RestController
@Async
@RequestMapping("api/v1/test")
public class TestController {
    private final TestService service;

    public TestController(TestService service) {
        this.service = service;
    }

    @CrossOrigin(origins = "*")
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public CompletableFuture<ResponseEntity<BaseResult<TaskTreeTraversalView>>> getView(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return service.getView(request, response)
                .thenApply(BaseResult::new)
                .thenApply(ResponseEntity::ok);
    }
}
