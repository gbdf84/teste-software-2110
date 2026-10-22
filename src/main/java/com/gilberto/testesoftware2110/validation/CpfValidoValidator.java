package com.gilberto.testesoftware2110.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidoValidator implements ConstraintValidator<CpfValido, String> {
    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        if (cpf == null || !cpf.matches("\\d{11}")) return false;
        if (cpf.chars().distinct().count() == 1) return false;
        return cpf.charAt(9) - '0' == calculateDigit(cpf, 9)
                && cpf.charAt(10) - '0' == calculateDigit(cpf, 10);
    }

    private int calculateDigit(String cpf, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) sum += (cpf.charAt(i) - '0') * (length + 1 - i);
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
