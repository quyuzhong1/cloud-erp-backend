package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.wms.enums.QcResultEnum;
import com.erp.model.wms.enums.QcTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("qc_info")
public class QcInfoEntity extends BaseEntity<QcInfoEntity> {

    /**
     * 质检单id
     */
    @TableField("main_id")
    private String mainId;

    /**
     * 质检类型 stockIn 入库质检   outsideQc 外检质检  insideQc  在库质检   newProductStockIn 新品入库质检  b2bOutsideQc  B2B外检  
     */
    @TableField("qc_type")
    private QcTypeEnum qcType;

    /**
     * 抽检比例
     */
    @TableField("qc_sample_rate")
    private BigDecimal qcSampleRate;

    /**
     * 是否内检 默认 true
     */
    @TableField("is_inside")
    private Boolean isInside;

    /**
     * 质检数量
     */
    @TableField("qc_qty")
    private Integer qcQty;

    /**
     * 质检合格数量
     */
    @TableField("qc_good_qty")
    private Integer qcGoodQty;

    /**
     * 不良数量
     */
    @TableField("qc_bad_qty")
    private Integer qcBadQty;

    /**
     * 总数量
     */
    @TableField("total_qty")
    private Integer totalQty;

    /**
     * 质检合格率
     */
    @TableField("qc_good_rate")
    private BigDecimal qcGoodRate;

    /**
     * 质检不良率
     */
    @TableField("qc_bad_rate")
    private BigDecimal qcBadRate;

    /**
     * 质检问题属性
     * type=qcProblemType
     */
    @TableField("qc_problem_dict")
    private String qcProblemDict;

    /**
     * 不良描述
     */
    @TableField("bad_description ")
    private String badDescription ;

    /**
     * 质检结果
     */
    @TableField("qc_result")
    private QcResultEnum qcResult;

    /**
     * 处理方式
     * type=handleModeType
     */
    @TableField("handle_mode_dict")
    private String handleModeDict;


    public static final String MAIN_ID = "main_id";

    public static final String QC_TYPE = "qc_type";

    public static final String QC_SAMPLE_RATE = "qc_sample_rate";

    public static final String IS_INSIDE = "is_inside";

    public static final String QC_QTY = "qc_qty";

    public static final String QC_BAD_QTY = "qc_bad_qty";

    public static final String TOTAL_QTY = "total_qty";

    public static final String QC_GOOD_RATE = "qc_good_rate";

    public static final String QC_BAD_RATE = "qc_bad_rate";

    public static final String QC_PROBLEM_KEY = "qc_problem_dict";

    public static final String BAD_DESCRIPTION  = "bad_description ";

    public static final String QC_RESULT = "qc_result";

    public static final String HANDLE_MODE_KEY = "handle_mode_dict";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
