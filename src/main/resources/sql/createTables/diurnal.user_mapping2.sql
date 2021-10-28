CREATE TABLE IF NOT EXISTS user_mapping2(
    mobile bigint NOT NULL,
    email character varying(150) NOT NULL,
    "user" character varying(150) NOT NULL,
    premium_user boolean NOT NULL DEFAULT false,
    hash_cred character varying(512),
    hash_email bigint PRIMARY KEY,

    timestamp_save_cloud_last timestamptz,
    timestamp_save_last timestamptz,
    timestamp_expiry_payment timestamptz,
    timestamp_creation_account timestamptz NOT NULL,

    currency character varying(12)
);