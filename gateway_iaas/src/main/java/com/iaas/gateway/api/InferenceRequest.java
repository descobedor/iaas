package com.iaas.gateway.api;

import jakarta.validation.constraints.NotBlank;

public record InferenceRequest(
        @NotBlank(message = "El prompt es obligatorio") String prompt,
        String metadata
) {
}
