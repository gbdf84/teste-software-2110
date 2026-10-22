package com.gilberto.testesoftware2110.dto;

import com.gilberto.testesoftware2110.entity.Usuarios;
import java.time.LocalDate;
import java.util.UUID;

public record UsuarioResponse(UUID id, String cpf, String nome, LocalDate dataNascimento) {
    public static UsuarioResponse from(Usuarios usuario) {
        java.util.Date nascimento = usuario.getDataNascimento();
        LocalDate dataNascimento = nascimento instanceof java.sql.Date sqlDate
                ? sqlDate.toLocalDate()
                : nascimento.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        return new UsuarioResponse(usuario.getId(), usuario.getCpf(), usuario.getNome(), dataNascimento);
    }
}
