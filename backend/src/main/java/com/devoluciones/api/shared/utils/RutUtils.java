package com.devoluciones.api.shared.utils;

public class RutUtils {

    private RutUtils() {
        // Clase de utilidad no instanciable
    }

    /**
     * Valida si un RUT chileno es válido según el algoritmo Módulo 11.
     * Acepta formatos con o sin puntos y con guion (ej: 12.345.678-K, 12345678-K,
     * 9685216-9).
     */
    public static boolean esRutValido(String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            return false;
        }

        // Limpiar puntos y espacios, convertir a mayúsculas
        String rutLimpio = rut.replace(".", "").trim().toUpperCase();

        // Validar formato básico: entre 7 y 8 dígitos, guion y dígito verificador (0-9
        // o K)
        if (!rutLimpio.matches("^[0-9]{7,8}-[0-9K]$")) {
            return false;
        }

        String[] partes = rutLimpio.split("-");
        String numeroStr = partes[0];
        char dvIngresado = partes[1].charAt(0);

        char dvCalculado = calcularDigitoVerificador(numeroStr);

        return dvIngresado == dvCalculado;
    }

    /**
     * Calcula el Dígito Verificador Módulo 11 para la serie de números dada.
     */
    private static char calcularDigitoVerificador(String numeroStr) {
        int suma = 0;
        int multiplicador = 2;

        for (int i = numeroStr.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(numeroStr.charAt(i)) * multiplicador;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }

        int resto = 11 - (suma % 11);

        if (resto == 11) {
            return '0';
        } else if (resto == 10) {
            return 'K';
        } else {
            return Character.forDigit(resto, 10);
        }
    }

    /**
     * Formatea un RUT a su representación estándar (ej: 12345678-K).
     */
    public static String formatearRut(String rut) {
        if (rut == null)
            return null;
        String limpio = rut.replace(".", "").trim().toUpperCase();
        return limpio;
    }
}
