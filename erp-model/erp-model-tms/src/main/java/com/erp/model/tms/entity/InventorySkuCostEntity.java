package com.erp.model.tms.entity;

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
 * SKU成本
 * </p>
 *
 * @author zdy
 * @since 2024-08-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("inventory_sku_cost")
public class InventorySkuCostEntity extends BaseEntity<InventorySkuCostEntity> {

    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 单据编号
    */
    @TableField("code")
    private String code;
    /**
    * 单据状态：waitSubmit=待提交，approveIng=审核中，reject=审核不通过，approve=已审核
    */
    @TableField("status")
    private String status;
    /**
    * 分摊月份
    */
    @TableField("allocated_month")
    private LocalDate allocatedMonth;
    /**
    * 币种
    */
    @TableField("currency")
    private String currency;
    /**
    * 币别符号
    */
    @TableField("currency_symbol")
    private String currencySymbol;
    /**
    * 汇率（兑换人民币汇率）
    */
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;
    /**
    * 核算公司id(sys.sys_accounting_company)
    */
    @TableField("company_id")
    private String companyId;
    /**
    * 核算公司名称
    */
    @TableField("company_name")
    private String companyName;


    public static final String REMARK = "remark";

    public static final String CODE = "code";

    public static final String STATUS = "status";

    public static final String ALLOCATED_MONTH = "allocated_month";

    public static final String CURRENCY = "currency";

    public static final String CURRENCY_SYMBOL = "currency_symbol";

    public static final String EXCHANGE_RATE = "exchange_rate";

    public static final String COMPANY_ID = "company_id";

    public static final String COMPANY_NAME = "company_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}