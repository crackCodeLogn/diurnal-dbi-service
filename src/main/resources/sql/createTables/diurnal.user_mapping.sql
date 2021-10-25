CREATE TABLE IF NOT EXISTS public.user_mapping(
    mobile bigint NOT NULL,
    email character varying(150) NOT NULL,
    "user" character varying(150) NOT NULL,
    premium_user boolean NOT NULL DEFAULT false,
    hash_cred character varying(512),
    hash_email bigint NOT NULL,

    timestamp_save_cloud_last timestamptz,
    timestamp_save_last timestamptz,
    timestamp_expiry_payment timestamptz,
    timestamp_creation_account timestamptz NOT NULL,

    currency character varying(12),

    CONSTRAINT user_mapping_pkey PRIMARY KEY (hash_email)
);