package org.lab.telegram_bot.service;

import org.dental.restclient.UserService;
import org.lab.enums.MailingType;
import org.lab.model.User;
import org.springframework.http.HttpHeaders;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class UserServiceWrapper {

    private final UserService userService;
    private final Function<UUID, Consumer<HttpHeaders>> httpHeaderConsumerFunction;

    public UserServiceWrapper(UserService userService, Function<UUID, Consumer<HttpHeaders>> httpHeaderConsumerFunction) {
        this.userService = userService;
        this.httpHeaderConsumerFunction = httpHeaderConsumerFunction;
    }


    public User get(UUID userId) {
        return userService.get(httpHeaderConsumerFunction.apply(userId));
    }

    public boolean subscribeForNotifications(MailingType type, UUID userId) {
        return userService.subscribeForNotifications(type, httpHeaderConsumerFunction.apply(userId));
    }

    public boolean unsubscribeForNotifications(UUID userId) {
        return userService.unsubscribeForNotifications(httpHeaderConsumerFunction.apply(userId));
    }
}
