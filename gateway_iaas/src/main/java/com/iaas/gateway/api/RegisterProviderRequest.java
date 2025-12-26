package com.iaas.gateway.api;

import jakarta.validation.constraints.NotBlank;

public record RegisterProviderRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Endpoint is required") String endpoint
) {
}
