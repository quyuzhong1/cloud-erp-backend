package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 税种基础数据实体
 * @author system
 * @date 2025/01/XX
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_tax_category")
public class TaxCategoryEntity extends BaseEntity<TaxCategoryEntity> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 税种ID（第三方返回的ID，如TF14）
     */
    @TableField("category_id")
    private String categoryId;

    /**
     * 税种描述
     */
    @TableField("descricao")
    private String descricao;

    /**
     * 税种详情JSON（包含icms、ipi、pis、cofins等完整配置信息）
     */
    @TableField("category_detail")
    private String categoryDetail;

    /**
     * 数据MD5值（用于判断数据是否更新，基于category_detail计算）
     */
    @TableField("data_md5")
    private String dataMd5;

    /**
     * 外部系统更新时间（用于同步判断）
     */
    @TableField("external_update_time")
    private LocalDateTime externalUpdateTime;

    /**
     * 启用禁用 true禁用 false启用
     */
    @TableField("disabled")
    private Boolean disabled;
}
