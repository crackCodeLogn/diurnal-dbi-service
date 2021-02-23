CREATE TABLE IF NOT EXISTS public.title_mapping
(
    mobile bigint NOT NULL,
    date integer NOT NULL,
    title character varying(25) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT title_mapping_pkey PRIMARY KEY (mobile, date)
)