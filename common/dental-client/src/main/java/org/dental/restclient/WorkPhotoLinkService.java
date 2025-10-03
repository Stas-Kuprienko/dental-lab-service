package org.dental.restclient;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.util.function.Consumer;

public class WorkPhotoLinkService {

    private static final String RESOURCE = "/dental_works";
    private static final String URI_TEMPLATE = "/%d/photo";

    private final RestClient restClient;


    WorkPhotoLinkService(String baseUrl, RestClient.Builder restClientBuilder) {
        baseUrl += RESOURCE;
        restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }


    public String create(long workId, byte[] fileBytes) {
        return restClient
                .post()
                .uri(URI_TEMPLATE.formatted(workId))
                .body(fileBytes)
                .retrieve()
                .body(String.class);
    }

    public String create(long workId, byte[] fileBytes, Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .post()
                .uri(URI_TEMPLATE.formatted(workId))
                .headers(headersConsumer)
                .body(fileBytes)
                .retrieve()
                .body(String.class);
    }

    public String findByIdAndFilename(long workId, String filename) {
        return restClient
                .get()
                .uri(URI_TEMPLATE.formatted(workId) + DentalLabRestClient.uriById(filename))
                .retrieve()
                .body(String.class);
    }

    public String findByIdAndFilename(long workId, String filename, Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .get()
                .uri(URI_TEMPLATE.formatted(workId) + DentalLabRestClient.uriById(filename))
                .headers(headersConsumer)
                .retrieve()
                .body(String.class);
    }

    public List<String> findAllById(long workId) {
        return restClient
                .get()
                .uri(URI_TEMPLATE.formatted(workId))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<String> findAllById(long workId, Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .get()
                .uri(URI_TEMPLATE.formatted(workId))
                .headers(headersConsumer)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public void deleteByIdAndFilename(long workId, String filename) {
        restClient
                .delete()
                .uri(URI_TEMPLATE.formatted(workId) + DentalLabRestClient.uriById(filename))
                .retrieve()
                .body(Void.class);
    }

    public void deleteByIdAndFilename(long workId, String filename, Consumer<HttpHeaders> headersConsumer) {
        restClient
                .delete()
                .uri(URI_TEMPLATE.formatted(workId) + DentalLabRestClient.uriById(filename))
                .headers(headersConsumer)
                .retrieve()
                .body(Void.class);
    }
}
