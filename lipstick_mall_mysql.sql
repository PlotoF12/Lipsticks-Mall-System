-- ============================================================
-- 口红商城系统 (Lipstick Mall) - MySQL 数据库建表语句
-- 数据库字符集: utf8mb4  排序规则: utf8mb4_unicode_ci
-- 适配 Spring Boot 3 + MyBatis Plus + Spring Security + JWT
-- ============================================================

CREATE DATABASE IF NOT EXISTS lipstick_mall
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE lipstick_mall;

-- -----------------------------------------------------------
-- 1. 用户账户表
--    存储登录凭证与角色，配合 Spring Security + JWT 使用
-- -----------------------------------------------------------
DROP TABLE IF EXISTS user_account;
CREATE TABLE user_account (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username      VARCHAR(64)  NOT NULL                COMMENT '用户名，登录账号',
    password_hash VARCHAR(255) NOT NULL                COMMENT 'BCrypt 加密后的密码',
    role          VARCHAR(16)  NOT NULL DEFAULT 'USER' COMMENT '角色: USER / ADMIN',
    enabled       TINYINT(1)   NOT NULL DEFAULT 1      COMMENT '账户是否启用: 1-启用 0-禁用',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账户表';

-- -----------------------------------------------------------
-- 2. 用户画像表
--    存储用户肤质信息，用于个性化推荐算法
-- -----------------------------------------------------------
DROP TABLE IF EXISTS user_profile;
CREATE TABLE user_profile (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    username   VARCHAR(64) NOT NULL                COMMENT '用户名，关联 user_account.username',
    gender     VARCHAR(16)                         COMMENT '性别: male / female',
    skin_tone  VARCHAR(32)                         COMMENT '肤色色调: warm(暖调) / cool(冷调) / neutral(中性)',
    skin_type  VARCHAR(32)                         COMMENT '肤质: oily(油性) / dry(干性) / normal(中性) / combination(混合)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    CONSTRAINT fk_profile_username FOREIGN KEY (username) REFERENCES user_account (username) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户画像表';

-- -----------------------------------------------------------
-- 3. 口红商品表
--    存储口红产品全部信息，支撑商城展示、推荐、可视化、试妆
-- -----------------------------------------------------------
DROP TABLE IF EXISTS lipstick_product;
CREATE TABLE lipstick_product (
    id                 BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    title              VARCHAR(128)  NOT NULL                COMMENT '商品标题，如"Dior 999 经典正红"',
    brand              VARCHAR(64)                           COMMENT '品牌: Dior / YSL / Armani / Chanel / MAC / Tom Ford / NARS',
    shade              VARCHAR(64)                           COMMENT '色号: 如 999 / 1966 / 405 / Chili',
    color_hex          VARCHAR(16)                           COMMENT '颜色十六进制值: 如 #B3123F',
    category           VARCHAR(32)                           COMMENT '色系分类: red / red_brown / tomato / bean_paste / pink / rose / rose_brown / nude',
    finish_type        VARCHAR(32)                           COMMENT '质地: matte(哑光) / gloss(滋润) / satin(缎面)',
    detail             VARCHAR(2000)                         COMMENT '商品详情描述',
    price              INT           NOT NULL                COMMENT '价格，单位: 人民币(元)',
    stock              INT           NOT NULL DEFAULT 0      COMMENT '库存数量',
    on_sale            TINYINT(1)    NOT NULL DEFAULT 1      COMMENT '是否上架: 1-上架 0-下架',
    suitable_skin_tone VARCHAR(64)                           COMMENT '适合肤色，逗号分隔: warm,neutral,cool',
    suitable_gender    VARCHAR(32)                           COMMENT '适合性别，逗号分隔: male,female',
    scene              VARCHAR(128)                          COMMENT '适用场景，逗号分隔: 日常,约会,职场,派对',
    image_url          VARCHAR(512)                          COMMENT '商品图片路径',
    PRIMARY KEY (id),
    KEY idx_brand (brand),
    KEY idx_category (category),
    KEY idx_on_sale (on_sale),
    KEY idx_price (price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='口红商品表';

-- -----------------------------------------------------------
-- 4. 试妆记录表
--    记录用户上传照片试妆的历史，便于回看与管理
-- -----------------------------------------------------------
DROP TABLE IF EXISTS tryon_record;
CREATE TABLE tryon_record (
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username          VARCHAR(64)  NOT NULL                COMMENT '用户名',
    product_id        BIGINT       NOT NULL                COMMENT '试妆口红商品ID',
    original_filename VARCHAR(256)                         COMMENT '用户上传的原始图片文件名',
    result_filename   VARCHAR(256)                         COMMENT '试妆结果图片文件名',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_username (username),
    KEY idx_product_id (product_id),
    KEY idx_created_at (created_at),
    CONSTRAINT fk_tryon_username FOREIGN KEY (username) REFERENCES user_account (username) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_tryon_product  FOREIGN KEY (product_id) REFERENCES lipstick_product (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='试妆记录表';

-- ============================================================
-- 初始数据 (可选)
-- ============================================================

-- 管理员账户 (密码: Admin@123，BCrypt 加密)
INSERT INTO user_account (username, password_hash, role, enabled) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 1);

-- 普通用户账户 (密码: User@123，BCrypt 加密)
INSERT INTO user_account (username, password_hash, role, enabled) VALUES
('user',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', 1),
('xiaoming', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', 1),
('xiaohong', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', 1);

-- 用户画像
INSERT INTO user_profile (username, gender, skin_tone, skin_type) VALUES
('admin',    NULL,     NULL,      NULL),
('user',     'female', 'neutral', 'normal'),
('xiaoming', 'male',   'warm',    'oily'),
('xiaohong', 'female', 'cool',    'dry');

-- 口红商品数据
INSERT INTO lipstick_product (title, brand, shade, color_hex, category, finish_type, detail, price, stock, on_sale, suitable_skin_tone, suitable_gender, scene, image_url) VALUES
('Dior 999 经典正红',       'Dior',      '999',    '#B3123F', 'red',         'matte', '经典正红色，气场全开。Dior最经典的正红色号，适合所有肤色，职场和重要场合的不二之选。', 350, 50, 1, 'warm,neutral,cool', 'female', '职场,派对,重要场合', '/images/dior-999.jpg'),
('YSL 小金条 1966',         'YSL',       '1966',   '#8B2252', 'red_brown',   'matte', '复古红棕色，高级质感。YSL小金条系列热门色号，丝绒哑光质地，秋冬必备。', 350, 45, 1, 'warm,neutral', 'female', '日常,约会,秋冬', '/images/ysl-1966.jpg'),
('Armani 红管 405',         'Armani',    '405',    '#D94F4F', 'tomato',      'gloss', '番茄红色，显白百搭。Armani红管最火色号，滋润质地，素颜也能轻松驾驭。', 340, 60, 1, 'warm,neutral,cool', 'female', '日常,素颜,百搭', '/images/armani-405.jpg'),
('Chanel 58 玫瑰豆沙',      'Chanel',    '58',     '#C88088', 'bean_paste',  'satin', '温柔豆沙色，日常首选。Chanel经典豆沙色号，缎面质地滋润不拔干，适合日常通勤。', 320, 55, 1, 'neutral,cool', 'female', '日常,通勤,温柔', '/images/chanel-58.jpg'),
('MAC Chili 小辣椒',        'MAC',       'Chili',  '#B5453A', 'red_brown',   'matte', '经典红棕色，显白利器。MAC最经典色号之一，暖调红棕，黄皮友好，四季可用。', 180, 100, 1, 'warm,neutral', 'female', '日常,秋冬,显白', '/images/mac-chili.jpg'),
('Tom Ford 16 番茄红',      'Tom Ford',  '16',     '#CC4444', 'tomato',      'gloss', '鲜亮番茄红，活力满满。TF黑管经典色号，明亮番茄红色，减龄显气色。', 460, 30, 1, 'warm,neutral,cool', 'female', '约会,派对,春夏', '/images/tf-16.jpg'),
('NARS DV 唇膏笔',          'NARS',      'DV',     '#B87070', 'bean_paste',  'satin', '气质豆沙色，温柔知性。NARS唇膏笔经典色号，哑光缎面质地，温柔又高级。', 280, 40, 1, 'neutral,cool', 'female', '日常,通勤,气质', '/images/nars-dv.jpg'),
('YSL 方管 12 玫瑰粉',      'YSL',       '12',     '#D4888C', 'pink',        'gloss', '甜美玫瑰粉，少女感十足。YSL方管经典粉色号，滋润质地，打造甜美少女妆。', 300, 35, 1, 'cool,neutral', 'female', '约会,春夏,少女', '/images/ysl-12.jpg'),
('Dior 772 玫瑰棕',         'Dior',      '772',    '#9B6B6B', 'rose',        'matte', '气质玫瑰棕，知性优雅。Dior哑光系列，玫瑰棕色调，适合轻熟龄女性。', 350, 40, 1, 'neutral,cool', 'female', '职场,日常,知性', '/images/dior-772.jpg'),
('MAC 316 摩卡玫瑰',        'MAC',       '316',    '#A86060', 'rose_brown',  'satin', '温柔摩卡玫瑰，日常百搭。MAC摩卡系列，低饱和玫瑰棕，素颜也能涂。', 180, 80, 1, 'warm,neutral', 'female', '日常,素颜,通勤', '/images/mac-316.jpg'),
('Chanel 99 深红',          'Chanel',    '99',     '#7A1A2E', 'red',         'matte', '浓郁深红，女王气场。深红色调气场全开，适合需要气场的场合。', 350, 40, 1, 'warm,neutral,cool', 'female', '派对,晚宴,气场', '/images/chanel-99.jpg'),
('Armani 501 玫瑰豆沙',     'Armani',    '501',    '#C08080', 'bean_paste',  'gloss', '滋润豆沙色，温柔满载。Armani红管经典豆沙色，质地滋润，日常美唇。', 340, 75, 1, 'neutral,cool', 'female', '日常,通勤,约会', '/images/armani-501.jpg'),
('MAC Taupe 摩卡裸棕',      'MAC',       'Taupe',  '#8B6B6B', 'nude',        'matte', '低调裸棕色，男性友好。低饱和摩卡棕色，自然提气色不突兀，男性日常使用首选。', 170, 80, 1, 'warm,neutral', 'male', '日常,通勤,男性', '/images/mac-taupe.jpg'),
('Dior 421 裸粉豆沙',       'Dior',      '421',    '#C8A0A0', 'nude',        'gloss', '自然裸粉，低调提气色。低饱和裸粉色调，男性使用自然不违和，女性素颜也好搭。', 310, 55, 1, 'neutral,cool', 'male,female', '日常,通勤,素颜', '/images/dior-421.jpg'),
('YSL 黑管 83 蜜桃奶茶',    'YSL',       '83',     '#D4A090', 'nude',        'gloss', '温暖蜜桃奶茶，自然好气色。低饱和蜜桃色调，男女通用，日常通勤阶段百搭。', 300, 60, 1, 'warm,neutral', 'male,female', '日常,通勤,素颜', '/images/ysl-83.jpg'),
('Tom Ford 25 裸玫瑰',      'Tom Ford',  '25',     '#C09090', 'nude',        'satin', '高级裸玫瑰，低调奢华。玫瑰调裸玫瑰色，男性使用自然有型，女性通勤首选。', 460, 35, 1, 'neutral,cool', 'male,female', '通勤,日常,低调', '/images/tf-25.jpg');
