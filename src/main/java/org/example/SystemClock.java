package org.example;

import java.time.LocalDate;

// THIS IS THE SYSTEM'S TIME MACHINE
public class SystemClock {
    // By default, the computer uses today's date.
    private static LocalDate simulatedDate = LocalDate.now();

    // This function will replace the entire LocalDate.now() command in the system
    public static LocalDate now() {
        return simulatedDate;
    }

    // Fast-forward function (adds days)
    public static void addDays(int days) {
        simulatedDate = simulatedDate.plusDays(days);
    }

    // Reset to the current time if the test is complete
    public static void reset() {
        simulatedDate = LocalDate.now();
    }
}