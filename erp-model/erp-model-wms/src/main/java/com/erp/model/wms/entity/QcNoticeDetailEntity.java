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
 * 质检通知单明细
 * </p>
 *
 * @author jack
 * @since 2025-04-21
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("qc_notice_detail")
public class QcNoticeDetailEntity extends BaseEntity<QcNoticeDetailEntity> {

    /**
    * main_id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * sku_id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku_no
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * sku_name
    */
    @TableField("product_name")
    private String productName;
    /**
    * 质检通知数量
    */
    @TableField("qc_notice_qty")
    private Integer qcNoticeQty;
    /**
    * 质检数量
    */
    @TableField("qc_qty")
    private Integer qcQty;
    /**
    * 送检差异数量
    */
    @TableField("qc_diff_qty")
    private Integer qcDiffQty;
    /**
    * 良品数量
    */
    @TableField("qc_good_qty")
    private Integer qcGoodQty;
    /**
    * 不良品数量
    */
    @TableField("qc_bad_qty")
    private Integer qcBadQty;
    /**
    * 上架数量
    */
    @TableField("putaway_qty")
    private Integer putawayQty;
    /**
    * 不良备注
    */
    @TableField("bad_desc")
    private String badDesc;
    /**
    * 质检员id
    */
    @TableField("qc_user_id")
    private String qcUserId;
    /**
    * 质检员
    */
    @TableField("qc_user_name")
    private String qcUserName;
    /**
    * 问题属性 type=qcProblemType
    */
    @TableField("qc_problem_dict")
    private String qcProblemDict;
    /**
    * 质检状态 QcBillStatusEnum
    */
    @TableField("qc_status")
    private String qcStatus;
    /**
    * 质检时间
    */
    @TableField("qc_date")
    private LocalDateTime qcDate;
    /**
    * 上架状态 待上架:wait  部分上架：part  已上架：finish
    */
    @TableField("putaway_status")
    private String putawayStatus;
    /**
    * 上架时间
    */
    @TableField("putaway_date")
    private LocalDateTime putawayDate;

    /**
     * 来源明细id
     */
    @TableField("source_detail_id")
    private String sourceDetailId;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String PRODUCT_NAME = "product_name";

    public static final String SKU_NAME = "sku_name";

    public static final String QC_NOTICE_QTY = "qc_notice_qty";

    public static final String QC_QTY = "qc_qty";

    public static final String QC_DIFF_QTY = "qc_diff_qty";

    public static final String QC_GOOD_QTY = "qc_good_qty";

    public static final String QC_BAD_QTY = "qc_bad_qty";

    public static final String PUTAWAY_QTY = "putaway_qty";

    public static final String BAD_DESC = "bad_desc";

    public static final String QC_USER_ID = "qc_user_id";

    public static final String QC_USER_NAME = "qc_user_name";

    public static final String QC_PROBLEM_DICT = "qc_problem_dict";

    public static final String QC_STATUS = "qc_status";

    public static final String QC_DATE = "qc_date";

    public static final String PUTAWAY_STATUS = "putaway_status";

    public static final String PUTAWAY_DATE = "putaway_date";

    @Override
    public Serializable pkVal() {
        return null;
    }

}