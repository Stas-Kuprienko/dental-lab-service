package org.lab.ui_application.util;

import lombok.Data;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

@Data
public class HeaderMonth {

    private final Month month;
    private final int monthValue;
    private final int prevMonthValue;
    private final int nowMonthValue;
    private final int year;
    private final int prevYear;
    private final int nowYear;
    private final boolean isCurrentMonth;


    public HeaderMonth(String year_month) {
        YearMonth now = YearMonth.now();
        if (year_month == null || year_month.isEmpty()) {
            this.year = now.getYear();
            this.month = now.getMonth();
            isCurrentMonth = true;
        } else {
            String[] year_month_split = year_month.split("-");
            YearMonth ofParameter = YearMonth.of(Integer.parseInt(year_month_split[0]), Integer.parseInt(year_month_split[1]));
            this.year = ofParameter.getYear();
            this.month = ofParameter.getMonth();
            isCurrentMonth = false;
        }
        this.nowYear = now.getYear();
        this.prevYear = now.minusYears(1).getYear();
        this.monthValue = month.getValue();
        this.nowMonthValue = now.getMonthValue();
        this.prevMonthValue = now.getMonth().minus(1).getValue();
    }


    public String value() {
        return month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.of("ru")).toUpperCase() + " - " + year;
    }
}