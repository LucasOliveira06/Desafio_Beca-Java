package br.com.lucas.ms2_transaction.adapter.security;

import br.com.lucas.ms2_transaction.adapter.client.Ms1AuthenticationClient;
import br.com.lucas.ms2_transaction.adapter.client.dto.Ms1UserResponse;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class Ms1AuthenticationProvider
        implements AuthenticationProvider {

    private final Ms1AuthenticationClient ms1AuthenticationClient;

    public Ms1AuthenticationProvider(
            Ms1AuthenticationClient ms1AuthenticationClient
    ) {
        this.ms1AuthenticationClient = ms1AuthenticationClient;
    }

    @Override
    public Authentication authenticate(
            Authentication authentication
    ) {
        String email = authentication.getName();
        String password = String.valueOf(
                authentication.getCredentials()
        );

        try {
            Ms1UserResponse user = ms1AuthenticationClient
                    .authenticate(email, password)
                    .orElseThrow(() -> new BadCredentialsException(
                            "E-mail ou senha inválidos"
                    ));

            return new UsernamePasswordAuthenticationToken(
                    user.id().toString(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );

        } catch (BadCredentialsException exception) {
            throw exception;

        } catch (RestClientException exception) {
            throw new AuthenticationServiceException(
                    "Não foi possível validar as credenciais no MS1",
                    exception
            );
        }
    }

    @Override
    public boolean supports(Class<?> authenticationType) {
        return UsernamePasswordAuthenticationToken.class
                .isAssignableFrom(authenticationType);
    }
}