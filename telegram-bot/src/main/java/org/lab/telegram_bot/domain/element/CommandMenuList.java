package org.lab.telegram_bot.domain.element;

import org.lab.telegram_bot.domain.command.BotCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class CommandMenuList {

    private final MessageSource messageSource;
    private final SetMyCommands botCommandListWithDefaultLocale;

    @Autowired
    public CommandMenuList(MessageSource messageSource) {
        this.messageSource = messageSource;
        botCommandListWithDefaultLocale = getMenuForLocale(Locale.getDefault());
    }


    public SetMyCommands getDefaultMenu() {
        return botCommandListWithDefaultLocale;
    }

    public SetMyCommands getMenuForLocale(Locale locale) {
        List<BotCommand> commandList = Arrays
                .stream(BotCommands.values())
                .filter(c -> c.isMenu)
                .map(c -> {
                    String description = messageSource.getMessage(c.value, null, locale);
                    return new BotCommand(c.value, description);
                })
                .toList();
        return new SetMyCommands(commandList, new BotCommandScopeDefault(), locale.getLanguage());
    }
}
