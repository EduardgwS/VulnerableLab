package br.unipar.frameworks.dto;

import br.unipar.frameworks.model.User;

public record UserResponseDTO(Long id, String name) {

    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(user.getId(), user.getName());
    }
}   