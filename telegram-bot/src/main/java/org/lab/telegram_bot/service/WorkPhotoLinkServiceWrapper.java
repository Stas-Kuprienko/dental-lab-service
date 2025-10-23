package org.lab.telegram_bot.service;

import org.dental.restclient.WorkPhotoLinkService;
import org.lab.model.WorkPhotoFileData;
import org.springframework.http.HttpHeaders;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class WorkPhotoLinkServiceWrapper {

    private final WorkPhotoLinkService workPhotoLinkService;
    private final Function<UUID, Consumer<HttpHeaders>> httpHeaderConsumerFunction;

    public WorkPhotoLinkServiceWrapper(WorkPhotoLinkService workPhotoLinkService1, Function<UUID, Consumer<HttpHeaders>> httpHeaderConsumerFunction) {
        this.workPhotoLinkService = workPhotoLinkService1;
        this.httpHeaderConsumerFunction = httpHeaderConsumerFunction;
    }


    public String create(long workId, byte[] fileBytes, UUID userId) {
        return workPhotoLinkService.create(workId, fileBytes, httpHeaderConsumerFunction.apply(userId));
    }

    public String findByIdAndFilename(long workId, String filename, UUID userId) {
        return workPhotoLinkService.findByIdAndFilename(workId, filename, httpHeaderConsumerFunction.apply(userId));
    }

    public List<String> findAllById(long workId, UUID userId) {
        return workPhotoLinkService.findAllById(workId, httpHeaderConsumerFunction.apply(userId));
    }

    public WorkPhotoFileData download(long workId, String filename, UUID userId) {
        return workPhotoLinkService.download(workId, filename, httpHeaderConsumerFunction.apply(userId));
    }

    public List<WorkPhotoFileData> downloadAllById(long workId, UUID userId) {
        return workPhotoLinkService.downloadAllById(workId, httpHeaderConsumerFunction.apply(userId));
    }

    public void deleteByIdAndFilename(long workId, String filename, UUID userId) {
        workPhotoLinkService.deleteByIdAndFilename(workId, filename, httpHeaderConsumerFunction.apply(userId));
    }
}
