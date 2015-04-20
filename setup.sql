CREATE EXTENSION "uuid-ossp";
DROP TABLE IF EXISTS "messages";
DROP TABLE IF EXISTS "users";

CREATE TABLE users (login varchar(20) NOT NULL, password_digest varchar(60) NOT NULL,  PRIMARY KEY (login));
ALTER TABLE users ADD CONSTRAINT login_format CHECK (login ~ '^[a-z][a-z0-9]+$');
CREATE INDEX users_login_idx ON users (login);

CREATE TABLE messages (
    id uuid NOT NULL DEFAULT uuid_generate_v4(), 
    login varchar(20) NOT NULL, 
    message varchar(140) NOT NULL, 
    created_at timestamp NOT NULL DEFAULT now(),
    PRIMARY KEY (id) );
CREATE INDEX messages_created_at_idx ON messages (created_at);
CREATE INDEX messages_login_idx ON messages (login);

ALTER TABLE messages ADD CONSTRAINT fkey_users_login FOREIGN KEY (login) REFERENCES users (login);

