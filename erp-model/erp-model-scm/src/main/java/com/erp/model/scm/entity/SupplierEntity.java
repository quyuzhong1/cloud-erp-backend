package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.scm.enums.SupplierPhaseEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 供应商表
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("supplier")
public class SupplierEntity extends BaseEntity<SupplierEntity> {

    /**
     * 供应商名
     */
    @TableField("name")
    private String name;

    /**
     * 编号
     */
    @TableField("code")
    private String code;


    /**
     * 分类名
     */
    @TableField("category_name")
    private String categoryName;

    /**
     * 分类id
     */
    @TableField("category_id")
    private String categoryId;

    /**
     * 等级id
     */
    @TableField("grade_id")
    private String gradeId;

    /**
     * 等级名
     */
    @TableField("grade_name")
    private String gradeName;

    /**
     * 采购员id
     */
    @TableField("purchase_user_id")
    private String purchaseUserId;


    /**
     * 采购员
     */
    @TableField("purchase_user_name")
    private String purchaseUserName;


    /**
     * 阶段
     */
    @TableField("phase")
    private SupplierPhaseEnum phase;

    /**
     * 公司地址
     */
    @TableField("company_address")
    private String companyAddress;


    /**
     * 公司网址
     */
    @TableField("company_website")
    private String companyWebsite;


    /**
     * 审核状态
     */
    @TableField(value="approve_status")
    private ApproveStatusEnum approveStatus;

    /**
     * 禁用状态  true  禁用
     *  false 启用
     */
    @TableField("disabled")
    private Boolean disabled;


    /**
     * 付款方式
     */
    @TableField("pay_method_id")
    private String payMethodId;

    /**
     * 付款币种
     */
    @TableField("pay_currency")
    private String payCurrency;

    /**
     * 同步金蝶id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;

    /**
     * 付款条件
     */
    @TableField("payment_condition")
    private String paymentCondition;

    /**
     * 审核时间
     */
    @TableField(value = "approve_time",updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime approveTime;

    /**
     * 审核人名称
     */
    @TableField("approve_user_name")
    private String approveUserName;

    /**
     * 审核人id
     */
    @TableField("approve_user_id")
    private String approveUserId;


    /**
     * SRM协同 true 否 false 是
     */
    @TableField("srm_disabled")
    private Boolean srmDisabled;

    /**
     * SRM操作时间
     */
    @TableField("srm_disabled_date")
    private LocalDate srmDisabledDate;

    /**
     * SRM操作人名称ID
     */
    @TableField("srm_operate_user_id")
    private String srmOperateUserId;
    /**
     * SRM操作人名称
     */
    @TableField("srm_operate_user_name")
    private String srmOperateUserName;

    /**
     * 税率
     */
    @TableField("tax_rate")
    private BigDecimal taxRate;
    /**
     * 付款公司名称
     */
    @TableField("payment_company_name")
    private String paymentCompanyName;

    @Override
    public Serializable pkVal() {
        return null;
    }


}
