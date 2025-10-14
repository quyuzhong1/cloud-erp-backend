package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 组包计划主表
 * </p>
 *
 * @author zdy
 * @since 2025-10-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("package_plan")
public class PackagePlanEntity extends BaseEntity<PackagePlanEntity> {

    /**
    * 单据编号
    */
    @TableField("code")
    private String code;
    /**
    * 平台
    */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
    * 店铺id
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * 大包单号
    */
    @TableField("package_no")
    private String packageNo;
    /**
    * 组包状态 PackageStatusEnum
    */
    @TableField("package_status")
    private String packageStatus;
    /**
    * 交接标签下载
    */
    @TableField("is_handover_download")
    private Boolean isHandoverDownload;
    /**
    * 箱数
    */
    @TableField("box_num")
    private Integer boxNum;
    /**
    * 发货仓库id
    */
    @TableField("delivery_warehouse_id")
    private String deliveryWarehouseId;
    /**
    * 发货仓库名称
    */
    @TableField("delivery_warehouse_name")
    private String deliveryWarehouseName;
    /**
    * 打印交接单状态  not 未打印  already 已打印
     * PackagePrintStatusEnum
    */
    @TableField("print_handover_status")
    private String printHandoverStatus;
    /**
    * 打印订单状态  not 未打印  already 已打印
     * PackagePrintStatusEnum
    */
    @TableField("print_order_status")
    private String printOrderStatus;

    /**
     * 大包运单号
     */
    @TableField("transport_no")
    private String transportNo;
    /**
     * 大包交接面单
     */
    @TableField("handover_label_url")
    private String handoverLabelUrl;

    public static final String CODE = "code";

    public static final String PLATFORM = "platform";

    public static final String SHOP_ID = "shop_id";

    public static final String PACKAGE_NO = "package_no";

    public static final String PACKAGE_STATUS = "package_status";

    public static final String IS_HANDOVER_DOWNLOAD = "is_handover_download";

    public static final String BOX_NUM = "box_num";

    public static final String DELIVERY_WAREHOUSE_ID = "delivery_warehouse_id";

    public static final String DELIVERY_WAREHOUSE_NAME = "delivery_warehouse_name";

    public static final String PRINT_HANDOVER_STATUS = "print_handover_status";

    public static final String PRINT_ORDER_STATUS = "print_order_status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}