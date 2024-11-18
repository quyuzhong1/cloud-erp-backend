package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 查询比较符配置
 * </p>
 *
 * @author lrp
 * @since 2024-01-04
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_query_compare")
public class CfgQueryCompareEntity extends BaseEntity<CfgQueryCompareEntity> {

    /**
    * 接口名称
比较符
    */
    @TableField("compare_code")
    private String compareCode;
    /**
    * 比较符name
    */
    @TableField("compare_name")
    private String compareName;
    /**
    * 数据类型
    */
    @TableField("data_type")
    private String dataType;


    public static final String COMPARE_CODE = "compare_code";

    public static final String COMPARE_NAME = "compare_name";

    public static final String DATA_TYPE = "data_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}