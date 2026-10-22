package com.gilberto.testesoftware2110.controller;

import com.gilberto.testesoftware2110.dto.UsuarioRequest;
import com.gilberto.testesoftware2110.dto.UsuarioResponse;
import com.gilberto.testesoftware2110.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
public class UsuariosController {
    private final UsuarioService service;
    public UsuariosController(UsuarioService service) { this.service = service; }

    @GetMapping
    public List<UsuarioResponse> listar() { return service.listar().stream().map(UsuarioResponse::from).toList(); }

    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable UUID id) { return UsuarioResponse.from(service.buscar(id)); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(@Valid @RequestBody UsuarioRequest request) {
        return UsuarioResponse.from(service.criar(request));
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable UUID id, @Valid @RequestBody UsuarioRequest request) {
        return UsuarioResponse.from(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID id) { service.remover(id); }
}
