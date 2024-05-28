package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 第三方系统映射关系表
 * </p>
 *
 * @author hyj
 * @since 2024-05-17
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("third_mapping")
public class ThirdMappingEntity extends BaseEntity<ThirdMappingEntity> {

    /**
     * 类型  warehouse 仓库 shop 店铺
     */
    @TableField("type")
    private String type;
    /**
     * 系统表id
     */
    @TableField("sys_id")
    private String sysId;
    /**
     * 系统表名称
     */
    @TableField("sys_name")
    private String sysName;
    /**
     * 生效日期
     */
    @TableField("effective_time")
    private LocalDateTime effectiveTime;
    /**
     * 失效日期
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;
    /**
     * 是否失效 true 失效 false 未失效
     */
    @TableField("is_expire")
    private Boolean isExpire;
    /**
     * 第三方id
     */
    @TableField("third_info_id")
    private String thirdInfoId;
    /**
     * 第三方id
     */
    @TableField("third_id")
    private String thirdId;
    /**
     * 第三方编码
     */
    @TableField("third_code")
    private String thirdCode;
    /**
     * 第三方名称
     */
    @TableField("third_name")
    private String thirdName;
    /**
     * 第三方系统类型：lingxing领星，wangdian旺店通
     */
    @TableField("third_sys_type")
    private String thirdSysType;
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;


    public static final String TYPE = "type";

    public static final String SYS_ID = "sys_id";

    public static final String SYS_NAME = "sys_name";

    public static final String EFFECTIVE_TIME = "effective_time";

    public static final String EXPIRE_TIME = "expire_time";

    public static final String IS_EXPIRE = "is_expire";

    public static final String THIRD_ID = "third_id";

    public static final String THIRD_CODE = "third_code";

    public static final String THIRD_NAME = "third_name";

    public static final String THIRD_SYS_TYPE = "third_sys_type";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

    public ThirdMappingEntity(String thirdInfoId){
        this.thirdInfoId = thirdInfoId;
    }
}