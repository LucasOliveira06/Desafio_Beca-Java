package br.com.lucas.ms1_user.adapter.persistence;

import br.com.lucas.ms1_user.domain.model.User;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.Optional;
import java.util.List;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPersistenceAdapterTest {

    @Mock
    private UserJpaRepository jpaRepository;

    @InjectMocks
    private UserPersistenceAdapter adapter;

    @Test
    void deveSalvarUsuarioComMapeamentoCorreto() {

        User user = new User(
                null,
                "Ana Silva",
                "ana@exemplo.com",
                "hashSimulado"
        );

        when(jpaRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    UserEntity entity = invocation.getArgument(0);
                    entity.setId(10L);
                    return entity;
                });

        User result = adapter.save(user);

        ArgumentCaptor<UserEntity> captor =
                ArgumentCaptor.forClass(UserEntity.class);

        verify(jpaRepository).save(captor.capture());

        UserEntity entity = captor.getValue();

        assertEquals("Ana Silva", entity.getName());
        assertEquals("ana@exemplo.com", entity.getEmail());
        assertEquals("hashSimulado", entity.getPassword());

        assertEquals(10L, result.getId());
        assertEquals("Ana Silva", result.getName());
        assertEquals("ana@exemplo.com", result.getEmail());
        assertEquals("hashSimulado", result.getPassword());
    }

    @Test
    void deveBuscarUsuarioPorEmail() {

        String email = "ana@exemplo.com";

        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setName("Ana Silva");
        entity.setEmail(email);
        entity.setPassword("hashSimulado");

        when(jpaRepository.findByEmail(email))
                .thenReturn(Optional.of(entity));

        User result = adapter.findByEmail(email)
                .orElseThrow();

        assertEquals(1L, result.getId());
        assertEquals("Ana Silva", result.getName());
        assertEquals(email, result.getEmail());
        assertEquals("hashSimulado", result.getPassword());

        verify(jpaRepository).findByEmail(email);
    }

    @Test
    void deveListarUsuarios() {

        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setName("Ana Silva");
        entity.setEmail("ana@exemplo.com");
        entity.setPassword("hashSimulado");

        when(jpaRepository.findAll())
                .thenReturn(List.of(entity));

        List<User> users = adapter.findAll();

        assertEquals(1, users.size());

        assertEquals(1L, users.get(0).getId());
        assertEquals("Ana Silva", users.get(0).getName());
        assertEquals("ana@exemplo.com", users.get(0).getEmail());
        assertEquals("hashSimulado", users.get(0).getPassword());

        verify(jpaRepository).findAll();
    }

    @Test
    void deveBuscarUsuarioPorId() {

        Long id = 1L;

        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setName("Ana Silva");
        entity.setEmail("ana@exemplo.com");
        entity.setPassword("hashSimulado");

        when(jpaRepository.findById(id))
                .thenReturn(Optional.of(entity));

        User result = adapter.findById(id)
                .orElseThrow();

        assertEquals(id, result.getId());
        assertEquals("Ana Silva", result.getName());
        assertEquals("ana@exemplo.com", result.getEmail());
        assertEquals("hashSimulado", result.getPassword());

        verify(jpaRepository).findById(id);
    }

    @Test
    void deveVerificarEmailExistente() {

        String email = "ana@exemplo.com";

        when(jpaRepository.existsByEmail(email))
                .thenReturn(true);

        boolean exists = adapter.existsByEmail(email);

        assertTrue(exists);

        verify(jpaRepository).existsByEmail(email);
    }

    @Test
    void deveExcluirUsuarioPorId() {

        Long id = 1L;

        adapter.deleteById(id);

        verify(jpaRepository).deleteById(id);
    }

}
