CREATE TABLE IF NOT EXISTS entry_day(
    hel_dt VARCHAR(50),
    hash_email NUMERIC NOT NULL,
    date integer NOT NULL,
    title VARCHAR(25) NOT NULL DEFAULT '-TITLE-',
    entries_as_string VARCHAR(21000),

    CONSTRAINT entry_day_pkey PRIMARY KEY (hel_dt)
--    CONSTRAINT entry_day_pkey PRIMARY KEY (hash_email, date)
);