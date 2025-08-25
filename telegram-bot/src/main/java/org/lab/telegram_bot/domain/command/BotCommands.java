package org.lab.telegram_bot.domain.command;

public enum BotCommands {

    START("/start", true),
    LOGIN("/login", true),
    HELP("/help", true),
    CLEAR("/clear", true),
    NEW_PRODUCT_TYPE("/new_product_type", false),
    PRODUCT_MAP("/product_map", false),
    NEW_DENTAL_WORK("/new_dental_work", false),
    WORK_FOR_TOMORROW("/work_for_tomorrow", false),
    SEARCH_BY("/search_by", false),
    GET_REPORT("/get_report", false),
    COUNT_PROFIT("/count_profit", false);


    public final String value;
    public final boolean isMenu;

    BotCommands(String value, boolean isMenu) {
        this.value = value;
        this.isMenu = isMenu;
    }
}
