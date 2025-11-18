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
 * B2B订单面单表
 * </p>
 *
 * @author zdy
 * @since 2025-02-24
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_label")
public class SoLabelEntity extends BaseEntity<SoLabelEntity> {

    /**
    * 订单主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
     * 平台物流面单URL
     */
    @TableField("logistics_label_url")
    private String logisticsLabelUrl;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;


    public static final String MAIN_ID = "main_id";


    public static final String SOURCE_TYPE = "source_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}