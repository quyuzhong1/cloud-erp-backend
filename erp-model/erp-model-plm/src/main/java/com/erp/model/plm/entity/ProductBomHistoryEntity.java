package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * bom 历史表(ProductBomHistory)实体类
 *
 * @author yl
 * @since 2023-01-11 12:26:09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product_bom_history")
public class ProductBomHistoryEntity implements Serializable {
    private static final long serialVersionUID = 618109019196556864L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 创建人
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    /**
     * 更改人
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;
    /**
     * 编号
     */
    private String serialNumber;
    /**
     * 类型combination 组合 single 单品
     */
    private String type;
    /**
     * 版本
     */
    private Integer version;

    /**
     * bom 表id
     */
    private String bomId;


}

