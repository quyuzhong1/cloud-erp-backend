package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.scm.enums.SupplierPhaseEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
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
     * 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
     */
    @TableField("sync_kingdee_status")
    private String syncKingdeeStatus;

    /**
     * 同步金蝶时间
     */
    @TableField("sync_kingdee_time")
    private LocalDateTime syncKingdeeTime;

    /**
     * 同步金蝶id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;

    /**
     * 同步操作
     */
    @TableField("sync_operate")
    private String syncOperate;


    @Override
    public Serializable pkVal() {
        return null;
    }


}
