package com.iaas.gateway.api;

import java.util.List;

public record MenuStructuredResponse(List<MenuSection> sections, String rawText) {
}
