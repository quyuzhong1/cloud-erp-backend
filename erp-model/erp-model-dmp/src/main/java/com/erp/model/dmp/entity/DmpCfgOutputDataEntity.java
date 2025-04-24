package com.erp.model.dmp.entity;

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
 * 输出数据获取配置
 * </p>
 *
 * @author shukai
 * @since 2025-02-10
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_cfg_output_data")
public class DmpCfgOutputDataEntity extends BaseEntity<DmpCfgOutputDataEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 名称
    */
    @TableField("name")
    private String name;
    /**
    * 获取数据类型：db=数据库,api=接口  枚举：DmpCfgOutputDataTypeEnum
    */
    @TableField("type")
    private String type;
    /**
    * 数据库id
    */
    @TableField("db_id")
    private String dbId;
    /**
    * sql语句
    */
    @TableField("sql_string")
    private String sqlString;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;


    public static final String MAIN_ID = "main_id";

    public static final String NAME = "name";

    public static final String TYPE = "type";

    public static final String DB_ID = "db_id";

    public static final String SQL_STRING = "sql_string";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
