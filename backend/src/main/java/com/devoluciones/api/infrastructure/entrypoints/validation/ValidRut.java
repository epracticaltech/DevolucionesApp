package com.devoluciones.api.infrastructure.entrypoints.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación custom Bean Validation para validar RUT chileno con algoritmo Módulo 11 (Exigencia 5).
 */
@Documented
@Constraint(validatedBy = RutValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRut {

    String message() default "El RUT ingresado no es válido según el algoritmo Módulo 11";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
