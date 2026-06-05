CREATE TABLE sys_dict_type (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dict_type   VARCHAR(64)  NOT NULL,
    dict_name   VARCHAR(128) NOT NULL,
    dict_source VARCHAR(32)  NOT NULL DEFAULT 'DB',
    is_builtin  TINYINT      NOT NULL DEFAULT 0,
    remark      VARCHAR(512),
    enabled     TINYINT      NOT NULL DEFAULT 1,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_dict_type UNIQUE (dict_type)
);

CREATE TABLE sys_dict_item (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dict_type   VARCHAR(64)  NOT NULL,
    item_code   VARCHAR(256) NOT NULL,
    item_label  VARCHAR(256) NOT NULL,
    item_value  VARCHAR(512),
    sort_order  INT          NOT NULL DEFAULT 0,
    enabled     TINYINT      NOT NULL DEFAULT 1,
    css_class   VARCHAR(64),
    extra_json  TEXT,
    remark      VARCHAR(512),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_dict_type_code UNIQUE (dict_type, item_code)
);

CREATE INDEX idx_dict_item_type ON sys_dict_item (dict_type);

INSERT INTO sys_dict_type (dict_type, dict_name, dict_source, is_builtin, enabled)
VALUES ('gender', '性别', 'DB', 1, 1),
       ('order_status', '订单状态', 'DB', 0, 1);

INSERT INTO sys_dict_item (dict_type, item_code, item_label, item_value, sort_order, enabled)
VALUES ('gender', '1', '男', 'M', 1, 1),
       ('gender', '2', '女', 'F', 2, 1),
       ('order_status', 'CREATED', '已创建', 'CREATED', 1, 1),
       ('order_status', 'FULL_PAID', '已支付', 'PAID', 2, 1);
