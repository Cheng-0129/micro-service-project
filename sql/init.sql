-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    age INT,
    email VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- 订单表
CREATE TABLE IF NOT EXISTS t_order (
    id BIGSERIAL PRIMARY KEY,
    order_no BIGINT NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    num INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- 库存表
CREATE TABLE IF NOT EXISTS t_stock (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    product_name VARCHAR(100) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    locked_count INT NOT NULL DEFAULT 0,
    price DECIMAL(10,2),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Seata AT模式回滚日志表
CREATE TABLE IF NOT EXISTS undo_log (
    id BIGSERIAL PRIMARY KEY,
    branch_id BIGINT NOT NULL,
    xid VARCHAR(128) NOT NULL,
    context VARCHAR(128) NOT NULL,
    rollback_info BYTEA NOT NULL,
    log_status INT NOT NULL,
    log_created TIMESTAMP NOT NULL,
    log_modified TIMESTAMP NOT NULL,
    CONSTRAINT ux_undo_log UNIQUE (xid, branch_id)
    );

-- 防重放Nonce记录表
CREATE TABLE IF NOT EXISTS t_nonce_record (
    id BIGSERIAL PRIMARY KEY,
    nonce VARCHAR(64) NOT NULL UNIQUE,
    expire_time TIMESTAMP NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
CREATE INDEX IF NOT EXISTS idx_nonce_expire ON t_nonce_record(expire_time);

-- 业务ID生成器表
CREATE TABLE IF NOT EXISTS biz_id_counter (
    id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(50) NOT NULL UNIQUE,
    current_max_id BIGINT NOT NULL DEFAULT 0
    );

-- 初始化三个业务表的起始 ID（幂等）
INSERT INTO biz_id_counter (table_name, current_max_id)
VALUES ('t_user', 10000), ('t_order', 10000), ('t_stock', 10000)
    ON CONFLICT (table_name) DO NOTHING;