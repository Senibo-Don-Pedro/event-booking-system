package com.senibo.apigateway.filter;

import java.util.List;
import java.util.function.Predicate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RouteValidator {

    // List of URLs that are open to the public
    public static final List<String> openApiEndpoints = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/verify",
            "/api/events/published",
            "/api/events/search",
            "/eureka",
            "/swagger-ui",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/webjars");

    public Predicate<ServerHttpRequest> isSecured = request -> openApiEndpoints
            .stream()
            .noneMatch(uri -> request.getURI().getPath().contains(uri));
}