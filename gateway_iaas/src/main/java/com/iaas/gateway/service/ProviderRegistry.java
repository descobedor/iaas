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

    private final List<InferenceProvider> providers = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger index = new AtomicInteger();

    public InferenceProvider registerProvider(String name, String endpoint) {
        InferenceProvider provider = new InferenceProvider(UUID.randomUUID().toString(), name, endpoint);
        providers.add(provider);
        return provider;
    }

    public List<InferenceProvider> listProviders() {
        synchronized (providers) {
            return List.copyOf(providers);
        }
    }

    public Optional<InferenceProvider> selectProvider() {
        synchronized (providers) {
            if (providers.isEmpty()) {
                return Optional.empty();
            }
            int position = Math.floorMod(index.getAndIncrement(), providers.size());
            return Optional.of(providers.get(position));
        }
    }
}
