package com.example.smartnutrtion;

import org.junit.Test;
import static org.junit.Assert.*;

public class UserReportCalculatorTest {

    @Test
    public void testCalculateBMI() {
        double bmi = UserReportCalculator.calculateBMI(70, 180);
        assertEquals(21.60, bmi, 0.01);
    }

    @Test
    public void testCalculateDailyCalories_Male() {
        int dailyCalories = UserReportCalculator.calculateDailyCalories(25, 70, 180, "Masculin");
        assertEquals(1705, dailyCalories);
    }

    @Test
    public void testCalculateDailyCalories_Female() {
        int dailyCalories = UserReportCalculator.calculateDailyCalories(25, 70, 180, "Feminin");
        assertEquals(1539, dailyCalories);
    }

    @Test
    public void testCalculateBMI_DifferentValues() {
        double bmi = UserReportCalculator.calculateBMI(85, 175);
        assertEquals(27.76, bmi, 0.01);

        bmi = UserReportCalculator.calculateBMI(50, 160);
        assertEquals(19.53, bmi, 0.01);
    }

    @Test
    public void testCalculateDailyCalories_Male_DifferentValues() {
        int dailyCalories = UserReportCalculator.calculateDailyCalories(30, 85, 175, "Masculin");
        assertEquals(1798, dailyCalories);

        dailyCalories = UserReportCalculator.calculateDailyCalories(40, 90, 180, "Masculin");
        assertEquals(1830, dailyCalories);
    }

    @Test
    public void testCalculateDailyCalories_Female_DifferentValues() {
        int dailyCalories = UserReportCalculator.calculateDailyCalories(30, 55, 160, "Feminin");
        assertEquals(1239, dailyCalories);

        dailyCalories = UserReportCalculator.calculateDailyCalories(50, 70, 170, "Feminin");
        assertEquals(1351, dailyCalories);
    }

    @Test
    public void testCalculateDailyCalories_UnknownSex() {
        int dailyCalories = UserReportCalculator.calculateDailyCalories(25, 70, 180, "Unknown");
        assertEquals(0, dailyCalories);
    }
}
