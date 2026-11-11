package br.com.lucas.ms2_transaction.adapter.security;

import br.com.lucas.ms2_transaction.adapter.client.Ms1AuthenticationClient;
import br.com.lucas.ms2_transaction.adapter.client.dto.Ms1UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.client.ResourceAccessException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Ms1AuthenticationProviderTest {

    @Mock
    private Ms1AuthenticationClient ms1AuthenticationClient;

    private Ms1AuthenticationProvider authenticationProvider;

    @BeforeEach
    void setUp() {
        authenticationProvider = new Ms1AuthenticationProvider(
                ms1AuthenticationClient
        );
    }

    @Test
    void deveAutenticarUsuarioValidadoPeloMs1() {
        String email = "user2@gmail.com";
        String password = "senha-correta";

        Ms1UserResponse user = new Ms1UserResponse(
                12L,
                "Lucas2",
                email
        );

        when(ms1AuthenticationClient.authenticate(email, password))
                .thenReturn(Optional.of(user));

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        password
                );

        Authentication result =
                authenticationProvider.authenticate(authentication);

        assertTrue(result.isAuthenticated());
        assertEquals("12", result.getName());
        assertNull(result.getCredentials());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_USER")
                ));
    }

    @Test
    void deveRejeitarCredenciaisInvalidas() {
        String email = "user2@gmail.com";
        String password = "senha-incorreta";

        when(ms1AuthenticationClient.authenticate(email, password))
                .thenReturn(Optional.empty());

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        password
                );

        assertThrows(
                BadCredentialsException.class,
                () -> authenticationProvider.authenticate(authentication)
        );
    }

    @Test
    void deveInformarQuandoMs1EstiverIndisponivel() {
        String email = "user2@gmail.com";
        String password = "senha-correta";

        when(ms1AuthenticationClient.authenticate(email, password))
                .thenThrow(new ResourceAccessException(
                        "MS1 indisponível"
                ));

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        password
                );

        assertThrows(
                AuthenticationServiceException.class,
                () -> authenticationProvider.authenticate(authentication)
        );
    }
}