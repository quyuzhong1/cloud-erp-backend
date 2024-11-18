package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 期初头程分摊
 * </p>
 *
 * @author zdy
 * @since 2024-08-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("init_first_mile_allocation")
public class InitFirstMileAllocationEntity extends BaseEntity<InitFirstMileAllocationEntity> {

    /**
    * 单据编号
    */
    @TableField("code")
    private String code;
    /**
    * 单据状态：waitSubmit=待提交，approveIng=审核中，reject=审核不通过，approve=已审核
    */
    @TableField("status")
    private String status;
    /**
     * 备注
     */
    private String remark;


    public static final String FIELD_CODE = "code";

    public static final String UPLOAD_STATUS = "upload_status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}