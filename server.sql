CREATE DATABASE IF NOT EXISTS `test_platform`;
USE `test_platform`;

CREATE TABLE Users
(
    id      INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username     VARCHAR(50)          NOT NULL UNIQUE COMMENT '用户名',
    password     VARCHAR(255)         NOT NULL COMMENT '用户密码',
    avatar_url   VARCHAR(255) COMMENT '用户头像地址',
    nickname     VARCHAR(50) COMMENT '用户昵称',
    is_active    TINYINT(1) DEFAULT 1 COMMENT '账户是否激活，1表示激活，0表示未激活',
    is_deleted   TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标志，0表示未删除，1表示已删除',
    version      INT        DEFAULT 0 NOT NULL COMMENT '乐观锁版本号',
    created_time TIMESTAMP            NOT NULL COMMENT '创建时间',
    updated_time TIMESTAMP            NOT NULL COMMENT '更新时间',

    UNIQUE (username, is_deleted) COMMENT '唯一约束'
) comment '用户表';

CREATE INDEX idx_users_is_deleted_is_active ON Users (is_deleted, is_active); -- 组合索引，方便查询有效且未删除用户

insert into Users (username, password, avatar_url, nickname, created_time, updated_time)
values ('admin', '123456', null, '管理员', '2026-06-06 23:42:42', '2026-06-06 23:42:42');

CREATE TABLE environment_variables
(
    id       INT AUTO_INCREMENT PRIMARY KEY COMMENT '环境ID',
    name         VARCHAR(100)         NOT NULL COMMENT '环境名称',
    variables    JSON COMMENT '环境级别的通用变量',
    is_deleted   TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标志，0表示未删除，1表示已删除',
    version      INT        DEFAULT 0 NOT NULL COMMENT '乐观锁版本号',
    create_user  int                  not null comment '创建用户ID',
    created_time TIMESTAMP            NOT NULL COMMENT '创建时间',
    update_user  int                  not null comment '更新用户ID',
    updated_time TIMESTAMP            NOT NULL COMMENT '更新时间'
) comment '环境变量表';

CREATE INDEX idx_environment_variables_create_user_is_deleted ON environment_variables (create_user, is_deleted); -- 方便查询某个用户创建的未删除环境

CREATE TABLE Interfaces
(
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '接口ID',
    name         VARCHAR(100)         NOT NULL COMMENT '接口名称',
    method       VARCHAR(10)          NOT NULL COMMENT 'HTTP请求方法',
    path         VARCHAR(255)         NOT NULL COMMENT '接口路径',
    description  TEXT COMMENT '接口描述',
    is_deleted   TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标志，0表示未删除，1表示已删除',
    version      INT        DEFAULT 0 NOT NULL COMMENT '乐观锁版本号',
    created_time TIMESTAMP            NOT NULL COMMENT '创建时间',
    create_user  int                  not null comment '创建用户ID',
    update_user  int                  not null comment '更新用户ID',
    updated_time TIMESTAMP            NOT NULL COMMENT '更新时间'
) comment '接口表';

CREATE INDEX idx_interfaces_create_user_is_deleted ON Interfaces (create_user, is_deleted); -- 方便查询某个用户创建的未删除接口

CREATE TABLE Interface_testcases
(
    id                           INT AUTO_INCREMENT PRIMARY KEY COMMENT '接口测试用例ID',
    interface_id                      INT                  NOT NULL COMMENT '关联的接口ID',
    name                              VARCHAR(100)         NOT NULL COMMENT '接口测试用例名称',
    priority                          INT        DEFAULT 0 COMMENT '用例优先级，0-高, 1-中, 2-低',
    host                              varchar(100)         not null comment '域名地址',
    request_method                    VARCHAR(10)          NOT NULL COMMENT '请求方法',
    request_body_type                 VARCHAR(30) COMMENT '请求体类型',
    request_path_variables            JSON COMMENT '请求路径参数',
    request_query_params              JSON COMMENT '请求查询参数',
    request_headers                   JSON COMMENT '特定于此用例的请求头',
    request_body                      JSON COMMENT '特定于此用例的请求体数据',
    pre_request_script                TEXT COMMENT '请求前置脚本',
    post_request_script               TEXT COMMENT '请求后置脚本/断言脚本',
    is_deleted                        TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标志，0表示未删除，1表示已删除',
    version                           INT        DEFAULT 0 NOT NULL COMMENT '乐观锁版本号',
    create_user                       int                  not null comment '创建用户ID',
    created_time                      TIMESTAMP            NOT NULL COMMENT '创建时间',
    update_user                       int                  not null comment '更新用户ID',
    updated_time                      TIMESTAMP            NOT NULL COMMENT '更新时间'
) comment '接口用例表';

CREATE INDEX idx_testcases_interface_id_is_deleted ON Interface_testcases (interface_id, is_deleted); -- 方便查询某个接口下未删除的用例
CREATE INDEX idx_testcases_create_user_is_deleted ON Interface_testcases (create_user, is_deleted); -- 方便查询某个用户创建的未删除用例

CREATE TABLE testcase_environments
(
    id                  INT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    testcase_id         INT              NOT NULL COMMENT '关联的接口测试用例ID',
    environment_id      INT              NOT NULL COMMENT '关联的环境ID',
    user_id             INT              NOT NULL COMMENT '创建关联的用户ID'
) COMMENT '测试用例与环境的关联表';