package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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
@ToString
@Getter
@Setter
@Accessors(chain = true)
@TableName("warehouse_location")
public class WarehouseLocationEntity extends BaseEntity<WarehouseLocationEntity> {

    /**
     * 类型，location-仓位;area-分区
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
     * 库区类型 warehouseAreaType
     */
    @TableField("area_type")
    private String areaType;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 占用状态
     */
    @TableField("occupy_status")
    private Boolean occupyStatus;

    

    

    

    public static final String WAREHOUSE_ID = "warehouse_id";

    

    public static final String PARENT_ID = "parent_id";

    

    

    @Override
    public Serializable pkVal() {
        return null;
    }

}
