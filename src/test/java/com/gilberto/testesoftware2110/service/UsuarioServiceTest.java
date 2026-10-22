package com.gilberto.testesoftware2110.service;

import com.gilberto.testesoftware2110.dto.UsuarioRequest;
import com.gilberto.testesoftware2110.entity.Usuarios;
import com.gilberto.testesoftware2110.reporitory.UsuariosRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    @Mock
    private UsuariosRepository repository;

    @InjectMocks
    private UsuarioService service;

    @Test
    void deveInserirUsuario() {
        UsuarioRequest request = request("Maria de Souza", "52998224725");
        when(repository.existsByCpf(request.cpf())).thenReturn(false);
        when(repository.save(any(Usuarios.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuarios salvo = service.criar(request);

        assertEquals(request.nome(), salvo.getNome());
        assertEquals(request.cpf(), salvo.getCpf());
        assertEquals(request.dataNascimento(), ((java.sql.Date) salvo.getDataNascimento()).toLocalDate());
        verify(repository).save(any(Usuarios.class));
    }

    @Test
    void deveEditarUsuarioSemAlterarCpf() {
        UUID id = UUID.randomUUID();
        Usuarios existente = usuario(id, "52998224725", "Nome antigo");
        UsuarioRequest request = request("Nome atualizado", existente.getCpf());
        when(repository.findById(id)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        Usuarios atualizado = service.atualizar(id, request);

        assertSame(existente, atualizado);
        assertEquals("Nome atualizado", atualizado.getNome());
        assertEquals("52998224725", atualizado.getCpf());
        verify(repository).save(existente);
    }

    @Test
    void deveExcluirUsuario() {
        UUID id = UUID.randomUUID();
        Usuarios existente = usuario(id, "52998224725", "Nome para excluir");
        when(repository.findById(id)).thenReturn(Optional.of(existente));

        service.remover(id);

        verify(repository).delete(existente);
    }

    private UsuarioRequest request(String nome, String cpf) {
        return new UsuarioRequest(cpf, nome, LocalDate.now().minusYears(30), "SenhaSegura123");
    }

    private Usuarios usuario(UUID id, String cpf, String nome) {
        Usuarios usuario = new Usuarios();
        usuario.setId(id);
        usuario.setCpf(cpf);
        usuario.setNome(nome);
        usuario.setDataNascimento(java.sql.Date.valueOf(LocalDate.now().minusYears(30)));
        usuario.setSenha("SenhaSegura123");
        return usuario;
    }
}
