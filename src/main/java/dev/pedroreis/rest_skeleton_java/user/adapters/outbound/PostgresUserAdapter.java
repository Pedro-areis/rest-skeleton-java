package dev.pedroreis.rest_skeleton_java.user.adapters.outbound;

import dev.pedroreis.rest_skeleton_java.user.domain.User;
import dev.pedroreis.rest_skeleton_java.user.ports.outbound.UserRepositoryPort;

import java.util.Optional;
import java.util.UUID;

public class PostgresUserAdapter implements UserRepositoryPort {
    private final SpringDataUserRepository jpaRepository;

    public PostgresUserAdapter(SpringDataUserRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        UserEntity entityToSave = UserEntity.fromDomain(user);

        UserEntity savedEntity = jpaRepository.save(entityToSave);

        return savedEntity.toDomain();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(UserEntity::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
