package org.dental.restclient;

import org.lab.exception.NotFoundCustomException;
import org.lab.model.DentalWork;
import org.lab.request.NewDentalWork;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.util.function.Consumer;

public class DentalWorkService {

    private static final String RESOURCE = "/dental_works";

    private final RestClient restClient;


    DentalWorkService(String baseUrl, RestClient.Builder restClientBuilder) {
        baseUrl += RESOURCE;
        restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }


    public DentalWork create(NewDentalWork newDentalWork) {
        return restClient
                .post()
                .body(newDentalWork)
                .retrieve()
                .body(DentalWork.class);
    }

    public DentalWork create(NewDentalWork newDentalWork, Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .post()
                .headers(headersConsumer)
                .body(newDentalWork)
                .retrieve()
                .body(DentalWork.class);
    }

    public DentalWork findById(Long id) {
        ResponseEntity<DentalWork> response = restClient
                .get()
                .uri(DentalLabRestClient.uriById(id))
                .retrieve()
                .toEntity(DentalWork.class);
        if (response.getStatusCode().value() == 200) {
            return response.getBody();
        } else if (response.getStatusCode().value() == 404) {
            throw NotFoundCustomException.byId(DentalWork.class.getSimpleName(), id);
        } else {
            //TODO
            throw new RuntimeException();
        }
    }

    public DentalWork findById(Long id, Consumer<HttpHeaders> headersConsumer) {
        ResponseEntity<DentalWork> response = restClient
                .get()
                .uri(DentalLabRestClient.uriById(id))
                .headers(headersConsumer)
                .retrieve()
                .toEntity(DentalWork.class);
        if (response.getStatusCode().value() == 200) {
            return response.getBody();
        } else if (response.getStatusCode().value() == 404) {
            throw NotFoundCustomException.byId(DentalWork.class.getSimpleName(), id);
        } else {
            //TODO
            throw new RuntimeException();
        }
    }

    public List<DentalWork> findAll() {
        return restClient
                .get()
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<DentalWork> findAll(Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .get()
                .headers(headersConsumer)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<DentalWork> findAllByMonth(int year, int month) {
        return restClient
                .get()
                .uri( uriBuilder -> uriBuilder
                        .path("/by-period")
                        .queryParam("year", year)
                        .queryParam("month", month)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<DentalWork> findAllByMonth(int year, int month, Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .get()
                .uri( uriBuilder -> uriBuilder
                        .path("/by-period")
                        .queryParam("year", year)
                        .queryParam("month", month)
                        .build())
                .headers(headersConsumer)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<DentalWork> searchDentalWorks(@Nullable String clinic, @Nullable String patient) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("clinic", clinic)
                        .queryParam("patient", patient)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<DentalWork> searchDentalWorks(@Nullable String clinic, @Nullable String patient, Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("clinic", clinic)
                        .queryParam("patient", patient)
                        .build())
                .headers(headersConsumer)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public DentalWork update(DentalWork updatable) {
        return restClient
                .put()
                .uri(DentalLabRestClient.uriById(updatable.getId()))
                .body(updatable)
                .retrieve()
                .body(DentalWork.class);
    }

    public DentalWork update(DentalWork updatable, Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .put()
                .uri(DentalLabRestClient.uriById(updatable.getId()))
                .headers(headersConsumer)
                .body(updatable)
                .retrieve()
                .body(DentalWork.class);
    }

    public void delete(long id) {
        ResponseEntity<Void> response = restClient
                .delete()
                .uri(DentalLabRestClient.uriById(id))
                .retrieve()
                .toBodilessEntity();
        if (response.getStatusCode().isError()) {
            throw new RuntimeException();
        }
    }

    public void delete(long id, Consumer<HttpHeaders> headersConsumer) {
        ResponseEntity<Void> response = restClient
                .delete()
                .uri(DentalLabRestClient.uriById(id))
                .headers(headersConsumer)
                .retrieve()
                .toBodilessEntity();
        if (response.getStatusCode().isError()) {
            throw new RuntimeException();
        }
    }
}
