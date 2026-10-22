package com.gilberto.testesoftware2110.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = DataNascimentoValidaValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataNascimentoValida {
    String message() default "Usuário deve ter entre 18 e 120 anos";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
