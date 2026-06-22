-- 任务编排实例与节点扩展字段（PostgreSQL）
-- 执行前请确认库：erp-oms

CREATE TABLE IF NOT EXISTS workflow_task_instance (
    id                  VARCHAR(32)  PRIMARY KEY,
    source_type         VARCHAR(64)  NOT NULL DEFAULT '',
    source_id           VARCHAR(32)  NOT NULL DEFAULT '',
    source_code         VARCHAR(128) NOT NULL DEFAULT '',
    status              VARCHAR(32)  NOT NULL DEFAULT 'running',
    current_index       INTEGER      NOT NULL DEFAULT 0,
    total_steps         INTEGER      NOT NULL DEFAULT 0,
    trace_id            VARCHAR(64)  NOT NULL DEFAULT '',
    start_time          TIMESTAMP,
    finish_time         TIMESTAMP,
    last_error          TEXT         NOT NULL DEFAULT '',
    create_user_id      VARCHAR(32)  NOT NULL DEFAULT '',
    create_user_name    VARCHAR(64)  NOT NULL DEFAULT '',
    create_time         TIMESTAMP    NOT NULL DEFAULT now(),
    update_user_id      VARCHAR(32)  NOT NULL DEFAULT '',
    update_user_name    VARCHAR(64)  NOT NULL DEFAULT '',
    update_time         TIMESTAMP    NOT NULL DEFAULT now(),
    version             INTEGER      NOT NULL DEFAULT 0,
    is_deleted          BOOLEAN      NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE workflow_task_instance IS '任务编排实例';
COMMENT ON COLUMN workflow_task_instance.id IS '主键';
COMMENT ON COLUMN workflow_task_instance.source_type IS '来源类型 WorkflowTaskRecordTypeEnum';
COMMENT ON COLUMN workflow_task_instance.source_id IS '来源业务主键';
COMMENT ON COLUMN workflow_task_instance.source_code IS '来源单号';
COMMENT ON COLUMN workflow_task_instance.status IS '实例状态 running/waiting/success/failed/cancelled';
COMMENT ON COLUMN workflow_task_instance.current_index IS '当前执行节点序号';
COMMENT ON COLUMN workflow_task_instance.total_steps IS '总节点数';
COMMENT ON COLUMN workflow_task_instance.trace_id IS '链路追踪 ID';
COMMENT ON COLUMN workflow_task_instance.start_time IS '编排开始时间';
COMMENT ON COLUMN workflow_task_instance.finish_time IS '编排结束时间';
COMMENT ON COLUMN workflow_task_instance.last_error IS '最近错误摘要';
COMMENT ON COLUMN workflow_task_instance.create_user_id IS '创建人 ID';
COMMENT ON COLUMN workflow_task_instance.create_user_name IS '创建人姓名';
COMMENT ON COLUMN workflow_task_instance.create_time IS '创建时间';
COMMENT ON COLUMN workflow_task_instance.update_user_id IS '更新人 ID';
COMMENT ON COLUMN workflow_task_instance.update_user_name IS '更新人姓名';
COMMENT ON COLUMN workflow_task_instance.update_time IS '更新时间';
COMMENT ON COLUMN workflow_task_instance.version IS '乐观锁版本号';
COMMENT ON COLUMN workflow_task_instance.is_deleted IS '是否删除';

CREATE INDEX IF NOT EXISTS idx_wti_source ON workflow_task_instance (source_type, source_id) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_wti_status ON workflow_task_instance (status, update_time) WHERE is_deleted = false;

ALTER TABLE workflow_task_record ADD COLUMN IF NOT EXISTS instance_id VARCHAR(32) NOT NULL DEFAULT '';
ALTER TABLE workflow_task_record ADD COLUMN IF NOT EXISTS target_service VARCHAR(32) NOT NULL DEFAULT '';
ALTER TABLE workflow_task_record ADD COLUMN IF NOT EXISTS target_endpoint VARCHAR(256) NOT NULL DEFAULT '';
ALTER TABLE workflow_task_record ADD COLUMN IF NOT EXISTS feign_duration_ms BIGINT;
ALTER TABLE workflow_task_record ADD COLUMN IF NOT EXISTS error_source VARCHAR(32) NOT NULL DEFAULT '';
ALTER TABLE workflow_task_record ADD COLUMN IF NOT EXISTS start_time TIMESTAMP;
ALTER TABLE workflow_task_record ADD COLUMN IF NOT EXISTS end_time TIMESTAMP;

COMMENT ON COLUMN workflow_task_record.instance_id IS '编排实例 ID';
COMMENT ON COLUMN workflow_task_record.target_service IS '目标微服务编码';
COMMENT ON COLUMN workflow_task_record.target_endpoint IS '目标接口标识';
COMMENT ON COLUMN workflow_task_record.feign_duration_ms IS '跨服务调用耗时毫秒';
COMMENT ON COLUMN workflow_task_record.error_source IS '错误来源 orchestrator/remote';
COMMENT ON COLUMN workflow_task_record.start_time IS '节点开始执行时间';
COMMENT ON COLUMN workflow_task_record.end_time IS '节点结束执行时间';

CREATE INDEX IF NOT EXISTS idx_wtr_instance ON workflow_task_record (instance_id, index) WHERE is_deleted = false;

-- 若已执行旧版 DDL（started_at/finished_at），可手工迁移：
-- ALTER TABLE workflow_task_instance RENAME COLUMN started_at TO start_time;
-- ALTER TABLE workflow_task_instance RENAME COLUMN finished_at TO finish_time;
-- ALTER TABLE workflow_task_record RENAME COLUMN started_at TO start_time;
-- ALTER TABLE workflow_task_record RENAME COLUMN finished_at TO end_time;
