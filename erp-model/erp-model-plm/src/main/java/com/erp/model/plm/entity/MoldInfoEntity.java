package com.erp.model.plm.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 模具档案
 * </p>
 *
 * @author jack
 * @since 2025-10-10
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mold_info")
public class MoldInfoEntity extends BaseEntity<MoldInfoEntity> {

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
    * 审批状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * 审批时间
    */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
    * 审批人ID
    */
    @TableField("approve_user_id")
    private String approveUserId;
    /**
    * 审批人姓名
    */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
    * 模具编号
    */
    @TableField("code")
    private String code;
    /**
    * 模具名称
    */
    @TableField("name")
    private String name;
    /**
    * 模具标识：first =首套模,copy =复制模  枚举：MoldInfoTagEnum
    */
    @TableField("tag")
    private String tag;
    /**
    * 项目编号
    */
    @TableField("project_code")
    private String projectCode;
    /**
    * 项目名称
    */
    @TableField("project_name")
    private String projectName;
    /**
    * 产品经理id
    */
    @TableField("charge_id")
    private String chargeId;
    /**
    * 产品经理
    */
    @TableField("charge_name")
    private String chargeName;
    /**
    * 项目经理id
    */
    @TableField("project_charge_id")
    private String projectChargeId;
    /**
    * 项目经理
    */
    @TableField("project_charge_name")
    private String projectChargeName;
    /**
    * 模具分类
    */
    @TableField("category_id")
    private String categoryId;
    /**
    * 模具类型
    */
    @TableField("type")
    private String type;
    /**
    * 模具穴数
    */
    @TableField("mold_holes")
    private String moldHoles;
    /**
    * 尺寸单位
    */
    @TableField("size_unit")
    private String sizeUnit;
    /**
    * 长(mm)
    */
    @TableField("product_length")
    private BigDecimal productLength;
    /**
    * 宽(mm)
    */
    @TableField("product_width")
    private BigDecimal productWidth;
    /**
    * 高(mm)
    */
    @TableField("product_height")
    private BigDecimal productHeight;
    /**
    * 模具材质
    */
    @TableField("materials")
    private String materials;
    /**
    * 开模周期(自然日)
    */
    @TableField("cycle")
    private Integer cycle;
    /**
    * 模具启用日期
    */
    @TableField("activation_date")
    private LocalDate activationDate;
    /**
    * 供应商id
    */
    @TableField("supplier_id")
    private String supplierId;
    /**
    * 供应商编码
    */
    @TableField("supplier_code")
    private String supplierCode;
    /**
    * 供应商
    */
    @TableField("supplier_name")
    private String supplierName;
    /**
    * 含税单价
    */
    @TableField("tax_price")
    private BigDecimal taxPrice;
    /**
    * 税率(%)
    */
    @TableField("rate")
    private BigDecimal rate;
    /**
    * 结算方式
    */
    @TableField("pay_method_id")
    private String payMethodId;
    /**
    * 付款条件
    */
    @TableField("payment_condition")
    private String paymentCondition;


    public static final String INVALID_STATUS = "invalid_status";

    public static final String REMARK = "remark";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String CODE = "code";

    public static final String NAME = "name";

    public static final String TAG = "tag";

    public static final String PROJECT_CODE = "project_code";

    public static final String PROJECT_NAME = "project_name";

    public static final String CHARGE_ID = "charge_id";

    public static final String CHARGE_NAME = "charge_name";

    public static final String PROJECT_CHARGE_ID = "project_charge_id";

    public static final String PROJECT_CHARGE_NAME = "project_charge_name";

    public static final String CATEGORY_ID = "category_id";

    public static final String TYPE = "type";

    public static final String MOLD_HOLES = "mold_holes";

    public static final String SIZE_UNIT = "size_unit";

    public static final String PRODUCT_LENGTH = "product_length";

    public static final String PRODUCT_WIDTH = "product_width";

    public static final String PRODUCT_HEIGHT = "product_height";

    public static final String MATERIALS = "materials";

    public static final String CYCLE = "cycle";

    public static final String ACTIVATION_DATE = "activation_date";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SUPPLIER_CODE = "supplier_code";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String TAX_PRICE = "tax_price";

    public static final String RATE = "rate";

    public static final String PAY_METHOD_ID = "pay_method_id";

    public static final String PAYMENT_CONDITION = "payment_condition";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
