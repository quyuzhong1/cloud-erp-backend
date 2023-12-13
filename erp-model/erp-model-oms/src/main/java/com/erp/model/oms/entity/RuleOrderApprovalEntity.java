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
 * 订单审核规则
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("rule_order_approval")
public class RuleOrderApprovalEntity extends BaseEntity<RuleOrderApprovalEntity> {

    /**
    * 名称
    */
    @TableField("name")
    private String name;
    /**
    * 优先级
    */
    @TableField("priority")
    private Integer priority;
    /**
    * 是否禁用 false 未禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 分类明细id
    */
    @TableField("category_detail_id")
    private String categoryDetailId;
    /**
    * 操作类型多个逗号分割
    */
    @TableField("operation_type")
    private String operationType;
    /**
    * 流向状态
    */
    @TableField("flow_status")
    private String flowStatus;


    public static final String NAME = "name";

    public static final String PRIORITY = "priority";

    public static final String DISABLED = "disabled";

    public static final String REMARK = "remark";

    public static final String CATEGORY_DETAIL_ID = "category_detail_id";

    public static final String OPERATION_TYPE = "operation_type";

    public static final String FLOW_STATUS = "flow_status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}