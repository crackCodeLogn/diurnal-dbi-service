CREATE TABLE IF NOT EXISTS public.entries
(
    mobile bigint NOT NULL,
    date integer NOT NULL,
    sign character(2) COLLATE pg_catalog."default" NOT NULL,
    curr character(2) COLLATE pg_catalog."default",
    amount double precision,
    "desc" character varying(200) COLLATE pg_catalog."default" NOT NULL,
    serial integer NOT NULL,
    CONSTRAINT entries_pkey PRIMARY KEY (mobile, date, serial)
)