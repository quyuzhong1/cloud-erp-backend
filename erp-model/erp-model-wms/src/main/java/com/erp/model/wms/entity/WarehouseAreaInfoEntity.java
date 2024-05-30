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
 * 库区管理
 * </p>
 *
 * @author liaohui
 * @since 2024-05-29
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("warehouse_area_info")
public class WarehouseAreaInfoEntity extends BaseEntity<WarehouseAreaInfoEntity> {

    /**
     * 库区编码
     */
    @TableField("code")
    private String code;

    /**
     * 库区名字
     */
    @TableField("name")
    private String name;

    /**
     * 库区类型
     */
    @TableField("type")
    private String type;

    /**
     * 所属仓库
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 是否禁用 true 是 false 不是
     */
    @TableField("disabled")
    private Boolean disabled;

    /**
     * 状态  waitSubmit 待提交 waitAudit 待审核 auditIng 审核中 auditNoPass 审核不通过， auditPass 审核通过
     */
    @TableField("status")
    private String status;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;


    public static final String CODE = "code";

    public static final String NAME = "name";

    public static final String TYPE = "type";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String DISABLED = "disabled";

    public static final String STATUS = "status";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
