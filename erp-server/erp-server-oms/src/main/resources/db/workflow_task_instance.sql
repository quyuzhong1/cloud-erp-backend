-- 任务编排实例与节点扩展字段（PostgreSQL）
-- 执行前请确认库：erp-oms

CREATE TABLE IF NOT EXISTS workflow_task_instance (
    id                  VARCHAR(32)  PRIMARY KEY,
    source_type         VARCHAR(64)  NOT NULL DEFAULT '',
    source_id           VARCHAR(32)  NOT NULL DEFAULT '',
    source_code         VARCHAR(128) NOT NULL DEFAULT '',
    status              VARCHAR(32)  NOT NULL DEFAULT 'running',
    current_index       INT          NOT NULL DEFAULT 0,
    total_steps         INT          NOT NULL DEFAULT 0,
    trace_id            VARCHAR(64)  NOT NULL DEFAULT '',
    started_at          TIMESTAMP,
    finished_at         TIMESTAMP,
    last_error          TEXT         NOT NULL DEFAULT '',
    create_user_id      VARCHAR(32)  NOT NULL DEFAULT '0',
    create_user_name    VARCHAR(64)  NOT NULL DEFAULT 'system',
    create_time         TIMESTAMP    NOT NULL DEFAULT now(),
    update_user_id      VARCHAR(32)  NOT NULL DEFAULT '0',
    update_user_name    VARCHAR(64)  NOT NULL DEFAULT 'system',
    update_time         TIMESTAMP    NOT NULL DEFAULT now(),
    version             INT          NOT NULL DEFAULT 0,
    is_deleted          BOOLEAN      NOT NULL DEFAULT false
);

COMMENT ON TABLE workflow_task_instance IS '任务编排实例';
COMMENT ON COLUMN workflow_task_instance.source_type IS '来源类型 WorkflowTaskRecordTypeEnum';
COMMENT ON COLUMN workflow_task_instance.status IS 'running/waiting/success/failed/cancelled';

CREATE INDEX IF NOT EXISTS idx_wti_source ON workflow_task_instance (source_type, source_id) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_wti_status ON workflow_task_instance (status, update_time) WHERE is_deleted = false;

ALTER TABLE workflow_task_record ADD COLUMN IF NOT EXISTS instance_id VARCHAR(32) NOT NULL DEFAULT '';
ALTER TABLE workflow_task_record ADD COLUMN IF NOT EXISTS target_service VARCHAR(32) NOT NULL DEFAULT '';
ALTER TABLE workflow_task_record ADD COLUMN IF NOT EXISTS target_endpoint VARCHAR(256) NOT NULL DEFAULT '';
ALTER TABLE workflow_task_record ADD COLUMN IF NOT EXISTS feign_duration_ms BIGINT;
ALTER TABLE workflow_task_record ADD COLUMN IF NOT EXISTS error_source VARCHAR(32) NOT NULL DEFAULT '';
ALTER TABLE workflow_task_record ADD COLUMN IF NOT EXISTS started_at TIMESTAMP;
ALTER TABLE workflow_task_record ADD COLUMN IF NOT EXISTS finished_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_wtr_instance ON workflow_task_record (instance_id, index) WHERE is_deleted = false;

-- 历史数据回填实例（可选，部署后执行）
-- INSERT INTO workflow_task_instance (...) SELECT DISTINCT source_type, source_id ...
