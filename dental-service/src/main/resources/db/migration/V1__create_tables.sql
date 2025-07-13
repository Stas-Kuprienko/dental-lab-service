CREATE SCHEMA IF NOT EXISTS dental_lab;

CREATE TABLE dental_lab.users (
    id UUID                 PRIMARY KEY DEFAULT gen_random_uuid(),
    login VARCHAR(255)      NOT NULL    UNIQUE,
    name VARCHAR(127)       NOT NULL,
    created_at DATE         NOT NULL    DEFAULT now(),
    status VARCHAR(63)      NOT NULL    DEFAULT 'ENABLED' CHECK (status IN('ENABLED', 'DISABLED'))
);


CREATE TABLE dental_lab.dental_work (
    id BIGSERIAL            PRIMARY KEY,
    clinic VARCHAR(127)     NOT NULL,
    patient VARCHAR(127)    NOT NULL,
    accepted_at DATE        NOT NULL    DEFAULT now(),
    complete_at DATE        NOT NULL    DEFAULT now(),
    status VARCHAR(63)      NOT NULL    DEFAULT 'MAKING' CHECK (status IN('MAKING', 'COMPLETED', 'PAID')),
    comment VARCHAR(255),
    user_id UUID            NOT NULL    REFERENCES dental_lab.users ON DELETE CASCADE
);

CREATE TABLE dental_lab.product_type (
    id UUID                 PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(127)      NOT NULL,
    price NUMERIC           NOT NULL    DEFAULT 0,
    user_id UUID            NOT NULL    REFERENCES dental_lab.users ON DELETE CASCADE,
	UNIQUE (user_id, title)
);

CREATE TABLE dental_lab.product (
    id UUID                 PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(127)      NOT NULL,
    quantity INT            NOT NULL    DEFAULT 0,
    price NUMERIC           NOT NULL    DEFAULT 0,
    accepted_at DATE        NOT NULL    DEFAULT now(),
    complete_at DATE        NOT NULL    DEFAULT now(),
    dental_work_id BIGINT   NOT NULL    REFERENCES dental_lab.dental_work ON DELETE CASCADE
);

CREATE TABLE dental_lab.photo_filename (
    filename VARCHAR(255)   PRIMARY KEY,
    dental_work_id BIGINT   NOT NULL    REFERENCES dental_lab.dental_work ON DELETE CASCADE
);
