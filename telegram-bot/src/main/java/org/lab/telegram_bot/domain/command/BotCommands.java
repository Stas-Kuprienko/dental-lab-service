package org.lab.telegram_bot.domain.command;

public enum BotCommands {

    START("/start"),
    LOGIN("/login"),
    HELP("/help"),
    CLEAR("/clear"),
    NEW_PRODUCT_TYPE("/new_product_type"),
    PRODUCT_MAP("/product_map"),
    NEW_DENTAL_WORK("/new_dental_work"),
    DENTAL_WORK_LIST("/dental_work_list"),
    SEARCH_BY("/search_by"),
    GET_REPORT("/get_report"),
    COUNT_PROFIT("/count_profit");


    public final String value;

    BotCommands(String value) {
        this.value = value;
    }
}
