package com.turnoapp.backend.service;

import com.turnoapp.backend.dto.client.ClientResponse;

public interface ClientService {
    ClientResponse getClientById(Long id);
    ClientResponse getClientByUserId(Long userId);
}

