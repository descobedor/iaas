package com.iaas.gateway.api;

import jakarta.validation.constraints.NotBlank;

public record RegisterProviderRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "El endpoint es obligatorio") String endpoint
) {
}
