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
 * 第三方平台与业务对接关联表
 * </p>
 *
 * @author Lambda
 * @since 2024-03-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("thirdparty_ref_business")
public class ThirdpartyRefBusinessEntity extends BaseEntity<ThirdpartyRefBusinessEntity> {

    /**
    * 第三方类型 目前是金蝶
    */
    @TableField("thirdparty_type")
    private String thirdpartyType;
    /**
    * 业务表id
    */
    @TableField("business_id")
    private String businessId;
    /**
    * 业务类 如单据的类型so 或者表名
    */
    @TableField("business_type")
    private String businessType;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 平台业务id 如金蝶id
    */
    @TableField("thirdparty_id")
    private String thirdpartyId;


    public static final String THIRDPARTY_TYPE = "thirdparty_type";

    public static final String BUSINESS_ID = "business_id";

    public static final String BUSINESS_TYPE = "business_type";

    public static final String FIELD_REMARK = "remark";

    public static final String THIRDPARTY_ID = "thirdparty_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}