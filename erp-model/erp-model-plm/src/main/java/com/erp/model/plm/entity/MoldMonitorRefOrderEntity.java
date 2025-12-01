package com.erp.model.plm.entity;

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
 * 模具监控关联单据
 * </p>
 *
 * @author jack
 * @since 2025-10-24
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mold_monitor_ref_order")
public class MoldMonitorRefOrderEntity extends BaseEntity<MoldMonitorRefOrderEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 单据类型
    */
    @TableField("business_type")
    private String businessType;
    /**
    * 单据id
    */
    @TableField("business_id")
    private String businessId;
    /**
    * 单据明细id
    */
    @TableField("business_detail_id")
    private String businessDetailId;


    public static final String MAIN_ID = "main_id";

    public static final String BUSINESS_TYPE = "business_type";

    public static final String BUSINESS_ID = "business_id";

    public static final String BUSINESS_DETAIL_ID = "business_detail_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}