package org.dental.restclient;

import org.lab.model.ProfitRecord;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import java.util.function.Consumer;

public class ReportService {

    private static final String RESOURCE = "/reports";

    private final RestClient restClient;


    ReportService(RestClient restClient) {
        this.restClient = restClient;
    }


    public byte[] downloadWorkReport(int year, int month) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(RESOURCE + "/works")
                        .queryParam("year", year)
                        .queryParam("month", month)
                        .build())
                .retrieve()
                .body(byte[].class);
    }

    public byte[] downloadWorkReport(int year, int month, Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(RESOURCE + "/works")
                        .queryParam("year", year)
                        .queryParam("month", month)
                        .build())
                .headers(headersConsumer)
                .retrieve()
                .body(byte[].class);
    }

    public ProfitRecord countProfitForMonth(int year, int month) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(RESOURCE + "/profit")
                        .queryParam("year", year)
                        .queryParam("month", month)
                        .build())
                .retrieve()
                .body(ProfitRecord.class);
    }

    public ProfitRecord countProfitForMonth(int year, int month, Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(RESOURCE + "/profit")
                        .queryParam("year", year)
                        .queryParam("month", month)
                        .build())
                .headers(headersConsumer)
                .retrieve()
                .body(ProfitRecord.class);
    }
}
