package org.lab.telegram_bot.utils;

import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.Locale;

public final class ChatBotUtility {

    private ChatBotUtility() {}


    public static Long getChatId(Update update) {
        return update.hasMessage() ?
                update.getMessage().getChatId() :
                update.getCallbackQuery().getMessage().getChatId();
    }

    public static Locale getLocale(Message message) {
        return Locale.of(message
                .getFrom()
                .getLanguageCode());
    }

    public static Locale getLocale(CallbackQuery callbackQuery) {
        return Locale.of(callbackQuery
                .getFrom()
                .getLanguageCode());
    }

    public static Locale getLocale(Update update) {
        return update.hasMessage() ?
                getLocale(update.getMessage()) :
                getLocale(update.getCallbackQuery());
    }

    public static String getUsername(Message message) {
        return message
                .getFrom()
                .getFirstName();
    }

    public static String getUsername(CallbackQuery callbackQuery) {
        return callbackQuery
                .getFrom()
                .getFirstName();
    }

    public static String getUsername(Update update) {
        return update.hasMessage() ?
                getUsername(update.getMessage()) :
                getUsername(update.getCallbackQuery());
    }

    public static String[] callBackQueryParse(CallbackQuery callbackQuery) {
        String[] callback = callbackQuery.getData().split(":");
        if (callback.length != 3) {
            throw new IllegalArgumentException("Incorrect callback query value: " + callbackQuery);
        }
        return callback;
    }

    public static String[] callBackQueryParse(String callbackQueryData) {
        String[] callback = callbackQueryData.split(":");
        if (callback.length != 3) {
            throw new IllegalArgumentException("Incorrect callback query value: " + callbackQueryData);
        }
        return callback;
    }

    public static String callBackQuery(ChatSession session, String data) {
        return session.getCommand().value +
                ':' +
                session.getStep() +
                ':' +
                data;
    }

    public static String callBackQuery(BotCommands command, int step, String data) {
        return command.name() +
                ':' +
                step +
                ':' +
                data;
    }

    public static String callBackQueryPrefix(BotCommands command, int step) {
        return command.name() +
                ':' +
                step +
                ':';
    }
}
