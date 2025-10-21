package com.erp.model.plm.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 模具预警策略
 * </p>
 *
 * @author jack
 * @since 2025-10-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_mold_alert_rule")
public class CfgMoldAlertRuleEntity extends BaseEntity<CfgMoldAlertRuleEntity> {

    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 是否作废
    */
    @TableField("invalid_status")
    private Boolean invalidStatus;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 作废备注
    */
    @TableField("invalid_remark")
    private String invalidRemark;
    /**
    * 模具id
    */
    @TableField("mold_id")
    private String moldId;
    /**
    * 模具编码
    */
    @TableField("mold_code")
    private String moldCode;
    /**
    * 模具名称
    */
    @TableField("mold_name")
    private String moldName;
    /**
    * 供应商id
    */
    @TableField("supplier_id")
    private String supplierId;
    /**
    * 供应商编号
    */
    @TableField("supplier_code")
    private String supplierCode;
    /**
    * 供应商名称
    */
    @TableField("supplier_name")
    private String supplierName;
    /**
    * 寿命数量
    */
    @TableField("life_qty")
    private Integer lifeQty;
    /**
    * 预警寿命（数量）
    */
    @TableField("alert_life_qty")
    private Integer alertLifeQty;
    /**
    * 预警寿命（%）
    */
    @TableField("alert_life_rate")
    private BigDecimal alertLifeRate;
    /**
    * 开始日期
    */
    @TableField("start_date")
    private LocalDate startDate;
    /**
    * 结束日期
    */
    @TableField("end_date")
    private LocalDate endDate;
    /**
    * 标准：purchaseOrder=以采购下单数量 ,warehouseReceive=以采购收货数量 ,poInstock=以采购入库数量  枚举：CfgMoldReturnAlertRuleCountDimEnum
    */
    @TableField("count_dim")
    private String countDim;


    public static final String DISABLED = "disabled";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String REMARK = "remark";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String MOLD_ID = "mold_id";

    public static final String MOLD_CODE = "mold_code";

    public static final String MOLD_NAME = "mold_name";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SUPPLIER_CODE = "supplier_code";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String LIFE_QTY = "life_qty";

    public static final String ALERT_LIFE_QTY = "alert_life_qty";

    public static final String ALERT_LIFE_RATE = "alert_life_rate";

    public static final String START_DATE = "start_date";

    public static final String END_DATE = "end_date";

    public static final String COUNT_DIM = "count_dim";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
