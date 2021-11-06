CREATE TABLE IF NOT EXISTS entry_day2(
    hel_dt VARCHAR(50) PRIMARY KEY,
    hash_email NUMERIC NOT NULL,
    date integer NOT NULL,
    title VARCHAR(25) NOT NULL DEFAULT '-TITLE-',
    entries_as_string VARCHAR(21000)
);