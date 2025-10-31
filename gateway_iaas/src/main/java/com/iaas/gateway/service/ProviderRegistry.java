package com.iaas.gateway.service;

import com.iaas.gateway.model.InferenceProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ProviderRegistry {

    private final List<InferenceProvider> proveedores = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger indice = new AtomicInteger();

    public InferenceProvider registrarProveedor(String nombre, String endpoint) {
        InferenceProvider proveedor = new InferenceProvider(UUID.randomUUID().toString(), nombre, endpoint);
        proveedores.add(proveedor);
        return proveedor;
    }

    public List<InferenceProvider> listarProveedores() {
        synchronized (proveedores) {
            return List.copyOf(proveedores);
        }
    }

    public Optional<InferenceProvider> seleccionarProveedor() {
        synchronized (proveedores) {
            if (proveedores.isEmpty()) {
                return Optional.empty();
            }
            int posicion = Math.floorMod(indice.getAndIncrement(), proveedores.size());
            return Optional.of(proveedores.get(posicion));
        }
    }
}
