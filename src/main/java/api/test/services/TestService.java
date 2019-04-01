package api.test.services;

import api.test.models.TestView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.CompletableFuture;

public interface TestService {
    CompletableFuture<TestView> getView(HttpServletRequest request, HttpServletResponse response);
}
