package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


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
@TableName("wms_carton_bill")
public class WmsCartonBillEntity extends BaseEntity<WmsCartonBillEntity> {

    /**
    * first_mile_carton表id
    */
    @TableField("carton_id")
    private String cartonId;
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
    /**
     * 来源id
     */
    @TableField("source_id")
    private String sourceId;

    public static final String CARTON_ID = "carton_id";

    public static final String CARTON_DETAIL_ID = "carton_detail_id";

    public static final String BOX_NO = "box_no";

    public static final String BOX_DESC = "box_desc";

    @Override
    public Serializable pkVal() {
        return null;
    }

}