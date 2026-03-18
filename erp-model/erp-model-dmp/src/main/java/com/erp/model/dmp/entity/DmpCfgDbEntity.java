package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 输入输出db信息
 * </p>
 *
 * @author shukai
 * @since 2026-03-17
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("dmp_cfg_db")
public class DmpCfgDbEntity extends BaseEntity<DmpCfgDbEntity> {

    /**
    * 系统id
    */
    @TableField("system_id")
    private String systemId;
    /**
    * 输入输出类型：input=输入，output=输出
    */
    @TableField("type")
    private String type;
    /**
    * db类型:pg=pg，mysql=mysql
    */
    @TableField("db_type")
    private String dbType;
    /**
    * 主机
    */
    @TableField("host")
    private String host;
    /**
    * 端口
    */
    @TableField("port")
    private Integer port;
    /**
    * 用户名
    */
    @TableField("user_name")
    private String userName;
    /**
    * 密码
    */
    @TableField("pass_word")
    private String passWord;
    /**
    * 数据库名
    */
    @TableField("db_name")
    private String dbName;
    /**
    * 表名
    */
    @TableField("table_name")
    private String tableName;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 最小连接数量
    */
    @TableField("min_connection_size")
    private Integer minConnectionSize;
    /**
    * 最大连接数量
    */
    @TableField("max_connection_size")
    private Integer maxConnectionSize;


    public static final String SYSTEM_ID = "system_id";

    public static final String TYPE = "type";

    public static final String DB_TYPE = "db_type";

    public static final String HOST = "host";

    public static final String PORT = "port";

    public static final String USER_NAME = "user_name";

    public static final String PASS_WORD = "pass_word";

    public static final String DB_NAME = "db_name";

    public static final String TABLE_NAME = "table_name";

    public static final String DISABLED = "disabled";

    public static final String MIN_CONNECTION_SIZE = "min_connection_size";

    public static final String MAX_CONNECTION_SIZE = "max_connection_size";

    @Override
    public Serializable pkVal() {
        return null;
    }

}