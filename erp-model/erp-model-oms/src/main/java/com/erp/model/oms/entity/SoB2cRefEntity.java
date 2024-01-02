package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;


/**
 * <p>
 * B2C销售订单合并拆分关联表
 * </p>
 *
 * @author Will
 * @since 2023-08-18
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_b2c_ref")
public class SoB2cRefEntity extends BaseEntity<SoB2cRefEntity> {

    /**
    * 关联类型（拆分/合并）
    */
    @TableField("type")
    private String type;
    /**
    * 来源单据id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 目标单据id
    */
    @TableField("target_id")
    private String targetId;

    /**
     * 来源单据明细id
     */
    @TableField("source_detail_id")
    private String sourceDetailId;

    /**
     * 目标单据明细id
     */
    @TableField("target_detail_id")
    private String targetDetailId;



    public static final String TYPE = "type";

    public static final String SOURCE_ID = "source_id";

    public static final String TARGET_ID = "target_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}