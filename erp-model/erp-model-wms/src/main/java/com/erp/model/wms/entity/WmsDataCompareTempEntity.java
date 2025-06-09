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
 * 数据对比对比加工临时表
 * </p>
 *
 * @author shukai
 * @since 2024-03-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("wms_data_compare_temp")
public class WmsDataCompareTempEntity extends BaseEntity<WmsDataCompareTempEntity> {

    /**
    * 任务id
    */
    @TableField("task_id")
    private String taskId;
    /**
    * 主数据类型：system=系统数据，import=导入数据  枚举：WmsDataCompareTempMainDataTypeEnum
    */
    @TableField("main_data_type")
    private String mainDataType;
    /**
    * 对比状态：wait=待对比，finish=对比完成  枚举：WmsDataCompareTempCompareStatusEnum
    */
    @TableField("compare_status")
    private String compareStatus;
    /**
    * 差异字段，多个用逗号隔开
    */
    @TableField("diff_fields")
    private String diffFields;
    /**
    * 主键字段值
    */
    @TableField("pk_field_value")
    private String pkFieldValue;
    /**
    * 系统数据id
    */
    @TableField("system_data_id")
    private String systemDataId;
    /**
    * 对比结果：same=完全一致，exceed=系统多单，miss=系统漏单，diff=差异  枚举：WmsDataCompareTempCompareResultEnum
    */
    @TableField("compare_result")
    private String compareResult;
    /**
    * 导入数据json
    */
    @TableField("import_data_json")
    private String importDataJson;
    /**
    * 系统数据json
    */
    @TableField("system_data_json")
    private String systemDataJson;


    public static final String TASK_ID = "task_id";

    public static final String MAIN_DATA_TYPE = "main_data_type";

    public static final String COMPARE_STATUS = "compare_status";

    public static final String DIFF_FIELDS = "diff_fields";

    public static final String PK_FIELD_VALUE = "pk_field_value";

    public static final String SYSTEM_DATA_ID = "system_data_id";

    public static final String COMPARE_RESULT = "compare_result";

    public static final String IMPORT_DATA_JSON = "import_data_json";

    public static final String SYSTEM_DATA_JSON = "system_data_json";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
