package br.com.leandrocoelho.backend.integration.pluggy.dto;

import java.util.List;

public record PluggyResultDto<T>(List<T> results) {
}
