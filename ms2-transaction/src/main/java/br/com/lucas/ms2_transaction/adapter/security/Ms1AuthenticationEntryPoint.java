package br.com.lucas.ms2_transaction.adapter.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class Ms1AuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        if (exception instanceof AuthenticationServiceException) {
            writeResponse(
                    response,
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Não foi possível validar as credenciais no MS1"
            );
            return;
        }

        response.setHeader(
                HttpHeaders.WWW_AUTHENTICATE,
                "Basic realm=\"Realm\""
        );

        writeResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                "E-mail ou senha inválidos"
        );
    }

    private void writeResponse(
            HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
                "{\"status\":" + status.value()
                        + ",\"message\":\"" + message + "\"}"
        );
    }
}
