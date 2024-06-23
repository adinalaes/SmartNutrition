package com.example.smartnutrtion;

public class UserReportCalculator {

    public static double calculateBMI(long weight, long height) {
        return weight / Math.pow(height / 100.0, 2);
    }

    public static int calculateDailyCalories(long age, long weight, long height, String sex) {
        if (sex == null) {
            return 0;
        }

        if (sex.equals("Masculin")) {
            return (int) (10 * weight + 6.25 * height - 5 * age + 5);
        } else if (sex.equals("Feminin")) {
            return (int) (10 * weight + 6.25 * height - 5 * age - 161);
        } else {
            return 0;
        }
    }
}
