CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(16) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS user_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    gender VARCHAR(16),
    skin_tone VARCHAR(32),
    skin_type VARCHAR(32)
);

CREATE TABLE IF NOT EXISTS lipstick_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(128) NOT NULL,
    brand VARCHAR(64),
    shade VARCHAR(64),
    color_hex VARCHAR(16),
    category VARCHAR(32),
    finish_type VARCHAR(32),
    detail VARCHAR(2000),
    price INT NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    on_sale BOOLEAN NOT NULL DEFAULT TRUE,
    suitable_skin_tone VARCHAR(64),
    suitable_gender VARCHAR(32),
    scene VARCHAR(128),
    image_url VARCHAR(512)
);

CREATE TABLE IF NOT EXISTS tryon_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    product_id BIGINT NOT NULL,
    original_filename VARCHAR(256),
    result_filename VARCHAR(256),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
