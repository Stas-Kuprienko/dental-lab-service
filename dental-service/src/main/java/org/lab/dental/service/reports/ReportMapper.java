package org.lab.dental.service.reports;

import org.lab.enums.WorkStatus;
import org.lab.model.*;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ReportMapper {


    public List<List<String>> mapDentalWorkReport(List<DentalWork> works, List<String> productTitles) {
        List<List<String>> result = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        headers.add(XLSXFileTool.PATIENT);
        headers.add(XLSXFileTool.CLINIC);
        headers.addAll(productTitles.stream().map(String::toUpperCase).toList());
        result.add(headers);
        for (DentalWork work : works) {
            List<String> row = new ArrayList<>();
            row.add(work.getPatient());
            row.add(work.getClinic());
            Map<String, Integer> productMap = work.getProducts()
                    .stream()
                    .collect(Collectors.toMap(Product::getTitle, Product::getQuantity));
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

    public List<DentalWork> parseReportToDentalWorks(List<List<String>> data, ProductMap productMap, LocalDate completeAt, WorkStatus status) {
        List<String> header = data.getFirst();
        Map<String, UUID> productTypeMapping = new HashMap<>();
        for (String s : header) {
            for (ProductType p : productMap.getEntries()) {
                if (p.getTitle().equalsIgnoreCase(s)) {
                    productTypeMapping.put(s, p.getId());
                    break;
                }
            }
        }
        List<DentalWork> dentalWorks = new ArrayList<>();
        for (int i = 1; i < data.size(); i++) {
            List<String> row = data.get(i);
            int n = 0;
            UUID userId = productMap.getUserId();
            LocalDate acceptedAt = LocalDate.now();
            DentalWork.DentalWorkBuilder builder = DentalWork.builder()
                    .userId(userId)
                    .patient(row.get(n++))
                    .clinic(row.get(n++))
                    .acceptedAt(acceptedAt)
                    .completeAt(completeAt)
                    .status(status);
            List<Product> productList = new ArrayList<>();
            for (; n < header.size(); n++) {
                if (row.get(n).isBlank()) {
                    continue;
                }
                UUID productId = productTypeMapping.get(header.get(n));
                float price = productMap.get(productId).orElseGet(ProductType::new).getPrice();
                int quantity = (int) Double.parseDouble(row.get(n));
                Product product = Product.builder()
                        .title(header.get(n))
                        .price(price)
                        .quantity(quantity)
                        .acceptedAt(acceptedAt)
                        .completeAt(completeAt)
                        .build();
                productList.add(product);
            }
            if (!productList.isEmpty()) {
                builder.products(productList);
                dentalWorks.add(builder.build());
            }
        }
        return dentalWorks;
    }
}
