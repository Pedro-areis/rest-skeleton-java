package dev.pedroreis.rest_skeleton_java.user.adapters.inbound.dto;

import jakarta.validation.constraints.Email;

import java.time.LocalDate;

/**
 * Atualização parcial (PATCH): todos os campos são opcionais.
 * Campo ausente (null) significa "não alterar". As validações abaixo só valem
 * quando o campo vem preenchido.
 */
public record UpdateUserRequest(
        String name,

        @Email(message = "Formato de e-mail inválido")
        String email,

        String password,

        // A regra "não pode ser futura" mora no domínio (User).
        LocalDate birthDate
) {
}
