package org.lab.telegram_bot.domain.element;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class InlineKeyboardBuilder {

    private final MessageSource messageSource;

    @Autowired
    public InlineKeyboardBuilder(MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    @SafeVarargs
    public final InlineKeyboardMarkup inlineKeyboard(List<InlineKeyboardButton>... buttons) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttonRows = List.of(buttons);
        keyboardMarkup.setKeyboard(buttonRows);
        return keyboardMarkup;
    }

    public InlineKeyboardMarkup inlineKeyboard(List<List<InlineKeyboardButton>> buttons) {
        return new InlineKeyboardMarkup(buttons);
    }

    public InlineKeyboardMarkup flatInlineKeyboard(Locale locale, List<String> buttons) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> keyboardButtons = inlineKeyboardButtons(locale, buttons);
        List<List<InlineKeyboardButton>> buttonRows = List.of(keyboardButtons);
        keyboardMarkup.setKeyboard(buttonRows);
        return keyboardMarkup;
    }

    public InlineKeyboardMarkup multiInlineKeyboard(Locale locale, List<List<String>> buttonLists) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttonRows = buttonLists.stream()
                .map(list -> inlineKeyboardButtons(locale, list))
                .toList();
        keyboardMarkup.setKeyboard(buttonRows);
        return keyboardMarkup;
    }

    public List<InlineKeyboardButton> inlineKeyboardButtons(Locale locale, List<String> callbackQueries) {
        return callbackQueries.stream()
                .map(callback -> {
                    String label = messageSource
                            .getMessage(callback, null, locale);
                    var button = new InlineKeyboardButton(label);
                    button.setCallbackData(callback);
                    return button;
                })
                .toList();
    }

    public List<InlineKeyboardButton> inlineKeyboardButtons(Locale locale, String... callbackQueries) {
        return Arrays.stream(callbackQueries)
                .map(callback -> {
                    String label = messageSource
                            .getMessage(callback, null, locale);
                    var button = new InlineKeyboardButton(label);
                    button.setCallbackData(callback);
                    return button;
                })
                .toList();
    }

    /**
     * Expects as argument Map of callback data (key) and button label (value).
     * @param values {@link Map} of callback query data and button label.
     * @return List of {@link InlineKeyboardButton}.
     */
    public List<InlineKeyboardButton> inlineKeyboardButtons(Map<String, String> values) {
        return values.entrySet()
                .stream()
                .map(e -> {
                    var button = new InlineKeyboardButton(e.getValue());
                    button.setCallbackData(e.getKey());
                    return button;
                })
                .toList();
    }

    public List<InlineKeyboardButton> singleInlineKeyboardButton(String callback, String label) {
        var button = new InlineKeyboardButton(label);
        button.setCallbackData(callback);
        return List.of(button);
    }

    public InlineKeyboardButton navigationButton(ButtonKeys buttonKey, String callbackPrefix, Locale locale) {
        String label = messageSource.getMessage(buttonKey.name(), null, locale);
        var button = new InlineKeyboardButton(label);
        button.setCallbackData(callbackPrefix + buttonKey.name());
        return button;
    }

    public List<InlineKeyboardButton> navigationButtonRow(String callbackPrefix, boolean isFirstPage, Locale locale) {
        var backButton = navigationButton(ButtonKeys.BACK, callbackPrefix, locale);
        var nextButton = navigationButton(ButtonKeys.NEXT, callbackPrefix, locale);
        if (isFirstPage) {
            return List.of(backButton, nextButton);
        } else {
            var prevButton = navigationButton(ButtonKeys.PREVIOUS, callbackPrefix, locale);
            return List.of(backButton, prevButton, nextButton);
        }
    }
}
