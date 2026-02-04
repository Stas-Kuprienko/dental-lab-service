package org.dental.restclient;

import org.lab.model.DentalWork;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.util.function.Consumer;

public class OldRecordTransferService {

    private static final String RESOURCE = "/admin/old-transfer";

    private final RestClient restClient;


    OldRecordTransferService(RestClient restClient) {
        this.restClient = restClient;
    }


    public void transfer(List<DentalWork> dentalWorks) {
        restClient
                .post()
                .uri(RESOURCE)
                .body(dentalWorks)
                .retrieve()
                .toBodilessEntity();
    }

    public void transfer(List<DentalWork> dentalWorks, Consumer<HttpHeaders> headersConsumer) {
        restClient
                .post()
                .uri(RESOURCE)
                .headers(headersConsumer)
                .body(dentalWorks)
                .retrieve()
                .toBodilessEntity();
    }

}
