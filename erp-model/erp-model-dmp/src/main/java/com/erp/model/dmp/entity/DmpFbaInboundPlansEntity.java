package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * FBA InboundPlans 列表表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_fba_inbound_plans")
public class DmpFbaInboundPlansEntity extends BaseEntity<DmpFbaInboundPlansEntity> {

    /**
     * 入库计划ID
     */
    @TableField("inbound_plan_id")
    private String inboundPlanId;

    /**
     * 计划名称
     */
    @TableField("name")
    private String name;

    /**
     * 计划状态
     */
    @TableField("status")
    private String status;

    /**
     * 平台创建时间（ISO字符串）
     */
    @TableField("created_at_platform")
    private String createdAtPlatform;

    /**
     * 平台更新时间（ISO字符串）
     */
    @TableField("last_updated_at_platform")
    private String lastUpdatedAtPlatform;

    /**
     * 站点ID集合JSON
     */
    @TableField("marketplace_ids_json")
    private String marketplaceIdsJson;

    /**
     * 发货地址JSON
     */
    @TableField("source_address_json")
    private String sourceAddressJson;

    /**
     * ===== DMP框架跟踪字段（非接口业务字段）=====
     */

    /**
     * 输入任务ID
     */
    @TableField("input_task_id")
    private String inputTaskId;

    /**
     * 亚马逊账号代号
     */
    @TableField("platform_shop_code")
    private String platformShopCode;

    /**
     * 任务转换ID
     */
    @TableField("convert_id")
    private String convertId;

    /**
     * 店铺ID
     */
    @TableField("next_level_id")
    private String nextLevelId;

    /**
     * 任务来源唯一加密代号
     */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;

    /**
     * 任务数据加密代号
     */
    @TableField("data_encrypt")
    private String dataEncrypt;
}

