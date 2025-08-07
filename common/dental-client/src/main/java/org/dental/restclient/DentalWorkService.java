package org.dental.restclient;

import org.lab.exception.NotFoundCustomException;
import org.lab.model.DentalWork;
import org.lab.request.NewDentalWork;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestClient;
import java.util.List;

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

    public List<DentalWork> findAll() {
        return restClient
                .get()
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

    public DentalWork update(DentalWork updatable) {
        return restClient
                .put()
                .uri(DentalLabRestClient.uriById(updatable.getId()))
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

    public DentalWork create(NewDentalWork newDentalWork, String headerKey, String headerValue) {
        return restClient
                .post()
                .header(headerKey, headerValue)
                .body(newDentalWork)
                .retrieve()
                .body(DentalWork.class);
    }



    public DentalWork findById(Long id, String headerKey, String headerValue) {
        ResponseEntity<DentalWork> response = restClient
                .get()
                .uri(DentalLabRestClient.uriById(id))
                .header(headerKey, headerValue)
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

    public List<DentalWork> findAll(String headerKey, String headerValue) {
        return restClient
                .get()
                .header(headerKey, headerValue)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<DentalWork> findAllByMonth(int year, int month, String headerKey, String headerValue) {
        return restClient
                .get()
                .uri( uriBuilder -> uriBuilder
                        .path("/by-period")
                        .queryParam("year", year)
                        .queryParam("month", month)
                        .build())
                .header(headerKey, headerValue)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<DentalWork> searchDentalWorks(@Nullable String clinic, @Nullable String patient, String headerKey, String headerValue) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("clinic", clinic)
                        .queryParam("patient", patient)
                        .build())
                .header(headerKey, headerValue)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public DentalWork update(DentalWork updatable, String headerKey, String headerValue) {
        return restClient
                .put()
                .uri(DentalLabRestClient.uriById(updatable.getId()))
                .header(headerKey, headerValue)
                .body(updatable)
                .retrieve()
                .body(DentalWork.class);
    }

    public void delete(long id, String headerKey, String headerValue) {
        ResponseEntity<Void> response = restClient
                .delete()
                .uri(DentalLabRestClient.uriById(id))
                .header(headerKey, headerValue)
                .retrieve()
                .toBodilessEntity();
        if (response.getStatusCode().isError()) {
            throw new RuntimeException();
        }
    }
}
