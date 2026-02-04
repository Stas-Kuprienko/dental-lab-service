package org.lab.uimvc.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dental.restclient.DentalLabRestClient;
import org.dental.restclient.OldRecordTransferService;
import org.lab.exception.ApplicationCustomException;
import org.lab.model.DentalWork;
import org.lab.old.OldDentalWork;
import org.lab.old.OldDentalWorkConverter;
import org.lab.old.OldProductConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class OldRecordsTransferService {

    private final ObjectMapper objectMapper;
    private final OldDentalWorkConverter oldDentalWorkConverter;
    private final OldRecordTransferService oldRecordTransferService;

    @Autowired
    public OldRecordsTransferService(ObjectMapper objectMapper,
                                     DentalLabRestClient dentalLabRestClient) {
        this.objectMapper = objectMapper;
        this.oldDentalWorkConverter = new OldDentalWorkConverter(new OldProductConverter());
        this.oldRecordTransferService = dentalLabRestClient.OLD_RECORD_TRANSFER;
    }


    public void transfer(MultipartFile file, UUID userId) {
        try {
            List<OldDentalWork> oldDentalWorks = objectMapper.readValue(file.getBytes(), new TypeReference<>() {});
            List<DentalWork> dentalWorks = oldDentalWorks
                    .stream()
                    .map(odw -> oldDentalWorkConverter.parse(odw, userId))
                    .toList();
            oldRecordTransferService.transfer(dentalWorks);
        } catch (IOException e) {
            throw new ApplicationCustomException(e);
        }
    }
}
