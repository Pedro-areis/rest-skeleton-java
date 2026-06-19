package dev.pedroreis.rest_skeleton_java.user.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    private UUID id;
    private String name;
    private String email;
    private String password;
    private LocalDateTime createdAt;

    public User (UUID id, String name, String email, String password, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
    }

    private void validateEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
    }

    public User (String name, String email, String password) {
        validateEmail(email);

        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    public void updateProfile (String newName, String newEmail) {
        if (newName != null && !newName.trim().isEmpty()) {
            this.name = newName;
        }
        if (newEmail != null && !newEmail.trim().isEmpty()) {
            validateEmail(newEmail);
            this.email = newEmail;
        }
    }

    public void updatePassword (String newPassword) {
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            this.password = newPassword;
        }
    }

    public UUID getId () { return id; }
    public String getName () { return name; }
    public String getEmail () { return email; }
    public String getPassword () { return password; }
    public LocalDateTime getCreatedAt () { return createdAt; }
}
