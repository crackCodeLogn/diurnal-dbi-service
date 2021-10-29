CREATE TABLE IF NOT EXISTS entry_day(
    hash_email bigint NOT NULL,
    date integer NOT NULL,
    title character varying(25) NOT NULL DEFAULT '-TITLE-',
    entries_as_string character varying(21000),

    CONSTRAINT entry_day_pkey PRIMARY KEY (hash_email, date)
);