UPDATE users
SET email = LOWER(TRIM(email));

-- Impedir emails duplicados
ALTER TABLE users
    ADD CONSTRAINT uk_users_email UNIQUE (email);
