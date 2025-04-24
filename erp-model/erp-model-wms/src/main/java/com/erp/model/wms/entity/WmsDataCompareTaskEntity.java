package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 数据对比任务
 * </p>
 *
 * @author shukai
 * @since 2024-03-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("wms_data_compare_task")
public class WmsDataCompareTaskEntity extends BaseEntity<WmsDataCompareTaskEntity> {

    /**
    * 任务编号
    */
    @TableField("code")
    private String code;
    /**
    * 任务名称
    */
    @TableField("name")
    private String name;
    /**
    * 单据类型：soOutstock=销售出库单，fbaShipment=FBA货件签收，overseasInbound=第三方仓货件签收  枚举：WmsDataCompareTaskBillTypeEnum
    */
    @TableField("bill_type")
    private String billType;
    /**
    * 任务状态：init=初始，doing=进行中，finish=已完成，error=异常  枚举：WmsDataCompareTaskStatusEnum
    */
    @TableField("status")
    private String status;
    /**
    * 子任务状态：wait_parse=待解析，wait_compare=待比对，wait_upload=待上传，finish=已完成，error=异常  枚举：WmsDataCompareTaskSubStatusEnum
    */
    @TableField("sub_status")
    private String subStatus;
    /**
    * 异常原因
    */
    @TableField("error_message")
    private String errorMessage;
    /**
    * 失败次数，超过3次告警
    */
    @TableField("error_count")
    private Integer errorCount;
    /**
    * 系统数据范围条件json串
    */
    @TableField("system_data_condition")
    private String systemDataCondition;
    /**
    * 系统数据总行数
    */
    @TableField("system_data_count")
    private Integer systemDataCount;
    /**
    * 导入数据总行数
    */
    @TableField("import_data_count")
    private Integer importDataCount;
    /**
    * 导入数据字段映射json串
    */
    @TableField("import_data_mapping")
    private String importDataMapping;
    /**
    * 对比结果-完全一致
    */
    @TableField("result_same_count")
    private Integer resultSameCount;
    /**
    * 对比结果-系统多单
    */
    @TableField("result_exceed_count")
    private Integer resultExceedCount;
    /**
    * 对比结果-系统漏单
    */
    @TableField("result_miss_count")
    private Integer resultMissCount;
    /**
    * 对比结果-差异条数
    */
    @TableField("result_diff_count")
    private Integer resultDiffCount;
    /**
    * 对比结果报告下载地址
    */
    @TableField("result_report_url")
    private String resultReportUrl;
    
    /**
     * 对比类型 WmsDataCompareTypeEnum
     */
    @TableField("compare_type")
    private String compareType;


    

    

    public static final String BILL_TYPE = "bill_type";

    

    public static final String SUB_STATUS = "sub_status";

    public static final String ERROR_MESSAGE = "error_message";

    public static final String ERROR_COUNT = "error_count";

    public static final String SYSTEM_DATA_CONDITION = "system_data_condition";

    public static final String SYSTEM_DATA_COUNT = "system_data_count";

    public static final String IMPORT_DATA_COUNT = "import_data_count";

    public static final String IMPORT_DATA_MAPPING = "import_data_mapping";

    public static final String RESULT_SAME_COUNT = "result_same_count";

    public static final String RESULT_EXCEED_COUNT = "result_exceed_count";

    public static final String RESULT_MISS_COUNT = "result_miss_count";

    public static final String RESULT_DIFF_COUNT = "result_diff_count";

    public static final String RESULT_REPORT_URL = "result_report_url";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
