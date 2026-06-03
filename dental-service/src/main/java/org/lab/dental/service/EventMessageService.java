package org.lab.dental.service;

import org.lab.event.EventMessage;

public interface EventMessageService {

    void send(EventMessage message);
}
