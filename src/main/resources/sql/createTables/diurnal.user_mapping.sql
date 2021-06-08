CREATE TABLE IF NOT EXISTS public.user_mapping
(
    mobile bigint NOT NULL,
    email character varying(150) COLLATE pg_catalog."default" NOT NULL,
    "user" character varying(150) COLLATE pg_catalog."default" NOT NULL,
    premium_user boolean NOT NULL DEFAULT false,
    hash_cred character varying(512) COLLATE pg_catalog."default",
    hash_email bigint NOT NULL,

    timestamp_save_cloud_last bigint,
    timestamp_save_last bigint,
    timestamp_expiry_payment bigint,
    timestamp_creation_account bigint NOT NULL,

    currency character varying(12) COLLATE pg_catalog."default",

    CONSTRAINT user_mapping_pkey PRIMARY KEY (hash_email)
)