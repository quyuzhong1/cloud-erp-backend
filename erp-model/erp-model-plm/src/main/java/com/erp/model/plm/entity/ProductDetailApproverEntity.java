package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: 产品信息审核人设置表
 * @date 2022/11/28 16:50
 */
@TableName(value ="product_detail_approver")
@Data
public class ProductDetailApproverEntity implements Serializable {


    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 审批人1
     */
    @TableField("first_approve_id")
    private String firstApproveId;

    /**
     * 审批人2
     */
    @TableField("second_approve_id")
    private String secondApproveId;

    /**
     * 审批人3
     */
    @TableField("third_approve_id")
    private String thirdApproveId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name")
    private String createUserName;


    /**
     * 创建人id
     */
    @TableField(value = "create_user_id")
    private String createUserId;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 修改人id
     */
    @TableField(value = "update_user_id")
    private String updateUserId;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name")
    private String updateUserName;

}
