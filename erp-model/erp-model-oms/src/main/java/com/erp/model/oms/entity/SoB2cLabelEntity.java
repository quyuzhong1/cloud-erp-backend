package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 订单标签，面单表
 * </p>
 *
 * @author Luo_WG
 * @since 2024-04-18
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_b2c_label")
public class SoB2cLabelEntity extends BaseEntity<SoB2cLabelEntity> {

    /**
    * 订单主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
     * 跨境物流面单url
     */
    @TableField("cross_label_url")
    private String crossLabelUrl;
    /**
     * 平台物流面单url
     */
    @TableField("logistics_label_url")
    private String logisticsLabelUrl;

    /**
     * 来源类型
     */
    @TableField("source_type")
    private String sourceType;

    public static final String MAIN_ID = "main_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}