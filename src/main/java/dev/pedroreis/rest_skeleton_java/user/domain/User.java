package dev.pedroreis.rest_skeleton_java.user.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    private UUID id;
    private String name;
    private String email;
    private String password;
    private LocalDateTime createdAt;

    public User() {
    }

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

    public User (String email, String password) {
        validateEmail(email);

        this.id = UUID.randomUUID();
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId () { return id; }
    public String getName () { return name; }
    public String getEmail () { return email; }
    public String getPassword () { return password; }
    public LocalDateTime getCreatedAt () { return createdAt; }

    public void setId (UUID id) { this.id = id; }
    public void setName (String name) { this.name = name; }
    public void setEmail (String email) { this.email = email; }
    public void setPassword (String password) { this.password = password; }
    public void setCreatedAt (LocalDateTime createdAt) { this.createdAt = createdAt; }
}
