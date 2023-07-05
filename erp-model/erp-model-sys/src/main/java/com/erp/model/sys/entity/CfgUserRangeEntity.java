package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 用户区间配置表
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_user_range")
public class CfgUserRangeEntity extends BaseEntity<CfgUserRangeEntity> {


    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
    * 区间类型
    */
    @TableField("type")
    private String type;

    /**
    * 开始值
    */
    @TableField("start_value")
    private Integer startValue;

    /**
    * 结束值
    */
    @TableField("end_value")
    private Integer endValue;

    /**
    * 用户id
    */
    @TableField("user_id")
    private String userId;

    public static final String NAME = "name";

    public static final String TYPE = "type";

    public static final String START_VALUE = "start_value";

    public static final String END_VALUE = "end_value";

    public static final String USER_ID = "user_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}