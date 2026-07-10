package org.lab.dental.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.service.EventMessageService;
import org.lab.event.EventMessage;

@Slf4j
public class MockTelegramSender implements EventMessageService {


    @Override
    public void send(EventMessage message) {
        log.info("EventMessage is sent to Telegram");
    }
}
