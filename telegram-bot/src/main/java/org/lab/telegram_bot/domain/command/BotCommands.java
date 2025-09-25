package org.lab.telegram_bot.domain.command;

public enum BotCommands {

    START("/start", false),
    LOGIN("/login", false),
    CLEAR("/clear", true),
    NEW_PRODUCT_TYPE("/new_product_type", true),
    PRODUCT_MAP("/product_map", true),
    NEW_DENTAL_WORK("/new_dental_work", true),
    VIEW_DENTAL_WORK("/view_dental_work", false),
    DENTAL_WORKS("/dental_works", true),
    WORK_FOR_TOMORROW("/work_for_tomorrow", true),
    SEARCH_BY("/search_by", true),
    GET_REPORT("/get_report", true),
    COUNT_PROFIT("/count_profit", true);


    public final String value;
    public final boolean isMenu;

    BotCommands(String value, boolean isMenu) {
        this.value = value;
        this.isMenu = isMenu;
    }
}
