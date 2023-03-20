package com.common.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @description: 实体类基类
 * @author Will
 * @date: 2023/2/21 9:18
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class BaseEntity<T extends BaseEntity<?>> extends Model<T> {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;


    /**
     * 创建人id
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name", fill = FieldFill.INSERT)
    private String CreateUserName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time" , fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改人id
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name", fill = FieldFill.INSERT_UPDATE)
    private String updateUserName;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    /**
     * 逻辑删除字段
     */
    @TableField(value = "is_deleted")
    @TableLogic
    private Boolean isDeleted;

    public static final String ID = "id";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public static final String VERSION = "version";

    public static final String IS_DELETED = "is_deleted";
    public static final String DELETED_USER_ID = "deleted_user_id";

    public static final String DELETED_TIME = "deleted_time";


    @Override
    public Serializable pkVal() {
        return this.id;
    }

    public BaseEntity(String id) {
        super();
        if(null != id){
            this.id = id;
        }
    }
}