package org.dental.restclient;

import org.lab.model.WorkPhotoFileData;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.util.function.Consumer;

public class WorkPhotoLinkService {

    private static final String RESOURCE = "/dental_works/%d/photo";

    private final RestClient restClient;


    WorkPhotoLinkService(RestClient restClient) {
        this.restClient = restClient;
    }


    public String create(long workId, byte[] fileBytes) {
        return restClient
                .post()
                .uri(RESOURCE.formatted(workId))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(resourceToRequest(fileBytes))
                .retrieve()
                .body(String.class);
    }

    public String create(long workId, byte[] fileBytes, Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .post()
                .uri(RESOURCE.formatted(workId))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .headers(headersConsumer)
                .body(resourceToRequest(fileBytes))
                .retrieve()
                .body(String.class);
    }

    public String findByIdAndFilename(long workId, String filename) {
        return restClient
                .get()
                .uri(RESOURCE.formatted(workId) + DentalLabRestClient.uriById(filename))
                .retrieve()
                .body(String.class);
    }

    public String findByIdAndFilename(long workId, String filename, Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .get()
                .uri(RESOURCE.formatted(workId) + DentalLabRestClient.uriById(filename))
                .headers(headersConsumer)
                .retrieve()
                .body(String.class);
    }

    public List<String> findAllById(long workId) {
        return restClient
                .get()
                .uri(RESOURCE.formatted(workId))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<String> findAllById(long workId, Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .get()
                .uri(RESOURCE.formatted(workId))
                .headers(headersConsumer)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public WorkPhotoFileData download(long workId, String filename) {
        return restClient
                .get()
                .uri(RESOURCE.formatted(workId) + "/download" + DentalLabRestClient.uriById(filename))
                .retrieve()
                .body(WorkPhotoFileData.class);
    }

    public WorkPhotoFileData download(long workId, String filename, Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .get()
                .uri(RESOURCE.formatted(workId) + "/download" + DentalLabRestClient.uriById(filename))
                .headers(headersConsumer)
                .retrieve()
                .body(WorkPhotoFileData.class);
    }

    public List<WorkPhotoFileData> downloadAllById(long workId) {
        return restClient
                .get()
                .uri(RESOURCE.formatted(workId) + "/download")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<WorkPhotoFileData> downloadAllById(long workId, Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .get()
                .uri(RESOURCE.formatted(workId) + "/download")
                .accept(MediaType.APPLICATION_JSON)
                .headers(headersConsumer)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public void deleteByIdAndFilename(long workId, String filename) {
        restClient
                .delete()
                .uri(RESOURCE.formatted(workId) + DentalLabRestClient.uriById(filename))
                .retrieve()
                .body(Void.class);
    }

    public void deleteByIdAndFilename(long workId, String filename, Consumer<HttpHeaders> headersConsumer) {
        restClient
                .delete()
                .uri(RESOURCE.formatted(workId) + DentalLabRestClient.uriById(filename))
                .headers(headersConsumer)
                .retrieve()
                .body(Void.class);
    }

    public MultiValueMap<String, Object> resourceToRequest(byte[] fileBytes) {
        String filename = System.currentTimeMillis() + ".jpg";
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
