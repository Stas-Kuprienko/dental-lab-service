package org.lab.telegram_bot.domain.command;

public enum BotCommands {

    START("/start"),
    HELP("/help"),
    CLEAR("/clear");


    public final String value;

    BotCommands(String value) {
        this.value = value;
    }
}
