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
 * 数据对比导入文件信息
 * </p>
 *
 * @author shukai
 * @since 2024-03-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("wms_data_compare_import")
public class WmsDataCompareImportEntity extends BaseEntity<WmsDataCompareImportEntity> {

    /**
    * 任务id
    */
    @TableField("task_id")
    private String taskId;
    /**
    * 导入数据文件url地址
    */
    @TableField("file_url")
    private String fileUrl;
    /**
    * 解析状态：wait=待解析，finish=已解析  枚举：WmsDataCompareImportParseStatusEnum
    */
    @TableField("parse_status")
    private String parseStatus;
    /**
    * 当前解析偏移量
    */
    @TableField("curr_parse_offset")
    private Integer currParseOffset;
    
    /**
     * 主数据标识
     */
    @TableField(value = "main_flag")
    private Boolean mainFlag; 
    
    public static final String TASK_ID = "task_id";

    public static final String FILE_URL = "file_url";

    public static final String PARSE_STATUS = "parse_status";

    public static final String CURR_PARSE_OFFSET = "curr_parse_offset";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
