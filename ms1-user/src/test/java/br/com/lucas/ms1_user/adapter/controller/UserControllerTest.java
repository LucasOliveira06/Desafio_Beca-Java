package br.com.lucas.ms1_user.adapter.controller;

import br.com.lucas.ms1_user.adapter.controller.dto.UserRequestDTO;
import br.com.lucas.ms1_user.adapter.controller.dto.UserResponseDTO;
import br.com.lucas.ms1_user.application.port.in.UserUseCase;
import br.com.lucas.ms1_user.domain.model.User;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.net.URI;
import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserUseCase userUseCase;

    @InjectMocks
    private UserController controller;

    @Test
    void deveCriarUsuarioERetornar201() {

        UserRequestDTO request = new UserRequestDTO();
        request.setName("Ana Silva");
        request.setEmail("ana@exemplo.com");
        request.setPassword("SenhaAna123");

        User createdUser = new User(
                10L,
                "Ana Silva",
                "ana@exemplo.com",
                "hashSimulado"
        );

        when(userUseCase.create(any(User.class)))
                .thenReturn(createdUser);

        ResponseEntity<UserResponseDTO> response =
                controller.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        assertEquals(
                URI.create("/users/10"),
                response.getHeaders().getLocation()
        );

        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getId());
        assertEquals("Ana Silva", response.getBody().getName());
        assertEquals("ana@exemplo.com", response.getBody().getEmail());

        verify(userUseCase).create(any(User.class));
    }

    @Test
    void deveListarUsuariosSemExporSenha() {

        User user = new User(
                1L,
                "Ana Silva",
                "ana@exemplo.com",
                "hashSimulado"
        );

        when(userUseCase.findAll())
                .thenReturn(List.of(user));

        List<UserResponseDTO> response = controller.findAll();

        assertEquals(1, response.size());

        UserResponseDTO result = response.get(0);

        assertEquals(1L, result.getId());
        assertEquals("Ana Silva", result.getName());
        assertEquals("ana@exemplo.com", result.getEmail());

        verify(userUseCase).findAll();
    }

    @Test
    void deveBuscarUsuarioPorIdSemExporSenha() {

        Long id = 1L;

        User user = new User(
                id,
                "Ana Silva",
                "ana@exemplo.com",
                "hashSimulado"
        );

        when(userUseCase.findById(id))
                .thenReturn(user);

        UserResponseDTO response = controller.findById(id);

        assertEquals(1L, response.getId());
        assertEquals("Ana Silva", response.getName());
        assertEquals("ana@exemplo.com", response.getEmail());

        verify(userUseCase).findById(id);
    }

    @Test
    void deveAtualizarUsuarioSemExporSenha() {

        Long id = 1L;

        // Usuário autenticado
        Principal principal = () -> "ana@exemplo.com";

        // Conta pertencente ao usuário autenticado
        User existingUser = new User(
                id,
                "Ana Silva",
                "ana@exemplo.com",
                "hashAntigo"
        );

        UserRequestDTO request = new UserRequestDTO();
        request.setName("Ana Oliveira");
        request.setEmail("ana.oliveira@exemplo.com");
        request.setPassword("NovaSenha123");

        User updatedUser = new User(
                id,
                "Ana Oliveira",
                "ana.oliveira@exemplo.com",
                "hashAtualizado"
        );

        when(userUseCase.findById(id))
                .thenReturn(existingUser);

        when(userUseCase.update(eq(id), any(User.class)))
                .thenReturn(updatedUser);

        UserResponseDTO response =
                controller.update(id, request, principal);

        assertEquals(1L, response.getId());
        assertEquals("Ana Oliveira", response.getName());
        assertEquals(
                "ana.oliveira@exemplo.com",
                response.getEmail()
        );

        verify(userUseCase).findById(id);
        verify(userUseCase).update(eq(id), any(User.class));
    }

    @Test
    void deveExcluirUsuarioPorId() {

        Long id = 1L;

        // Usuário autenticado
        Principal principal = () -> "ana@exemplo.com";

        // Conta pertencente ao usuário autenticado
        User existingUser = new User(
                id,
                "Ana Silva",
                "ana@exemplo.com",
                "hashSimulado"
        );

        when(userUseCase.findById(id))
                .thenReturn(existingUser);

        controller.delete(id, principal);

        verify(userUseCase).findById(id);
        verify(userUseCase).delete(id);
    }

    @Test
    void deveRejeitarCsvMaiorQueUmMB() {

        MultipartFile file = mock(MultipartFile.class);

        when(file.getSize())
                .thenReturn(1024L * 1024 + 1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.importCsv(file)
        );

        assertEquals(
                "O arquivo CSV deve ter no máximo 1 MB",
                exception.getMessage()
        );

        verifyNoInteractions(userUseCase);
    }

    @Test
    void deveImpedirAtualizacaoDeOutroUsuario() {

        Long id = 2L;


        Principal principal = () -> "ana@exemplo.com";


        User pedro = new User(
                id,
                "Pedro Souza",
                "pedro@exemplo.com",
                "hashPedro"
        );

        UserRequestDTO request = new UserRequestDTO();
        request.setName("Nome Alterado");
        request.setEmail("alterado@exemplo.com");
        request.setPassword("SenhaNova123");

        when(userUseCase.findById(id))
                .thenReturn(pedro);

        assertThrows(
                AccessDeniedException.class,
                () -> controller.update(id, request, principal)
        );

        verify(userUseCase).findById(id);


        verify(userUseCase, never())
                .update(eq(id), any(User.class));
    }

    @Test
    void deveImpedirExclusaoDeOutroUsuario() {

        Long id = 2L;


        Principal principal = () -> "ana@exemplo.com";


        User pedro = new User(
                id,
                "Pedro Souza",
                "pedro@exemplo.com",
                "hashPedro"
        );

        when(userUseCase.findById(id))
                .thenReturn(pedro);

        assertThrows(
                AccessDeniedException.class,
                () -> controller.delete(id, principal)
        );

        verify(userUseCase).findById(id);


        verify(userUseCase, never()).delete(id);
    }


}
