package api.test.services;

import api.test.models.TestView;

import java.util.concurrent.CompletableFuture;

public interface TestService {
    CompletableFuture<TestView> getView(TestServiceContext context);
}
