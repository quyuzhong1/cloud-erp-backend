package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 手工出入库待同步数据表
 * </p>
 *
 * @author Cloud
 * @since 2023-06-25
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_out_in_stock")
public class DmpOutInStockEntity extends BaseEntity<DmpOutInStockEntity> {


    /**
    * 仓库编码
    */
    @TableField("warehouse_code")
    private String warehouseCode;

    /**
    * 仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;

    /**
    * 出库类型
    */
    @TableField("type_name")
    private String typeName;

    /**
    * 负责人名称
    */
    @TableField("charge_user_name")
    private String chargeUserName;

    /**
    * 备注
    */
    @TableField("remark")
    private String remark;

    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;

    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;

    /**
    * 来源平台 自研erp，马帮，管易云，金蝶云星空
    */
    @TableField("platform_sign")
    private String platformSign;

    /**
    * 同步马帮状态
    */
    @TableField("sync_mb_status")
    private String syncMbStatus;

    /**
    * 同步时间
    */
    @TableField("last_sync_mb_time")
    private LocalDateTime lastSyncMbTime;

    /**
    * 类型，入库：in 出库 : out
    */
    @TableField("type")
    private String type;

    /**
    * 目标平台
    */
    @TableField("target_platform_sign")
    private String targetPlatformSign;

    /**
    * 目标平台单据号
    */
    @TableField("target_order_code")
    private String targetOrderCode;

    /**
     * 来源单据编号
     */
    @TableField("source_code")
    private String sourceCode;


    public static final String WAREHOUSE_CODE = "warehouse_code";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String TYPE_NAME = "type_name";

    public static final String CHARGE_USER_NAME = "charge_user_name";

    public static final String REMARK = "remark";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_ID = "source_id";

    public static final String PLATFORM_SIGN = "platform_sign";

    public static final String SYNC_MB_STATUS = "sync_mb_status";

    public static final String LAST_SYNC_MB_TIME = "last_sync_mb_time";

    public static final String TYPE = "type";

    public static final String TARGET_PLATFORM_SIGN = "target_platform_sign";

    public static final String TARGET_ORDER_CODE = "target_order_code";

    public static final String SOURCE_CODE = "source_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}