package com.iaas.gateway.api;

import com.iaas.gateway.model.InferenceProvider;
import com.iaas.gateway.service.InferenceService;
import com.iaas.gateway.service.ProviderRegistry;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/")
public class GatewayController {

    private final ProviderRegistry registry;
    private final InferenceService inferenceService;

    public GatewayController(ProviderRegistry registry, InferenceService inferenceService) {
        this.registry = registry;
        this.inferenceService = inferenceService;
    }

    @PostMapping("/providers")
    @ResponseStatus(HttpStatus.CREATED)
    public InferenceProvider registerProvider(@Valid @RequestBody RegisterProviderRequest request) {
        return registry.registerProvider(request.name(), request.endpoint());
    }

    @GetMapping("/providers")
    public List<InferenceProvider> listProviders() {
        return registry.listProviders();
    }

    @PostMapping("/inference")
    public InferenceResponse infer(@Valid @RequestBody InferenceRequest request) {
        return inferenceService.processRequest(request);
    }
}
