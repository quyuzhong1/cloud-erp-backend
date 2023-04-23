
-- ----------------------------
-- Table structure for msg_config
-- ----------------------------
DROP TABLE IF EXISTS "public"."msg_config";
CREATE TABLE "public"."msg_config" (
  "id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "channel" varchar(100) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "msg_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "send_flag" bool NOT NULL DEFAULT true,
  "retry_times" int4 NOT NULL DEFAULT 0,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "create_user_id" char(19) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::bpchar,
  "create_user_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_user_id" char(19) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::bpchar,
  "update_user_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "extend1" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "is_deleted" bool NOT NULL DEFAULT false,
  "version" int4 NOT NULL DEFAULT 0
)
;
COMMENT ON COLUMN "public"."msg_config"."id" IS '消息来源key';
COMMENT ON COLUMN "public"."msg_config"."name" IS '消息来源名称';
COMMENT ON COLUMN "public"."msg_config"."channel" IS '消息渠道，多个用英文逗号隔开';
COMMENT ON COLUMN "public"."msg_config"."msg_type" IS '消息类型';
COMMENT ON COLUMN "public"."msg_config"."send_flag" IS '发送标识，true-发送；false-不发送';
COMMENT ON COLUMN "public"."msg_config"."retry_times" IS '发送失败最大重试次数';
COMMENT ON COLUMN "public"."msg_config"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."msg_config"."create_user_id" IS '创建人id';
COMMENT ON COLUMN "public"."msg_config"."create_user_name" IS '创建人';
COMMENT ON COLUMN "public"."msg_config"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."msg_config"."update_user_id" IS '更新人id';
COMMENT ON COLUMN "public"."msg_config"."update_user_name" IS '更新人名称';
COMMENT ON COLUMN "public"."msg_config"."extend1" IS '扩展字段1';
COMMENT ON TABLE "public"."msg_config" IS '消息配置表';

-- ----------------------------
-- Records of msg_config
-- ----------------------------
INSERT INTO "public"."msg_config" VALUES ('SCM_TASK', '供应链系统任务通知', 'feishu,mail', 'action_card', 't', 0, '2023-04-21 03:50:49.198019', '                   ', '', '2023-04-21 03:50:49.198019', '                   ', '', '', 'f', 0);
INSERT INTO "public"."msg_config" VALUES ('PLM_TASK', '产品计划系统任务通知', 'feishu', 'action_card', 't', 0, '2023-04-23 01:13:17.371557', '                   ', '', '2023-04-23 01:13:17.371557', '                   ', '', '', 'f', 0);

-- ----------------------------
-- Primary Key structure for table msg_config
-- ----------------------------
ALTER TABLE "public"."msg_config" ADD CONSTRAINT "msg_conf_pkey" PRIMARY KEY ("id");


/*
 Navicat PostgreSQL Data Transfer

 Source Server         : 开发
 Source Server Type    : PostgreSQL
 Source Server Version : 120006 (120006)
 Source Host           : 172.16.100.12:5432
 Source Catalog        : erp-sys
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 120006 (120006)
 File Encoding         : 65001

 Date: 23/04/2023 09:25:28
*/


-- ----------------------------
-- Table structure for msg_channel_config
-- ----------------------------
DROP TABLE IF EXISTS "public"."msg_channel_config";
CREATE TABLE "public"."msg_channel_config" (
  "id" char(19) COLLATE "pg_catalog"."default" NOT NULL,
  "msg_config_id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "channel_code" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "channel_app_code" varchar(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "send_flag" bool NOT NULL DEFAULT true,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "create_user_id" char(19) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::bpchar,
  "create_user_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_user_id" char(19) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::bpchar,
  "update_user_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "extend1" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "is_deleted" bool,
  "version" int4
)
;
COMMENT ON COLUMN "public"."msg_channel_config"."id" IS '主键id';
COMMENT ON COLUMN "public"."msg_channel_config"."msg_config_id" IS '消息来源key';
COMMENT ON COLUMN "public"."msg_channel_config"."channel_code" IS '消息渠道编码';
COMMENT ON COLUMN "public"."msg_channel_config"."channel_app_code" IS '渠道应用编码';
COMMENT ON COLUMN "public"."msg_channel_config"."send_flag" IS '发送标识，true-发送；false-不发送';
COMMENT ON COLUMN "public"."msg_channel_config"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."msg_channel_config"."create_user_id" IS '创建人id';
COMMENT ON COLUMN "public"."msg_channel_config"."create_user_name" IS '创建人';
COMMENT ON COLUMN "public"."msg_channel_config"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."msg_channel_config"."update_user_id" IS '更新人id';
COMMENT ON COLUMN "public"."msg_channel_config"."update_user_name" IS '更新人名称';
COMMENT ON COLUMN "public"."msg_channel_config"."extend1" IS '扩展字段1';
COMMENT ON TABLE "public"."msg_channel_config" IS '消息渠道应用配置表';

-- ----------------------------
-- Records of msg_channel_config
-- ----------------------------
INSERT INTO "public"."msg_channel_config" VALUES ('1635216772825219074', 'SCM_TASK', 'feishu', 'plm', 't', '2023-04-21 07:41:13.746689', '                   ', '', '2023-04-21 07:41:13.746689', '                   ', '', '', 'f', NULL);
INSERT INTO "public"."msg_channel_config" VALUES ('1635216772825219075', 'PLM_TASK', 'feishu', 'plm', 't', '2023-04-21 07:41:13.746689', '                   ', '', '2023-04-21 07:41:13.746689', '                   ', '', '', 'f', NULL);

-- ----------------------------
-- Primary Key structure for table msg_channel_config
-- ----------------------------
ALTER TABLE "public"."msg_channel_config" ADD CONSTRAINT "msg_channel_config_pkey" PRIMARY KEY ("id");