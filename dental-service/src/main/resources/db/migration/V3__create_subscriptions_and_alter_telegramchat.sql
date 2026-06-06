
CREATE TABLE dental_lab.mailing_subscription (
    user_id UUID            PRIMARY KEY     REFERENCES dental_lab.users ON DELETE CASCADE,
    type VARCHAR(31)        NOT NULL        DEFAULT 'EMAIL' CHECK (type IN('EMAIL', 'TELEGRAM')),
    created_at DATE         NOT NULL        DEFAULT now()
);

ALTER TABLE dental_lab.telegram_chat ADD language VARCHAR(31);