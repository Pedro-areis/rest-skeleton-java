package dev.pedroreis.rest_skeleton_java.user.adapters.outbound;

import dev.pedroreis.rest_skeleton_java.user.domain.User;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.EmailAlreadyExistsException;
import dev.pedroreis.rest_skeleton_java.user.ports.outbound.UserRepositoryPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Component
public class PostgresUserAdapter implements UserRepositoryPort {
    /** SQLState do PostgreSQL para violação de unicidade (unique_violation). */
    private static final String UNIQUE_VIOLATION = "23505";

    private final SpringDataUserRepository jpaRepository;

    public PostgresUserAdapter(SpringDataUserRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        UserEntity entityToSave = UserEntity.fromDomain(user);

        try {
            UserEntity savedEntity = jpaRepository.save(entityToSave);
            return savedEntity.toDomain();
        } catch (DataIntegrityViolationException ex) {
            // Só a violação de unicidade vira "e-mail já cadastrado".
            // Outras violações (ex.: NOT NULL) seguem como erro inesperado.
            if (isUniqueViolation(ex)) {
                throw new EmailAlreadyExistsException();
            }
            throw ex;
        }
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

    private boolean isUniqueViolation(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLException sqlException
                    && UNIQUE_VIOLATION.equals(sqlException.getSQLState())) {
                return true;
            }
            Throwable next = current.getCause();
            current = (next == current) ? null : next;
        }
        return false;
    }
}
