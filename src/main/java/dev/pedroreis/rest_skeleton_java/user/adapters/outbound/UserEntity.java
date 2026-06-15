package dev.pedroreis.rest_skeleton_java.user.adapters.outbound;

import dev.pedroreis.rest_skeleton_java.user.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CurrentTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Email
    @Column(unique = true)
    private String email;

    private String password;

    @Column(name = "created_at")
    @CurrentTimestamp
    private LocalDateTime createdAt;

    public User toDomain () {
        return new User(id, name, email, password, createdAt);
    }

    public static UserEntity fromDomain (User user) {
        return new UserEntity(user.getId(), user.getName(), user.getEmail(), user.getPassword(), user.getCreatedAt());
    }
}
