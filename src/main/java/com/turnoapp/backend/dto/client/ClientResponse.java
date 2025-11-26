package com.turnoapp.backend.dto.client;

import com.turnoapp.backend.model.Client;
import com.turnoapp.backend.model.enums.Status;

public record ClientResponse(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String province,
        String city,
        Status status
) {
    public static ClientResponse fromEntity(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getUser().getId(),
                client.getUser().getFirstName(),
                client.getUser().getLastName(),
                client.getUser().getEmail(),
                client.getUser().getPhone(),
                client.getUser().getProvince(),
                client.getUser().getCity(),
                client.getUser().getStatus()
        );
    }
}

