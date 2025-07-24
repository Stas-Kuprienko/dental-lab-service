package org.lab.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class EventMessage implements Serializable {

    private UUID id;

    private NotificationType notificationType;


    public EventMessage() {}
}
