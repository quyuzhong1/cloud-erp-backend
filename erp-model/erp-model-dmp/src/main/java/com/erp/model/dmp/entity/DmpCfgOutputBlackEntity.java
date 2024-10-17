package com.erp.model.dmp.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 输出黑名单
 * </p>
 *
 * @author shukai
 * @since 2024-07-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_cfg_output_black")
public class DmpCfgOutputBlackEntity extends BaseEntity<DmpCfgOutputBlackEntity> {

    /**
    * 父id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 数据类型:int=数字,string=字符,date=日期  枚举：DmpCfgOutputBlackDataTypeEnum
    */
    @TableField("data_type")
    private String dataType;
    /**
    * 比较符:eq=等于,ne=不等于,in=属于,gt=大于,ge=大于等于,lt=小于,le=小于等于  枚举：DmpCfgOutputBlackCompareSignEnum
    */
    @TableField("compare_sign")
    private String compareSign;
    /**
    * 属性名
    */
    @TableField("field_name")
    private String fieldName;
    /**
    * 属性值
    */
    @TableField("field_value")
    private String fieldValue;

    /**
     * 逻辑删除字段
     */
    @TableField(value = "disabled")
    private Boolean disabled;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;
    
    /**
     * 是否界面增加
     */
    @TableField(value = "is_web_add")
    private Boolean isWebAdd;

    public static final String MAIN_ID = "main_id";

    public static final String DATA_TYPE = "data_type";

    public static final String COMPARE_SIGN = "compare_sign";

    public static final String FIELD_NAME = "field_name";

    public static final String FIELD_VALUE = "field_value";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
