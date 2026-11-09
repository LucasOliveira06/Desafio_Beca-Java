package br.com.lucas.ms1_user.adapter.controller;

import br.com.lucas.ms1_user.adapter.controller.dto.UserRequestDTO;
import br.com.lucas.ms1_user.adapter.controller.dto.UserResponseDTO;
import br.com.lucas.ms1_user.application.port.in.UserUseCase;
import br.com.lucas.ms1_user.domain.model.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(
        name = "Usuários",
        description = "API para gerenciamento e importação de usuários"
)
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)
public class UserController {

    private final UserUseCase userUseCase;

    public UserController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }


    @Operation(
            summary = "Cadastrar usuário",
            description = "Cadastra um novo usuário com nome, e-mail e senha"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuário cadastrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados informados são inválidos"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "E-mail já cadastrado"
            )
    })
    @PostMapping
    public ResponseEntity<UserResponseDTO> create(
            @Valid @RequestBody UserRequestDTO request) {

        User user = new User(
                null,
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );

        User createdUser = userUseCase.create(user);

        UserResponseDTO response = toResponseDTO(createdUser);

        URI location = URI.create("/users/" + createdUser.getId());

        return ResponseEntity.created(location).body(response);
    }


    @Operation(
            summary = "Listar usuários",
            description = "Retorna todos os usuários cadastrados, sem expor as senhas"
    )
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuários listados com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado"
            )
    })
    @GetMapping
    public List<UserResponseDTO> findAll() {

        return userUseCase.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }


    @Operation(
            summary = "Buscar usuário por ID",
            description = "Consulta os dados de um usuário pelo seu identificador"
    )
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário encontrado"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado"
            )
    })
    @GetMapping("/{id}")
    public UserResponseDTO findById(@PathVariable Long id) {

        User user = userUseCase.findById(id);

        return toResponseDTO(user);
    }


    @Operation(
            summary = "Atualizar usuário",
            description = "Atualiza o nome, e-mail e senha de um usuário existente"
    )
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário atualizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados informados são inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "E-mail já cadastrado"
            )
    })
    @PutMapping("/{id}")
    public UserResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO request) {

        User user = new User(
                id,
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );

        User updatedUser = userUseCase.update(id, user);

        return toResponseDTO(updatedUser);
    }


    @Operation(
            summary = "Excluir usuário",
            description = "Exclui um usuário cadastrado pelo ID"
    )
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuário excluído com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado"
            )
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        userUseCase.delete(id);
    }


    @Operation(
            summary = "Importar usuários por CSV",
            description = "Importa usuários a partir de um arquivo CSV com as colunas name,email,password. A importação é transacional."
    )
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuários importados com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Arquivo CSV inválido ou dados incorretos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "E-mail já cadastrado"
            )
    })
    @PostMapping(
            value = "/import",
            consumes = "multipart/form-data"
    )
    public List<UserResponseDTO> importCsv(
            @RequestPart("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException(
                    "O arquivo CSV está vazio"
            );
        }

        String content = new String(
                file.getBytes(),
                StandardCharsets.UTF_8
        );

        return userUseCase.importCsv(content)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }


    private UserResponseDTO toResponseDTO(User user) {

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
