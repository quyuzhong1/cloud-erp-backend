package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;


/**
 * <p>
 * 三方仓发货单
 * </p>
 *
 * @author lrp
 * @since 2024-10-17
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("third_warehouse_delivery")
public class ThirdWarehouseDeliveryEntity extends BaseEntity<ThirdWarehouseDeliveryEntity> {

    /**
    * 三方仓出库单号
    */
    @TableField("code")
    private String code;
    /**
    * 销售单号
    */
    @TableField("so_code")
    private String soCode;
    /**
    * 销售id
    */
    @TableField("so_id")
    private String soId;
    /**
    * 平台
    */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
    * 平台订单号
    */
    @TableField("platform_code")
    private String platformCode;
    /**
    * 三方仓平台
    */
    @TableField("third_warehouse_platform")
    private String thirdWarehousePlatform;

    /**
     * 配送方式
     */
    @TableField("shipping_method")
    private String shippingMethod;

    /**
     * 状态
     */
    @TableField("status")
    private String status;

    @TableField(exist = false)
    private List<ThirdWarehouseDeliveryDetailEntity> detailEntityList;

    

    public static final String SO_CODE = "so_code";

    public static final String SO_ID = "so_id";

    public static final String DICT_PLATFORM = "dict_platform";

    public static final String PLATFORM_CODE = "platform_code";

    public static final String THIRD_WAREHOUSE_PLATFORM = "third_warehouse_platform";

    @Override
    public Serializable pkVal() {
        return null;
    }

}