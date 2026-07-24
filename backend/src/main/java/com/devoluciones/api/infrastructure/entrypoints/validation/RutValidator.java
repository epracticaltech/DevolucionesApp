package com.devoluciones.api.infrastructure.entrypoints.validation;

import com.devoluciones.api.shared.utils.RutUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RutValidator implements ConstraintValidator<ValidRut, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Dejar que @NotBlank maneje el caso nulo/vacío si aplica
        }
        return RutUtils.esRutValido(value);
    }
}
