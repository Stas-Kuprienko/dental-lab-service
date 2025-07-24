package org.lab.telegram_bot.datasource;

import org.lab.telegram_bot.domain.session.ChatSession;

public interface ChatSessionRepository {

    boolean save(ChatSession chatSession);

    ChatSession find(long chatId);
}
