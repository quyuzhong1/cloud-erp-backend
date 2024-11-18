package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * <p>
 * 分货单拆单主表
 * </p>
 *
 * @author hyj
 * @since 2024-06-07
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("virtual_warehouse_push_handle")
@AllArgsConstructor
@NoArgsConstructor
public class VirtualWarehousePushHandleEntity extends BaseEntity<VirtualWarehousePushHandleEntity> {

    /**
    * 分货单id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 编号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 类型：allocation新增分货，transfer虚拟仓调拨，cancel取消分货
    */
    @TableField("type")
    private String type;
    /**
    * 状态 ：waitSubmit待提交 handle已处理 invalid已作废
    */
    @TableField("status")
    private String status;
    /**
    * 方向
    */
    @TableField("direction")
    private Integer direction;

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}