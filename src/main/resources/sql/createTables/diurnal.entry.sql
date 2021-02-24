CREATE TABLE IF NOT EXISTS public.entry
(
    mobile bigint NOT NULL,
    date integer NOT NULL,
    serial integer NOT NULL,
    sign integer NOT NULL,
    curr integer,
    amount double precision,
    description character varying(512) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT entry_pkey PRIMARY KEY (mobile, date, serial)
)
