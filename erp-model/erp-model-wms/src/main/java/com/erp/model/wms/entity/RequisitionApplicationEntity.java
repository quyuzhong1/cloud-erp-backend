package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * <p>
 * 要货申请单
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("requisition_application")
public class RequisitionApplicationEntity extends BaseEntity<RequisitionApplicationEntity> {

    /**
    * code
    */
    @TableField("code")
    private String code;
    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源单号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 单据状态
    */
    @TableField("status")
    private String status;
    /**
    * 作废状态
    */
    @TableField("invalid_status")
    private String invalidStatus;
    /**
    * 作废备注
    */
    @TableField("invalid_remark")
    private String invalidRemark;
    /**
    * 作废时间
    */
    @TableField("invalid_time")
    private LocalDateTime invalidTime;
    /**
    * 类型
    */
    @TableField("type")
    private String type;
    /**
    * 要货渠道id
    */
    @TableField("channel_id")
    private String channelId;
    /**
    * 要货渠道中文名
    */
    @TableField("channel_name")
    private String channelName;
    /**
    * 要货仓库id
    */
    @TableField("requisition_warehouse_id")
    private String requisitionWarehouseId;
    /**
    * 要货仓库中文名
    */
    @TableField("requisition_warehouse_name")
    private String requisitionWarehouseName;
    /**
    * 处理人id
    */
    @TableField("handle_user_id")
    private String handleUserId;
    /**
    * 处理人中文名
    */
    @TableField("handle_user_name")
    private String handleUserName;
    /**
    * 处理时间
    */
    @TableField("handle_time")
    private LocalDateTime handleTime;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;

    /**
     * fba货件号
     */
    @TableField("fba_shipment_code")
    private String fbaShipmentCode;

    

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String INVALID_TIME = "invalid_time";



    public static final String CHANNEL_ID = "channel_id";

    public static final String CHANNEL_NAME = "channel_name";

    public static final String REQUISITION_WAREHOUSE_ID = "requisition_warehouse_id";

    public static final String REQUISITION_WAREHOUSE_NAME = "requisition_warehouse_name";

    public static final String TO_WAREHOUSE_ID = "to_warehouse_id";

    public static final String TO_WAREHOUSE_NAME = "to_warehouse_name";

    public static final String FROM_WAREHOUSE_ID = "from_warehouse_id";

    public static final String FROM_WAREHOUSE_NAME = "from_warehouse_name";

    public static final String HANDLE_USER_ID = "handle_user_id";

    public static final String HANDLE_USER_NAME = "handle_user_name";

    public static final String HANDLE_TIME = "handle_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}