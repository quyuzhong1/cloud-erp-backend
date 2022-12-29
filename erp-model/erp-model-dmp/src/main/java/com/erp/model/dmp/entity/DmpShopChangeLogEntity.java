package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/15 14:24
 */
@Data
@NoArgsConstructor
@TableName(value ="dmp_shop_change_log")
public class DmpShopChangeLogEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name", fill = FieldFill.INSERT)
    private String createUserName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name", fill = FieldFill.INSERT_UPDATE)
    private String updateUserName;

    /**
     * 修改人id
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 负责人id
     */
    @TableField(value = "charge_id")
    private String chargeId;

    /**
     * 负责人名称
     */
    @TableField(value = "charge_name")
    private String chargeName;

    /**
     * 店铺id
     */
    @TableField(value = "shop_id")
    private String shopId;

    /**
     * 店铺负责开始时间
     */
    @TableField(value = "enable_time_begin")
    private LocalDate enableTimeBegin;

    /**
     * 店铺负责结束时间
     */
    @TableField(value = "enable_time_end")
    private LocalDate enableTimeEnd;
}
