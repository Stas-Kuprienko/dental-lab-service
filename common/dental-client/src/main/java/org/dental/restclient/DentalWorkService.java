package org.dental.restclient;

import org.lab.exception.NotFoundCustomException;
import org.lab.model.DentalWork;
import org.lab.request.NewDentalWork;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class DentalWorkService {

    private static final String RESOURCE = "/dental_works";

    private final RestClient restClient;


    DentalWorkService(String baseUrl, RestClient.Builder restClientBuilder) {
        baseUrl += RESOURCE;
        restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }


    public DentalWork create(UUID userId, NewDentalWork newDentalWork) {
        return restClient
                .post()
                .header(DentalLabRestClient.HEADER, userId.toString())
                .body(newDentalWork)
                .retrieve()
                .body(DentalWork.class);
    }



    public DentalWork findById(UUID userId, Long id) {
        ResponseEntity<DentalWork> response = restClient
                .get()
                .uri(DentalLabRestClient.uriById(id))
                .header(DentalLabRestClient.HEADER, userId.toString())
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

    public List<DentalWork> findAll(UUID userId) {
        return restClient
                .get()
                .header(DentalLabRestClient.HEADER, userId.toString())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<DentalWork> findAllByMonth(UUID userId, int year, int month) {
        return restClient
                .get()
                .uri( uriBuilder -> uriBuilder
                        .path("/by-period")
                        .queryParam("year", year)
                        .queryParam("month", month)
                        .build())
                .header(DentalLabRestClient.HEADER, userId.toString())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<DentalWork> searchDentalWorks(UUID userId, @Nullable String clinic, @Nullable String patient) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("clinic", clinic)
                        .queryParam("patient", patient)
                        .build())
                .header(DentalLabRestClient.HEADER, userId.toString())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public DentalWork update(UUID userId, DentalWork updatable) {
        return restClient
                .put()
                .uri(DentalLabRestClient.uriById(updatable.getId()))
                .header(DentalLabRestClient.HEADER, userId.toString())
                .body(updatable)
                .retrieve()
                .body(DentalWork.class);
    }

    public void delete(UUID userId, long id) {
        ResponseEntity<Void> response = restClient
                .delete()
                .uri(DentalLabRestClient.uriById(id))
                .header(DentalLabRestClient.HEADER, userId.toString())
                .retrieve()
                .toBodilessEntity();
        if (response.getStatusCode().isError()) {
            throw new RuntimeException();
        }
    }
}
