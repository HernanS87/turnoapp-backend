package com.turnoapp.backend.dto.professional;

import java.util.List;

public record FilterOptionsResponse(
        List<String> professions,
        List<String> provinces,
        List<String> cities
) {}

