package com.erp.model.tms.entity;

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
 * 偏远邮编组
 * </p>
 *
 * @author jack
 * @since 2024-11-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("remote_postcode")
public class RemotePostcodeEntity extends BaseEntity<RemotePostcodeEntity> {

    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * code
    */
    @TableField("code")
    private String code;
    /**
    * 名称
    */
    @TableField("name")
    private String name;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 邮编组状态:true 禁用 false 启用
    */
    @TableField("disabled")
    private Boolean disabled;


    public static final String APPROVE_STATUS = "approve_status";

    public static final String CODE = "code";

    public static final String NAME = "name";

    public static final String REMARK = "remark";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}