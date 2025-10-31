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

    public InferenceResponse procesarSolicitud(InferenceRequest request) {
        InferenceProvider proveedor = registry.seleccionarProveedor()
                .orElseThrow(() -> new IllegalStateException("No hay proveedores registrados"));

        String contenido = "[Mock] " + request.prompt();
        if (request.metadata() != null && !request.metadata().isBlank()) {
            contenido += " (metadata: " + request.metadata() + ")";
        }
        return new InferenceResponse(proveedor.id(), contenido);
    }
}
