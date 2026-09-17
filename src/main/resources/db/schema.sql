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

CREATE TABLE IF NOT EXISTS user_preference (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    preferred_colors VARCHAR(128),
    preferred_finish VARCHAR(32),
    preferred_brands VARCHAR(256),
    price_min INT,
    price_max INT,
    disliked_colors VARCHAR(128),
    scenes VARCHAR(256),
    lip_condition VARCHAR(32),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ==================== 订单 & 支付模块 ====================

CREATE TABLE IF NOT EXISTS order_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单编号',
    username VARCHAR(64) NOT NULL COMMENT '下单用户',
    total_amount INT NOT NULL COMMENT '订单总金额（分）',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING_PAYMENT' COMMENT '订单状态',
    remark VARCHAR(512) COMMENT '订单备注',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_title VARCHAR(128) NOT NULL COMMENT '商品名称（冗余）',
    product_image VARCHAR(512) COMMENT '商品图片（冗余）',
    quantity INT NOT NULL DEFAULT 1 COMMENT '购买数量',
    unit_price INT NOT NULL COMMENT '单价（分）'
);

CREATE TABLE IF NOT EXISTS payment_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_no VARCHAR(32) NOT NULL UNIQUE COMMENT '支付流水号',
    order_no VARCHAR(32) NOT NULL COMMENT '关联订单号',
    pay_method VARCHAR(16) NOT NULL COMMENT '支付方式：ALIPAY / WECHAT',
    amount INT NOT NULL COMMENT '支付金额（分）',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '支付状态',
    qr_code_url VARCHAR(1024) COMMENT '支付二维码/链接',
    gateway_trade_no VARCHAR(128) COMMENT '第三方交易号',
    gateway_response TEXT COMMENT '第三方响应原文',
    paid_at TIMESTAMP NULL COMMENT '支付完成时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
