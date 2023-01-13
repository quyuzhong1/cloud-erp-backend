package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * bom 信息表(BomInfo)实体类
 *
 * @author yl
 * @since 2023-01-09 11:31:06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product_bom_info")
public class BomInfoEntity implements Serializable {
    private static final long serialVersionUID = 458249379651802162L;

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
     * 状态
     */
    private Integer state;

    /**
     *
     */
    private Integer sequence;
    /**
     * 版本
     */
    private Integer version;
    /**
     * 备注
     */
    private String remark;


}

