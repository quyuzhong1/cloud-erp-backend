package com.erp.model.wms.entity;

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
 * 发货单箱子信息明细表
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("first_mile_carton_bill")
public class FirstMileCartonBillEntity extends BaseEntity<FirstMileCartonBillEntity> {

    /**
    * first_mile_carton表id
    */
    @TableField("carton_id")
    private String cartonId;
    /**
    * first_mile_carton_detail表id
    */
    @TableField("carton_detail_id")
    private String cartonDetailId;
    /**
    * 箱号
    */
    @TableField("box_no")
    private String boxNo;
    /**
    * 描述（sku*qty+sku*qty+...）
    */
    @TableField("box_desc")
    private String boxDesc;


    public static final String CARTON_ID = "carton_id";

    public static final String CARTON_DETAIL_ID = "carton_detail_id";

    public static final String BOX_NO = "box_no";

    public static final String BOX_DESC = "box_desc";

    @Override
    public Serializable pkVal() {
        return null;
    }

}