package org.lab.dental.util;

import jakarta.ws.rs.BadRequestException;
import java.time.DateTimeException;
import java.time.Month;
import java.time.YearMonth;

public final class RequestParamsConverter {

    private RequestParamsConverter() {}


    public static YearMonth converToYearMonth(int year, int month) {
        try{
            return YearMonth.of(year, Month.of(month));
        } catch (DateTimeException e) {
            throw new BadRequestException(e);
        }
    }
}
