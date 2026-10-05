package com.sri.apisentinel.dependencyservice.api;

import com.sri.apisentinel.commonmodel.ApiConsumer;
import com.sri.apisentinel.dependencyservice.service.DependencyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/consumers")
public class DependencyController {

    private final DependencyService service;

    public DependencyController(DependencyService service) {
        this.service = service;
    }

    @PostMapping
    public ApiConsumer register(@Valid @RequestBody RegisterRequest req) {
        return service.register(req.serviceName(), req.apiName(), req.apiVersion(),
                req.contactOwner(), req.environment());
    }

    @GetMapping("/by-api/{apiName}")
    public List<ApiConsumer> byApi(@PathVariable String apiName) {
        return service.consumersFor(apiName);
    }

    @GetMapping("/by-service/{serviceName}")
    public List<ApiConsumer> byService(@PathVariable String serviceName) {
        return service.apisFor(serviceName);
    }

    @GetMapping("/fan-out/{apiName}")
    public Map<String, Object> fanOut(@PathVariable String apiName) {
        return Map.of("apiName", apiName, "fanOut", service.fanOut(apiName));
    }

    public record RegisterRequest(
            @NotBlank String serviceName,
            @NotBlank String apiName,
            @NotBlank String apiVersion,
            String contactOwner,
            String environment
    ) {}
}