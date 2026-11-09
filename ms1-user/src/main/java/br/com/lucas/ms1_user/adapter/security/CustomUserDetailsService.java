package br.com.lucas.ms1_user.adapter.security;

import br.com.lucas.ms1_user.application.port.out.UserRepositoryPort;
import br.com.lucas.ms1_user.domain.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepositoryPort repository;

    public CustomUserDetailsService(UserRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        User user = repository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado"
                ));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_USER")
                .build();
    }
}

