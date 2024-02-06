package com.yury.trade.tradeHelper.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class Utils {

    public final static List<LocalDate> HOLIDAYS = List.of(
            //2022
            LocalDate.of(2022, 1, 17),
            LocalDate.of(2022, 2, 21),
            LocalDate.of(2022, 4, 15),
            LocalDate.of(2022, 5, 30),
            LocalDate.of(2022, 6, 20),
            LocalDate.of(2022, 7, 4),
            LocalDate.of(2022, 9, 5),
            LocalDate.of(2022, 11, 24),
            LocalDate.of(2022, 12, 26),
            //2023
            LocalDate.of(2023, 1, 2),
            LocalDate.of(2023, 1, 16),
            LocalDate.of(2023, 2, 20),
            LocalDate.of(2023, 4, 7),
            LocalDate.of(2023, 5, 29),
            LocalDate.of(2023, 6, 19),
            LocalDate.of(2023, 7, 4),
            LocalDate.of(2023, 9, 4),
            LocalDate.of(2023, 11, 23),
            LocalDate.of(2023, 12, 25),
            //2024
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 15),
            LocalDate.of(2024, 2, 19),
            LocalDate.of(2024, 3, 29),
            LocalDate.of(2024, 5, 27),
            LocalDate.of(2024, 6, 19),
            LocalDate.of(2024, 7, 4),
            LocalDate.of(2024, 9, 2),
            LocalDate.of(2024, 11, 23),
            LocalDate.of(2024, 12, 25),
            //2025
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 20),
            LocalDate.of(2024, 2, 17),
            LocalDate.of(2024, 4, 18),
            LocalDate.of(2024, 5, 26),
            LocalDate.of(2024, 6, 19),
            LocalDate.of(2024, 7, 4),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 11, 27),
            LocalDate.of(2024, 12, 25)
    );

    public static LocalDate convertToLocalDate(Date dateToConvert) {
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static int getDayOfWeek(LocalDate date) {
        return date.getDayOfWeek().getValue();
    }

    public static int getDayOfWeek(Date date) {
        return getDayOfWeek(convertToLocalDate(date));
    }

}
