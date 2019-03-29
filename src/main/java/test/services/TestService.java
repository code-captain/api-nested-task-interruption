package test.services;

import test.models.TestView;

import java.util.concurrent.CompletableFuture;

public interface TestService {
    CompletableFuture<TestView> getView();
}
