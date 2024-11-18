package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 虚拟仓分货单
 * </p>
 *
 * @author hyj
 * @since 2024-06-05
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("virtual_warehouse_allocation")
public class VirtualWarehouseAllocationEntity extends BaseEntity<VirtualWarehouseAllocationEntity> {

    /**
    * 是否失效 true 失效 false 未失效
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * code
    */
    @TableField("code")
    private String code;
    /**
    * 类型：allocation新增分货，transfer虚拟仓调拨，cancel取消分货
    */
    @TableField("type")
    private String type;
    /**
    * 状态 ：0待提交 1已处理 2已作废
    */
    @TableField("status")
    private String status;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 方向
    */
    @TableField("direction")
    private Integer direction;
    /**
    * 作废说明
    */
    @TableField("invalid_description")
    private String invalidDescription;

    /**
     * 是否计入统计，true是，false否
     */
    @TableField("is_statistics")
    private Boolean isStatistics;

    public static final String INVALID_DESCRIPTION = "invalid_description";

    @Override
    public Serializable pkVal() {
        return null;
    }

}