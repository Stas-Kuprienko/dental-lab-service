package org.dental.restclient;

import org.lab.model.ProfitRecord;
import org.springframework.web.client.RestClient;
import java.util.UUID;

public class ReportService {

    private static final String RESOURCE = "/reports";

    private final RestClient restClient;


    ReportService(String baseUrl, RestClient.Builder restClientBuilder) {
        baseUrl += RESOURCE;
        restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }


    public byte[] downloadWorkReport(UUID userId, int year, int month) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/works")
                        .queryParam("year", year)
                        .queryParam("month", month)
                        .build())
                .header(DentalLabRestClient.HEADER, userId.toString())
                .retrieve()
                .body(byte[].class);
    }

    public ProfitRecord countProfitForMonth(UUID userId, int year, int month) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/profit")
                        .queryParam("year", year)
                        .queryParam("month", month)
                        .build())
                .header(DentalLabRestClient.HEADER, userId.toString())
                .retrieve()
                .body(ProfitRecord.class);
    }
}
