CREATE TABLE IF NOT EXISTS public.entry_day
(
    hash_email bigint NOT NULL,
    date integer NOT NULL,
    title character varying(25) COLLATE pg_catalog."default" NOT NULL DEFAULT '-TITLE-',
    entries_as_string character varying(21000) COLLATE pg_catalog."default",
    CONSTRAINT entry_day_pkey PRIMARY KEY (hash_email, date)
)
