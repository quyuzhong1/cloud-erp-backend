package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * B2B三方发货单
 * </p>
 *
 * @author zdy
 * @since 2025-11-26
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("b2b_third_delivery")
public class B2bThirdDeliveryEntity extends BaseEntity<B2bThirdDeliveryEntity> {

    /**
    * 单据编号
    */
    @TableField("code")
    private String code;
    /**
    * 单据状态
     * ThirdDeliveryStatusEnum
    */
    @TableField("status")
    private String status;
    /**
    * 销售订单id
    */
    @TableField("so_id")
    private String soId;
    /**
    * 销售订单编码
    */
    @TableField("so_code")
    private String soCode;
    /**
    * 发货时间
    */
    @TableField("delivery_time")
    private LocalDateTime deliveryTime;
    /**
    * 仓库组织id
    */
    @TableField("warehouse_org_id")
    private String warehouseOrgId;
    /**
    * 仓库组织名称
    */
    @TableField("warehouse_org_name")
    private String warehouseOrgName;
    /**
    * 平台订单编号
    */
    @TableField("platform_order_code")
    private String platformOrderCode;
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
    * 三方仓代码
    */
    @TableField("third_warehouse_code")
    private String thirdWarehouseCode;
    /**
    * 虚拟仓库id
    */
    @TableField("virtual_warehouse_id")
    private String virtualWarehouseId;
    /**
    * 仓库操作类型
     * WarehouseOperationTypeEnum
    */
    @TableField("warehouse_operation_type")
    private String warehouseOperationType;
    /**
    * 仓库操作描述
    */
    @TableField("operation_desc")
    private String operationDesc;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;
    /**
    * 渠道名称
    */
    @TableField("logistics_channel_name")
    private String logisticsChannelName;
    /**
     * 渠道编码
     */
    @TableField("logistics_channel_code")
    private String logisticsChannelCode;
    /**
    * 交货方式
     * DeliveryMethodEnum
    */
    @TableField("delivery_method")
    private String deliveryMethod;
    /**
    * 物流跟踪号
    */
    @TableField("track_no")
    private String trackNo;
    /**
    * 收货人
    */
    @TableField("receiver_name")
    private String receiverName;
    /**
    * 联系人电话
    */
    @TableField("tel_number")
    private String telNumber;
    /**
     * 国家id
     */
    @TableField("country_id")
    private String countryId;
    /**
    * 收货国家
    */
    @TableField("country_name")
    private String countryName;
    /**
    * 省/州
    */
    @TableField("province")
    private String province;
    /**
    * 城市
    */
    @TableField("city")
    private String city;
    /**
    * 邮编
    */
    @TableField("postal_code")
    private String postalCode;
    /**
     *
     * 详细地址（取值销售订单下推receiveAddressId查询）
     */
    @TableField("receive_address")
    private String receiveAddress;
    /**
    * 是否API发货
    */
    @TableField("is_api_delivery")
    private Boolean isApiDelivery;

    /**
     * 异常原因
     */
    @TableField("error_message")
    private String errorMessage;
    /**
     * 推送类型
     * B2BDeliveryPushTypeEnum
     */
    @TableField("push_type")
    private String pushType;


    public static final String CODE = "code";

    public static final String STATUS = "status";

    public static final String SO_ID = "so_id";

    public static final String SO_CODE = "so_code";

    public static final String DELIVERY_TIME = "delivery_time";

    public static final String WAREHOUSE_ORG_ID = "warehouse_org_id";

    public static final String WAREHOUSE_ORG_NAME = "warehouse_org_name";

    public static final String PLATFORM_ORDER_CODE = "platform_order_code";

    public static final String DELIVERY_WAREHOUSE_ID = "delivery_warehouse_id";

    public static final String DELIVERY_WAREHOUSE_NAME = "delivery_warehouse_name";

    public static final String WAREHOUSE_OPERATION_TYPE = "warehouse_operation_type";

    public static final String OPERATION_DESC = "operation_desc";

    public static final String REMARK = "remark";

    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String LOGISTICS_CHANNEL_NAME = "logistics_channel_name";

    public static final String DELIVERY_METHOD = "delivery_method";

    public static final String TRACK_NO = "track_no";

    public static final String RECEIVER_NAME = "receiver_name";

    public static final String TEL_NUMBER = "tel_number";

    public static final String COUNTRY_NAME = "country_name";

    public static final String PROVINCE = "province";

    public static final String CITY = "city";

    public static final String POSTAL_CODE = "postal_code";

    public static final String IS_API_DELIVERY = "is_api_delivery";

    @Override
    public Serializable pkVal() {
        return null;
    }

}