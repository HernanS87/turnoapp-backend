package com.turnoapp.backend.service.impl;

import com.turnoapp.backend.dto.client.ClientResponse;
import com.turnoapp.backend.exception.ResourceNotFoundException;
import com.turnoapp.backend.model.Client;
import com.turnoapp.backend.repository.ClientRepository;
import com.turnoapp.backend.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        return ClientResponse.fromEntity(client);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getClientByUserId(Long userId) {
        Client client = clientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with userId: " + userId));
        return ClientResponse.fromEntity(client);
    }
}

