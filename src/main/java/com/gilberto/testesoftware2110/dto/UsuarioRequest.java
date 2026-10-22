package com.gilberto.testesoftware2110.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import com.gilberto.testesoftware2110.validation.CpfValido;
import com.gilberto.testesoftware2110.validation.DataNascimentoValida;

public record UsuarioRequest(
        @NotBlank @CpfValido String cpf,
        @NotBlank @Size(max = 150)
        @Pattern(regexp = "^[\\p{L}\\p{M}]+(?: +[\\p{L}\\p{M}]+)*$", message = "Nome deve conter apenas letras e espaços") String nome,
        @NotNull @DataNascimentoValida LocalDate dataNascimento,
        @NotBlank @Size(min = 8, max = 100) String senha
) {}
