CREATE TABLE IF NOT EXISTS public.user_mapping
(
    mobile bigint NOT NULL,
    email character varying(150) COLLATE pg_catalog."default" NOT NULL,
    "user" character varying(150) COLLATE pg_catalog."default" NOT NULL,
    power_user boolean NOT NULL DEFAULT false,
    hash_cred character varying(512) COLLATE pg_catalog."default",
    hash_email bigint NOT NULL,
    CONSTRAINT user_mapping_pkey PRIMARY KEY (hash_email)
)