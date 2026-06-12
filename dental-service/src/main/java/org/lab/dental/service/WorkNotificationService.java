package org.lab.dental.service;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.entity.DentalWorkEntity;
import org.lab.dental.entity.MailingSubscription;
import org.lab.dental.entity.ProductEntity;
import org.lab.dental.repository.MailingSubscriptionRepository;
import org.lab.enums.MailingType;
import org.lab.event.EventMessage;
import org.lab.event.EventType;
import org.lab.model.TelegramChat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class WorkNotificationService {

    private static final String ITEM_TEMPLATE = """
            %s : %s
            %s : %s
            """;
    private static final String DELIMITER = " ***** ";

    private final MailingSubscriptionRepository subscriptionRepository;
    private final DentalWorkService dentalWorkService;
    private final UserService userService;
    private final NotificationService notificationService;


    @Autowired
    public WorkNotificationService(MailingSubscriptionRepository subscriptionRepository,
                                   DentalWorkService dentalWorkService,
                                   UserService userService,
                                   NotificationService notificationService) {
        this.subscriptionRepository = subscriptionRepository;
        this.dentalWorkService = dentalWorkService;
        this.userService = userService;
        this.notificationService = notificationService;
    }


    public void subscribe(UUID userId, MailingType type) {
        if (type.equals(MailingType.TELEGRAM)) {
            userService.getTelegramChat(userId);
        }
        MailingSubscription subscription = MailingSubscription.builder()
                .userId(userId)
                .type(type)
                .createdAt(LocalDate.now())
                .build();
        subscriptionRepository.save(subscription);
    }

    public void unsubscribe(UUID userId) {
        subscriptionRepository.deleteById(userId);
    }


    @Async
    @Scheduled(cron = "${project.mailing.schedule.cron}", zone = "${project.mailing.schedule.zone}")
    public void mailing() {
        List<MailingSubscription> subscriptions = subscriptionRepository.findAll();
        log.info("Found {} mailing subscriptions", subscriptions.size());
        for (MailingSubscription ms : subscriptions) {
            List<DentalWorkEntity> dwList = dentalWorkService.getForTomorrowByUserId(ms.getUserId());
            switch (ms.getType()) {
                case TELEGRAM -> telegramNotify(ms.getUserId(), concatenateWorks(dwList));
                case EMAIL -> emailNotify(ms.getUserId(), concatenateWorks(dwList));
                default -> log.error("Unexpected mailing type: {}", ms.getType());
            }
        }
    }


    private String concatenateWorks(List<DentalWorkEntity> works) {
        if (works.isEmpty()) {
            return "";
        }
        StringBuilder str = new StringBuilder();
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        for (int i = 0; i < works.size(); i++) {
            DentalWorkEntity dw = works.get(i);
            for (ProductEntity p : dw.getProducts()) {
                if (p.getCompleteAt().equals(tomorrow)) {
                    str.append(ITEM_TEMPLATE.formatted(dw.getClinic(), dw.getPatient(), p.getTitle(), p.getQuantity()));
                }
            }
            if ((i + 1) < works.size()) {
                str.append('\n')
                        .append(DELIMITER)
                        .append('\n');
            }
        }
        return str.toString();
    }

    private void telegramNotify(UUID userId, String message) {
        TelegramChat telegramChat = userService.getTelegramChat(userId);
        EventMessage eventMessage = EventMessage.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .chatId(telegramChat.getChatId())
                .type(EventType.MAILING)
                .language(telegramChat.getLanguage())
                .text(message)
                .createdAt(LocalDateTime.now())
                .build();
        notificationService.sendTelegramMessage(eventMessage);
    }

    private void emailNotify(UUID userId, String message) {
        String email = userService.getEmail(userId);
        notificationService.sendMailingEventToEmail(userId, email, message);
    }
}
