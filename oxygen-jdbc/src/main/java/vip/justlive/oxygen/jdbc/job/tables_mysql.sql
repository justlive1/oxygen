CREATE TABLE oxy_job_info
(
    id            BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    job_key       VARCHAR(255) COMMENT 'job唯一标识',
    description   VARCHAR(255) COMMENT '描述',
    handler_class VARCHAR(255) COMMENT '处理类',
    param         text COMMENT '参数'
) ENGINE = INNODB
  DEFAULT CHARSET = UTF8MB4;

CREATE TABLE oxy_job_trigger
(
    id                  BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    job_key             VARCHAR(255) COMMENT 'job唯一标识',
    trigger_key         VARCHAR(255) COMMENT '触发器唯一标识',
    trigger_type        INT COMMENT '触发器类型：',
    trigger_value       VARCHAR(255) COMMENT '触发器表达式',
    state               INT COMMENT '状态',
    rounds              BIGINT DEFAULT 0 COMMENT '运行次数',
    start_time          BIGINT COMMENT '开始时间',
    end_time            BIGINT COMMENT '结束时间',
    previous_fire_time  BIGINT COMMENT '上一次执行时间',
    next_fire_time      BIGINT COMMENT '下一次执行时间',
    last_completed_time BIGINT COMMENT '最近一次执行完成时间'
) ENGINE = INNODB
  DEFAULT CHARSET = UTF8MB4;

CREATE TABLE oxy_lock
(
    name VARCHAR(255) COMMENT 'lock名称' PRIMARY KEY
) ENGINE = INNODB
  DEFAULT CHARSET = UTF8MB4;

INSERT INTO oxy_lock
values ('trigger_access');