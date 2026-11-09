package br.com.lucas.ms1_user.application.service;

import br.com.lucas.ms1_user.application.port.out.CsvUserReaderPort;
import br.com.lucas.ms1_user.application.port.out.PasswordEncoderPort;
import br.com.lucas.ms1_user.application.port.out.UserRepositoryPort;
import br.com.lucas.ms1_user.application.validation.UserValidator;
import br.com.lucas.ms1_user.domain.model.User;
import br.com.lucas.ms1_user.domain.exception.EmailAlreadyExistsException;
import br.com.lucas.ms1_user.domain.exception.UserNotFoundException;
import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepositoryPort repository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Mock
    private CsvUserReaderPort csvUserReader;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private UserService userService;

    @Test
    void deveNormalizarEmailECodificarSenha() {

        User user = new User(
                null,
                "Lucas",
                " LUCAS@EXEMPLO.COM ",
                "Senha12345"
        );

        when(passwordEncoder.encode("Senha12345"))
                .thenReturn("hashSimulado");

        when(repository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.create(user);

        assertEquals("lucas@exemplo.com", result.getEmail());
        assertEquals("hashSimulado", result.getPassword());

        verify(repository).existsByEmail("lucas@exemplo.com");
        verify(passwordEncoder).encode("Senha12345");
        verify(repository).save(user);
    }

    @Test
    void deveRejeitarEmailDuplicado() {

        User user = new User(
                null,
                "Lucas",
                " LUCAS@EXEMPLO.COM ",
                "Senha12345"
        );

        when(repository.existsByEmail("lucas@exemplo.com"))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.create(user)
        );

        verify(repository).existsByEmail("lucas@exemplo.com");

        verify(repository, never()).save(any(User.class));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {

        Long id = 999L;

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.findById(id)
        );

        verify(repository).findById(id);
    }

    @Test
    void deveAtualizarUsuarioECodificarNovaSenha() {

        Long id = 1L;

        User existingUser = new User(
                id,
                "Lucas",
                "lucas@exemplo.com",
                "hashAntigo"
        );

        User updatedData = new User(
                id,
                "Lucas Oliveira",
                " NOVO@EXEMPLO.COM ",
                "NovaSenha123"
        );

        when(repository.findById(id))
                .thenReturn(Optional.of(existingUser));

        when(passwordEncoder.encode("NovaSenha123"))
                .thenReturn("novoHashSimulado");

        when(repository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.update(id, updatedData);

        assertEquals("Lucas Oliveira", result.getName());
        assertEquals("novo@exemplo.com", result.getEmail());
        assertEquals("novoHashSimulado", result.getPassword());

        verify(repository).findById(id);
        verify(repository).existsByEmail("novo@exemplo.com");
        verify(passwordEncoder).encode("NovaSenha123");
        verify(repository).save(existingUser);
    }

    @Test
    void deveImportarUsuariosDoCsv() {

        String content = """
            name,email,password
            Ana Silva,ana.csv@exemplo.com,SenhaAna123
            Pedro Souza,pedro.csv@exemplo.com,SenhaPedro123
            """;

        User ana = new User(
                null,
                "Ana Silva",
                "ana.csv@exemplo.com",
                "SenhaAna123"
        );

        User pedro = new User(
                null,
                "Pedro Souza",
                "pedro.csv@exemplo.com",
                "SenhaPedro123"
        );

        when(csvUserReader.parse(content))
                .thenReturn(List.of(ana, pedro));

        when(passwordEncoder.encode("SenhaAna123"))
                .thenReturn("hashAna");

        when(passwordEncoder.encode("SenhaPedro123"))
                .thenReturn("hashPedro");

        when(repository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<User> result = userService.importCsv(content);

        assertEquals(2, result.size());

        assertEquals("hashAna", result.get(0).getPassword());
        assertEquals("hashPedro", result.get(1).getPassword());

        verify(csvUserReader).parse(content);

        verify(userValidator).validate(ana);
        verify(userValidator).validate(pedro);

        verify(repository, times(2)).save(any(User.class));
    }

    @Test
    void deveRejeitarCsvComUsuarioInvalido() {

        String content = """
            name,email,password
            Ana Silva,ana@exemplo.com,SenhaAna123
            Pedro Souza,pedro@exemplo.com,123
            """;

        User ana = new User(
                null,
                "Ana Silva",
                "ana@exemplo.com",
                "SenhaAna123"
        );

        User pedro = new User(
                null,
                "Pedro Souza",
                "pedro@exemplo.com",
                "123"
        );

        when(csvUserReader.parse(content))
                .thenReturn(List.of(ana, pedro));

        doNothing().when(userValidator).validate(ana);

        doThrow(new IllegalArgumentException(
                "A senha deve ter no mínimo 8 caracteres"
        )).when(userValidator).validate(pedro);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.importCsv(content)
        );

        assertEquals(
                "A senha deve ter no mínimo 8 caracteres",
                exception.getMessage()
        );

        verify(userValidator).validate(ana);
        verify(userValidator).validate(pedro);

        verifyNoInteractions(repository, passwordEncoder);
    }

    @Test
    void deveRejeitarCsvComEmailDuplicado() {

        String content = """
            name,email,password
            Roberto Silva,roberto@exemplo.com,SenhaRoberto123
            Usuario Duplicado,teste.security@exemplo.com,Senha12345
            """;

        User roberto = new User(
                null,
                "Roberto Silva",
                "roberto@exemplo.com",
                "SenhaRoberto123"
        );

        User duplicado = new User(
                null,
                "Usuario Duplicado",
                "teste.security@exemplo.com",
                "Senha12345"
        );

        when(csvUserReader.parse(content))
                .thenReturn(List.of(roberto, duplicado));

        when(repository.existsByEmail(anyString()))
                .thenAnswer(invocation -> {
                    String email = invocation.getArgument(0);
                    return email.equals("teste.security@exemplo.com");
                });

        when(passwordEncoder.encode("SenhaRoberto123"))
                .thenReturn("hashRoberto");

        when(repository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.importCsv(content)
        );

        verify(userValidator).validate(roberto);
        verify(userValidator).validate(duplicado);

        verify(repository).existsByEmail("roberto@exemplo.com");
        verify(repository).existsByEmail("teste.security@exemplo.com");

        verify(repository, times(1)).save(any(User.class));

        verify(passwordEncoder).encode("SenhaRoberto123");
        verify(passwordEncoder, never()).encode("Senha12345");
    }

}

