package com.gilberto.testesoftware2110.service;

import com.gilberto.testesoftware2110.dto.UsuarioRequest;
import com.gilberto.testesoftware2110.entity.Usuarios;
import com.gilberto.testesoftware2110.reporitory.UsuariosRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.sql.Date;
import java.util.List;
import java.util.UUID;

@Service
public class UsuarioService {
    private final UsuariosRepository repository;

    public UsuarioService(UsuariosRepository repository) { this.repository = repository; }

    public List<Usuarios> listar() { return repository.findAll(); }
    public Usuarios buscar(UUID id) { return repository.findById(id).orElseThrow(() -> naoEncontrado(id)); }

    public Usuarios criar(UsuarioRequest request) {
        if (repository.existsByCpf(request.cpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado");
        }
        return repository.save(mapear(request, new Usuarios()));
    }

    public Usuarios atualizar(UUID id, UsuarioRequest request) {
        Usuarios usuario = buscar(id);
        if (!usuario.getCpf().equals(request.cpf())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O CPF não pode ser alterado");
        }
        return repository.save(mapear(request, usuario));
    }

    public void remover(UUID id) { repository.delete(buscar(id)); }

    private Usuarios mapear(UsuarioRequest request, Usuarios usuario) {
        usuario.setCpf(request.cpf());
        usuario.setNome(request.nome());
        usuario.setDataNascimento(Date.valueOf(request.dataNascimento()));
        usuario.setSenha(request.senha());
        return usuario;
    }

    private ResponseStatusException naoEncontrado(UUID id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado: " + id);
    }
}
