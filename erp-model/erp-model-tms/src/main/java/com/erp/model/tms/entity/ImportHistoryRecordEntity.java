package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 物流授权表
 * </p>
 *
 * @author will
 * @since 2026-01-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("import_history_record")
public class ImportHistoryRecordEntity extends BaseEntity<ImportHistoryRecordEntity> {

    /**
     * 批次编号
     */
    @TableField("code")
    private String code;
    /**
     * 对账月份
     */
    @TableField("reconciliation_month")
    private String reconciliationMonth;
    /**
     * 业务类型，自发货费用/尾程费用
     */
    @TableField("business_type")
    private String businessType;
    /**
     * 上传导入附件的url
     */
    @TableField("file_url")
    private String fileUrl;
    /**
     * 附件名称
     */
    @TableField("file_name")
    private String fileName;
    /**
     * 导入数量
     */
    @TableField("import_count")
    private Integer importCount;
    /**
     * 匹配数量
     */
    @TableField("match_count")
    private Integer matchCount;
    /**
     * 处理状态
     */
    @TableField("status")
    private String status;
    /**
     * 类型
     */
    @TableField("type")
    private String type;
    /**
     * 操作人id
     */
    @TableField("operation_user_id")
    private String operationUserId;

    /**
     * 清洗结果附件的url
     */
    @TableField("clean_file_url")
    private String cleanFileUrl;

    /**
     * 清洗结果附件名称
     */
    @TableField("clean_file_name")
    private String cleanFileName;

    /**
     * sheet页名称
     */
    @TableField("sheet_name")
    private String sheetName;


    public static final String CODE = "code";

    public static final String RECONCILIATION_MONTH = "reconciliation_month";

    public static final String BUSINESS_TYPE = "business_type";

    public static final String FILE_URL = "file_url";

    public static final String FILE_NAME = "file_name";

    public static final String IMPORT_COUNT = "import_count";

    public static final String MATCH_QTY = "match_qty";

    public static final String STATUS = "status";

    public static final String TYPE = "type";

    public static final String OPERATION_USER_ID = "operation_user_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}