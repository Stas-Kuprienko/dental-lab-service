package org.lab.dental.service.reports;

import org.lab.dental.entity.DentalWorkEntity;
import org.lab.dental.entity.ProductEntity;
import org.lab.model.ProfitRecord;
import org.springframework.stereotype.Component;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ReportMapper {


    public List<List<String>> mapDentalWorkReport(List<DentalWorkEntity> works, List<String> productTitles) {
        List<List<String>> result = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        headers.add("ПАЦИЕНТ");
        headers.add("КЛИНИКА");
        headers.addAll(productTitles.stream().map(String::toUpperCase).toList());
        result.add(headers);
        for (DentalWorkEntity work : works) {
            List<String> row = new ArrayList<>();
            row.add(work.getPatient());
            row.add(work.getClinic());
            Map<String, Integer> productMap = work.getProducts()
                    .stream()
                    .collect(Collectors.toMap(ProductEntity::getTitle, ProductEntity::getQuantity));
            for (String title : productTitles) {
                row.add(Optional.ofNullable(productMap.get(title)).map(String::valueOf).orElse(""));
            }
            result.add(row);
        }
        return result;
    }

    public List<List<String>> mapProfitReport(List<ProfitRecord> records) {
        List<List<String>> result = new ArrayList<>();
        result.add(List.of("ГОД", "МЕСЯЦ", "ДОХОД"));
        for (ProfitRecord pr : records) {
            result.add(List.of(
                    String.valueOf(pr.getYear()),
                    pr.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.of("ru")),
                    String.valueOf(pr.getValue())
            ));
        }
        return result;
    }
}
