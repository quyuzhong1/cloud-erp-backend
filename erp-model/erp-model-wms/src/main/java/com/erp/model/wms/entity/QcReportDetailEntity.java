package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
@TableName("qc_report_detail")
public class QcReportDetailEntity extends BaseEntity<QcReportDetailEntity> {

    /**
     * 质检单id
     */
    @TableField("main_id")
    private String mainId;

    /**
     * 质检报告id
     */
    @TableField("qc_report_id")
    private String qcReportId;

    /**
     * 说明
     */
    @TableField("description")
    private String description;

    /**
     * 结果
     */
    @TableField("result_dict")
    private String resultDict;


    public static final String MAIN_ID = "main_id";

    public static final String QC_REPORT_ID = "qc_report_id";

    public static final String DESCRIPTION = "description";

    public static final String RESULT_KEY = "result_key";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
