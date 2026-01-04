package com.iaas.gateway.api;

import java.util.List;

public record MenuSection(int index, String name, List<MenuItem> items) {
}
