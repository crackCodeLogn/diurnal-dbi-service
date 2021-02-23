CREATE TABLE IF NOT EXISTS public.user_mapping
(
    mobile bigint NOT NULL,
    "user" character varying(50) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT user_mapping_pkey PRIMARY KEY (mobile)
)