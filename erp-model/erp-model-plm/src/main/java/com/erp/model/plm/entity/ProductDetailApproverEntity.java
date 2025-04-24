package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 产品信息审核人设置表
 * @date 2022/11/28 16:50
 */
@TableName(value ="product_detail_approver")
@Data
public class ProductDetailApproverEntity extends BaseEntity<ProductDetailApproverEntity> implements Serializable {

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

}
