package com.example.jisoo.myfluffy;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MyValues {

//    static final LocalDate C_DATE = LocalDate.now();
//    static final int C_YEAR = C_DATE.getYear();
//    static final int C_MONTH = C_DATE.getMonthValue();
//    static final int C_DAY = C_DATE.getDayOfMonth();
//    static final LocalTime C_TIME = LocalTime.now();
    static final DateTimeFormatter DF_DEFAULT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    static final DateTimeFormatter DF_NOZERO = DateTimeFormatter.ofPattern("yyyy/M/d");
    static final DateTimeFormatter DF_FULL = DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)", Locale.KOREA);
    static final DateTimeFormatter DF_DAILY = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREA);
    static final DateTimeFormatter DF_MONTHLY = DateTimeFormatter.ofPattern("M월", Locale.KOREA);
    static final DateTimeFormatter TF_DEFAULT = DateTimeFormatter.ofPattern("HH:mm");
//    static final String C_DATE_STR = C_DATE.format(DF_DEFAULT);
//    static final String C_TIME_STR = C_TIME.format(TF_DEFAULT);
//    static final String C_TIME2_STR = C_TIME.plusMinutes(10).format(TF_DEFAULT);

    static final String SYMPTOMS = "이상";
    static final String FOOD = "식사";
    static final String TOILET = "배변";
    static final String WALK = "산책";
    static final String CLINIC = "진료";
    static final String VACCINE = "접종";
    static final String MEDICINE = "투약";
    static final String BATH = "목욕";
    static final String HAIRCUT = "미용";
    static final String DIARY = "일기";
    static final String ETC = "기타";

}

