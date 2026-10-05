package dev.pedroreis.rest_skeleton_java.user.adapters.outbound;

import dev.pedroreis.rest_skeleton_java.user.domain.User;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.EmailAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgresUserAdapterTest {

    @Mock
    private SpringDataUserRepository jpaRepository;

    @InjectMocks
    private PostgresUserAdapter adapter;

    private final User user = new User("Pedro", "pedro@email.com", "123", LocalDate.of(2000, 1, 15));

    @Test
    void shouldThrowEmailAlreadyExistsOnUniqueViolation() {
        var violation = new DataIntegrityViolationException(
                "duplicado", new SQLException("unique_violation", "23505"));
        when(jpaRepository.save(any(UserEntity.class))).thenThrow(violation);

        assertThrows(EmailAlreadyExistsException.class, () -> adapter.save(user));
    }

    @Test
    void shouldRethrowOtherIntegrityViolations() {
        var violation = new DataIntegrityViolationException(
                "nulo", new SQLException("not_null_violation", "23502"));
        when(jpaRepository.save(any(UserEntity.class))).thenThrow(violation);

        DataIntegrityViolationException thrown = assertThrows(
                DataIntegrityViolationException.class, () -> adapter.save(user));

        assertSame(violation, thrown);
    }
}
