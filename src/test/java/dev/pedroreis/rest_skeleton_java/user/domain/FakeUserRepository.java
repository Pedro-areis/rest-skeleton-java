package dev.pedroreis.rest_skeleton_java.user.domain;

import dev.pedroreis.rest_skeleton_java.user.ports.outbound.UserRepositoryPort;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório falso, em memória, para testar o domínio sem Spring e sem banco.
 * <p>
 * Guarda e devolve CÓPIAS dos usuários, como o banco faz (a entidade vira um objeto novo
 * a cada leitura). Assim, se o serviço alterar um usuário e esquecer de chamar {@code save},
 * o teste percebe, porque a alteração não aparece na leitura seguinte.
 * <p>
 * De propósito, NÃO simula a constraint UNIQUE do e-mail: essa barreira é do banco
 * (testada no PostgresUserAdapterTest). Aqui queremos provar que o serviço confere sozinho.
 */
public class FakeUserRepository implements UserRepositoryPort {

    private final Map<UUID, User> storage = new HashMap<>();

    @Override
    public User save(User user) {
        storage.put(user.getId(), copyOf(user));
        return copyOf(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(storage.get(id)).map(FakeUserRepository::copyOf);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return storage.values().stream()
                .filter(user -> Objects.equals(user.getEmail(), email))
                .findFirst()
                .map(FakeUserRepository::copyOf);
    }

    @Override
    public void deleteById(UUID id) {
        storage.remove(id);
    }

    /** Quantos usuários estão guardados (útil para provar que nada foi salvo). */
    public int count() {
        return storage.size();
    }

    private static User copyOf(User user) {
        // Usa o construtor de reconstituição: não valida, só copia.
        return new User(user.getId(), user.getName(), user.getEmail(), user.getPassword(),
                user.getBirthDate(), user.getCreatedAt());
    }
}
