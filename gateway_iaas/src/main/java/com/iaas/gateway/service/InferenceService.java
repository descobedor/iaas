package com.iaas.gateway.service;

import com.iaas.gateway.api.InferenceRequest;
import com.iaas.gateway.api.InferenceResponse;
import com.iaas.gateway.model.InferenceProvider;
import org.springframework.stereotype.Service;

@Service
public class InferenceService {

    private final ProviderRegistry registry;

    public InferenceService(ProviderRegistry registry) {
        this.registry = registry;
    }

    public InferenceResponse processRequest(InferenceRequest request) {
        InferenceProvider provider = registry.selectProvider()
                .orElseThrow(() -> new IllegalStateException("No providers registered"));

        String content = "[Mock] " + request.prompt();
        if (request.metadata() != null && !request.metadata().isBlank()) {
            content += " (metadata: " + request.metadata() + ")";
        }
        return new InferenceResponse(provider.id(), content);
    }
}
