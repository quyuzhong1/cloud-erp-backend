package com.erp.model.tms.entity;

import java.math.BigDecimal;
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
 * 中转报关详情
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("transfer_declare_detail")
public class TransferDeclareDetailEntity extends BaseEntity<TransferDeclareDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 销售单id
    */
    @TableField("so_id")
    private String soId;
    /**
    * 销售单号
    */
    @TableField("so_code")
    private String soCode;
    /**
    * 物流渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;
    /**
    * 物流渠道中文
    */
    @TableField("logistics_channel_name")
    private String logisticsChannelName;
    /**
    * 物流跟踪号
    */
    @TableField("tracking_no")
    private String trackingNo;
    /**
    * 包裹重量
    */
    @TableField("package_weight")
    private BigDecimal packageWeight;
    /**
    * 包裹重量单位
    */
    @TableField("weight_unit")
    private String weightUnit;
    /**
    * 出库状态
    */
    @TableField("outstock_status")
    private String outstockStatus;
    /**
    * 中转状态
    */
    @TableField("transfer_staus")
    private String transferStaus;


    public static final String MAIN_ID = "main_id";

    public static final String SO_ID = "so_id";

    public static final String SO_CODE = "so_code";

    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String LOGISTICS_CHANNEL_NAME = "logistics_channel_name";

    public static final String TRACKING_NO = "tracking_no";

    public static final String PACKAGE_WEIGHT = "package_weight";

    public static final String WEIGHT_UNIT = "weight_unit";

    public static final String OUTSTOCK_STATUS = "outstock_status";

    public static final String TRANSFER_STAUS = "transfer_staus";

    @Override
    public Serializable pkVal() {
        return null;
    }

}