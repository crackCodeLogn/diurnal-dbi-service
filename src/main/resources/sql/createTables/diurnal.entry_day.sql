CREATE TABLE IF NOT EXISTS public.entry_day
(
    mobile bigint NOT NULL,
    date integer NOT NULL,
    entries_as_string character varying(21000) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT entry_pkey PRIMARY KEY (mobile, date)
)
