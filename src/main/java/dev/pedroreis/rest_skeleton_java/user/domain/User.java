package dev.pedroreis.rest_skeleton_java.user.domain;

import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidBirthDateException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidEmailException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    private UUID id;
    private String name;
    private String email;
    /** Hash da senha (nunca a senha em texto puro). Quem gera o hash é o UserService, via PasswordHasherPort. */
    private String passwordHash;
    private LocalDate birthDate;
    private LocalDateTime createdAt;

    /** Reconstituição a partir da persistência: não valida, o dado já foi validado na criação. */
    public User (UUID id, String name, String email, String passwordHash, LocalDate birthDate, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.birthDate = birthDate;
        this.createdAt = createdAt;
    }

    /** Criação de um usuário novo: aplica as regras de negócio. */
    public User (String name, String email, String passwordHash, LocalDate birthDate) {
        validateEmail(email);
        validateBirthDate(birthDate);

        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.birthDate = birthDate;
        this.createdAt = LocalDateTime.now();
    }

    private void validateEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new InvalidEmailException();
        }
    }

    /** A data de nascimento é obrigatória e não pode ser futura (hoje é permitido). */
    private void validateBirthDate(LocalDate birthDate) {
        if (birthDate == null || birthDate.isAfter(LocalDate.now())) {
            throw new InvalidBirthDateException();
        }
    }

    public void updateProfile (String newName, String newEmail, LocalDate newBirthDate) {
        if (newName != null && !newName.trim().isEmpty()) {
            this.name = newName;
        }
        if (newEmail != null && !newEmail.trim().isEmpty()) {
            validateEmail(newEmail);
            this.email = newEmail;
        }
        if (newBirthDate != null) {
            validateBirthDate(newBirthDate);
            this.birthDate = newBirthDate;
        }
    }

    /** Recebe um hash já pronto. Nulo ou em branco significa "não alterar". */
    public void updatePasswordHash (String newPasswordHash) {
        if (newPasswordHash != null && !newPasswordHash.trim().isEmpty()) {
            this.passwordHash = newPasswordHash;
        }
    }

    public UUID getId () { return id; }
    public String getName () { return name; }
    public String getEmail () { return email; }
    public String getPasswordHash () { return passwordHash; }
    public LocalDate getBirthDate () { return birthDate; }
    public LocalDateTime getCreatedAt () { return createdAt; }
}
