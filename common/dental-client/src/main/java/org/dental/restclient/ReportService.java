package org.dental.restclient;

import org.lab.enums.WorkStatus;
import org.lab.model.DentalWork;
import org.lab.model.ProfitRecord;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import java.time.YearMonth;
import java.util.List;
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

    public List<DentalWork> updateReport(byte[] fileBytes, YearMonth completeAt, WorkStatus status) {
        return restClient
                .post()
                .uri(uri -> uri.path(RESOURCE + "/works")
                        .queryParam("complete-at", completeAt)
                        .queryParam("status", status)
                        .build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(resourceToRequest(fileBytes))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<DentalWork> updateReport(byte[] fileBytes, YearMonth completeAt, WorkStatus status, Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .post()
                .uri(uri -> uri.path(RESOURCE + "/works")
                        .queryParam("complete-at", completeAt)
                        .queryParam("status", status)
                        .build())
                .headers(headersConsumer)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(resourceToRequest(fileBytes))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
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


    public MultiValueMap<String, Object> resourceToRequest(byte[] fileBytes) {
        String filename = System.currentTimeMillis() + ".xlsx";
        ByteArrayResource resource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        body.add("description", "This is a test file upload.");
        return body;
    }
}
