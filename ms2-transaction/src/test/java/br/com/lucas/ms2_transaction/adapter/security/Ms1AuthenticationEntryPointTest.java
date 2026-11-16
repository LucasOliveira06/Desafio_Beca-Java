package br.com.lucas.ms2_transaction.adapter.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Ms1AuthenticationEntryPointTest {

    private Ms1AuthenticationEntryPoint authenticationEntryPoint;

    @BeforeEach
    void setUp() {
        authenticationEntryPoint = new Ms1AuthenticationEntryPoint();
    }

    @Test
    void deveRetornarServiceUnavailableQuandoMs1EstiverIndisponivel()
            throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        authenticationEntryPoint.commence(
                request,
                response,
                new AuthenticationServiceException("MS1 indisponível")
        );

        assertEquals(503, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertTrue(response.getContentAsString().contains(
                "Não foi possível validar as credenciais no MS1"
        ));
    }

    @Test
    void deveRetornarUnauthorizedQuandoCredenciaisForemInvalidas()
            throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        authenticationEntryPoint.commence(
                request,
                response,
                new BadCredentialsException("Credenciais inválidas")
        );

        assertEquals(401, response.getStatus());
        assertEquals(
                "Basic realm=\"Realm\"",
                response.getHeader(HttpHeaders.WWW_AUTHENTICATE)
        );
        assertTrue(response.getContentAsString().contains(
                "E-mail ou senha inválidos"
        ));
    }
}
