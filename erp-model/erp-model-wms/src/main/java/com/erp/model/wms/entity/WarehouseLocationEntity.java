package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 仓库仓位分区表
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-05-22
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("warehouse_location")
public class WarehouseLocationEntity extends BaseEntity<WarehouseLocationEntity> {

    /**
     * 类型
     */
    @TableField("type")
    private String type;

    /**
     * 编码
     */
    @TableField("code")
    private String code;

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 状态
     */
    @TableField("status")
    private String status;

    /**
     * 仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 父id
     */
    @TableField("parent_id")
    private String parentId;

    /**
     * 禁用状态，true表示禁用
     */
    @TableField("disabled")
    private Boolean disabled;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    public static final String TYPE = "type";

    public static final String CODE = "code";

    public static final String NAME = "name";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String STATUS = "status";

    public static final String PARENT_ID = "parent_id";

    public static final String DISABLED = "disabled";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
