package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 
 * </p>
 *
 * @author jack
 * @since 2025-06-04
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dict_notice_role_option")
public class DictNoticeRoleOptionEntity extends BaseEntity<DictNoticeRoleOptionEntity> {

    /**
    * 单据类型
    */
    @TableField("business_type")
    private String businessType;
    /**
    * 表类型：table=主表,detail=明细表  枚举：DictNoticeRoleOptionTableTypeEnum
    */
    @TableField("table_type")
    private String tableType;
    /**
    * 表名
    */
    @TableField("table_name")
    private String tableName;
    /**
    * 系统归属
    */
    @TableField("sys_classify")
    private String sysClassify;
    /**
    * 类路径
    */
    @TableField("class_path")
    private String classPath;
    /**
    * 字段（驼峰命名）
    */
    @TableField("field")
    private String field;
    /**
    * 字段名
    */
    @TableField("field_name")
    private String fieldName;
    /**
    * 关联字段（明细表必填）
    */
    @TableField("ref_field")
    private String refField;
    /**
    * 是否启用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 排序
    */
    @TableField("index")
    private Integer index;


    public static final String BUSINESS_TYPE = "business_type";

    public static final String TABLE_TYPE = "table_type";

    public static final String TABLE_NAME = "table_name";

    public static final String SYS_CLASSIFY = "sys_classify";

    public static final String CLASS_PATH = "class_path";

    public static final String FIELD = "field";

    public static final String FIELD_NAME = "field_name";

    public static final String REF_FIELD = "ref_field";

    public static final String DISABLED = "disabled";

    public static final String REMARK = "remark";

    public static final String INDEX = "index";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
