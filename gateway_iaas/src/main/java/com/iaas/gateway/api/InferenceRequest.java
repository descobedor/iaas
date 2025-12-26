package com.iaas.gateway.api;

import jakarta.validation.constraints.NotBlank;

public record InferenceRequest(
        @NotBlank(message = "Prompt is required") String prompt,
        String metadata
) {
}
