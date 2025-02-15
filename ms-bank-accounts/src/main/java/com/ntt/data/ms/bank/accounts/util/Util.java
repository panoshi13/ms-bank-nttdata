package com.ntt.data.ms.bank.accounts.util;

import java.time.LocalDate;

public class Util {
    public static boolean isDayOfMoth(int dia) {
        // Obtener la fecha actual
        LocalDate fechaActual = LocalDate.now();
        // Obtener el día del mes actual
        int diaActual = fechaActual.getDayOfMonth();
        // Comparar el día actual con el día proporcionado
        return diaActual == dia;
    }
}
