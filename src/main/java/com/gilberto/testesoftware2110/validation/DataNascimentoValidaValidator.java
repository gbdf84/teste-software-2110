package com.gilberto.testesoftware2110.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class DataNascimentoValidaValidator implements ConstraintValidator<DataNascimentoValida, LocalDate> {
    @Override
    public boolean isValid(LocalDate dataNascimento, ConstraintValidatorContext context) {
        if (dataNascimento == null) return true; // @NotNull valida o campo obrigatório.
        LocalDate hoje = LocalDate.now();
        return !dataNascimento.isAfter(hoje.minusYears(18))
                && !dataNascimento.isBefore(hoje.minusYears(120));
    }
}
