package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


/**
 * <p>
 * 海外仓入库单
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("overseas_warehouse_inbound")
public class OverseasWarehouseInboundEntity extends BaseEntity<OverseasWarehouseInboundEntity> {

    /**
     * 单据编号
     */
    @TableField("code")
    private String code;
    /**
     * 平台类型: goodcang=谷仓，iml=艾姆勒
     */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
     * 来源单号
     */
    @TableField("source_code")
    private String sourceCode;
    /**
     * 来源ID
     */
    @TableField("source_id")
    private String sourceId;
    /**
     * 来源类型
     */
    @TableField("source_type")
    private String sourceType;
    /**
     * 入库类型
     */
    @TableField("instock_type")
    private String instockType;
    /**
     * 交货方式
     */
    @TableField("delivery_mode")
    private String deliveryMode;
    /**
     * 入库状态
     */
    @TableField("instock_status")
    private String instockStatus;
    /**
     * 发货仓名称
     */
    @TableField("delivery_warehouse_name")
    private String deliveryWarehouseName;
    /**
     * 发货仓ID
     */
    @TableField("delivery_warehouse_id")
    private String deliveryWarehouseId;
    /**
     * 中转仓名称
     */
    @TableField("transfer_warehouse_name")
    private String transferWarehouseName;
    /**
     * 中转仓ID
     */
    @TableField("transfer_warehouse_id")
    private String transferWarehouseId;
    /**
     * 海外中转仓库代号
     */
    @TableField("platform_transfer_warehouse_code")
    private String platformTransferWarehouseCode;
    /**
     * 海外目的仓库代号
     */
    @TableField("platform_to_warehouse_code")
    private String platformToWarehouseCode;
    /**
     * 目的仓名称
     */
    @TableField("to_warehouse_name")
    private String toWarehouseName;
    /**
     * 目的仓ID
     */
    @TableField("to_warehouse_id")
    private String toWarehouseId;
    /**
     * 物流方式
     */
    @TableField("logistics_method")
    private String logisticsMethod;
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    /**
     * 最新签收时间
     */
    @TableField(value = "receive_time")
    private LocalDateTime receiveTime;
    /**
     * 预计到达时间
     */
    @TableField(value = "estimated_arrival_date")
    private LocalDateTime estimatedArrivalDate;
    /**
     * 手动完结原因
     */
    @TableField("finish_reason")
    private String finishReason;
    /**
     * 第三方唯一编码
     */
    @TableField("overseas_warehouse_inbound_id")
    private String overseasWarehouseInboundId;

    /**
     * 物流跟踪号
     */
    @TableField("tracking_no")
    private String trackingNo;

    /**
     * 快递单号
     */
    @TableField("express_no")
    private String expressNo;

    /**
     * 报关方式代号:
     * /api/wms/common/enumDropDown?type=OverseasCustomsTypeNew
     */
    @TableField("customs_type")
    private String customsType;

    /**
     * 物流产品代码
     */
    @TableField("logistics_product_code")
    private String logisticsProductCode;

    /**
     * 报关方式名称
     */
    @TableField("customs_type_name")
    private String customsTypeName;
    /**
     * 物流产品名称
     */
    @TableField("logistics_product_name")
    private String logisticsProductName;
    /**
     * 预计揽收日期
     */
    @TableField("estimated_collect_date")
    private LocalDateTime estimatedCollectDate;
    /**
     * 字典)省ID
     */
    @TableField("dict_province_id")
    private String dictProvinceId;
    /**
     * 字典)城市ID
     */
    @TableField("dict_city_id")
    private String dictCityId;
    /**
     * 字典)地区ID
     */
    @TableField("dict_district_id")
    private String dictDistrictId;
    /**
     * 字典)省名称
     */
    @TableField("dict_province_name")
    private String dictProvinceName;
    /**
     * 字典)城市名称
     */
    @TableField("dict_city_name")
    private String dictCityName;
    /**
     * 字典)地区名称
     */
    @TableField("dict_district_name")
    private String dictDistrictName;
    /**
     * 平台)省ID
     */
    @TableField("platform_province_id")
    private String platformProvinceId;
    /**
     * 平台)城市ID
     */
    @TableField("platform_city_id")
    private String platformCityId;
    /**
     * 平台)地区ID
     */
    @TableField("platform_district_id")
    private String platformDistrictId;
    /**
     * 姓
     */
    @TableField("first_name")
    private String firstName;
    /**
     * 名
     */
    @TableField("last_name")
    private String lastName;
    /**
     * 手机号
     */
    @TableField("mobile")
    private String mobile;
    /**
     * 详情地址
     */
    @TableField("street")
    private String street;
    /**
     * 全地址
     */
    @TableField("full_address")
    private String fullAddress;

    /**
     * 地址邮编
     */
    @TableField("zipcode")
    private String zipcode;

    /**
     * 揽收地址国家代号
     */
    @TableField("collect_country_code")
    private String collectCountryCode;

    /**
     * 报关类型
     */
    @TableField("declare_type")
    private String declareType;
    /**
     * 数量
     */
    @TableField(exist = false)
    private Integer count;

    public static final String DICT_PLATFORM = "dict_platform";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_TYPE = "source_type";

    public static final String INSTOCK_TYPE = "instock_type";

    public static final String INSTOCK_STATUS = "instock_status";

    public static final String DELIVERY_WAREHOUSE_NAME = "delivery_warehouse_name";

    public static final String DELIVERY_WAREHOUSE_ID = "delivery_warehouse_id";

    public static final String TRANSFER_WAREHOUSE_NAME = "transfer_warehouse_name";

    public static final String TRANSFER_WAREHOUSE_ID = "transfer_warehouse_id";

    public static final String TO_WAREHOUSE_NAME = "to_warehouse_name";

    public static final String TO_WAREHOUSE_ID = "to_warehouse_id";

    public static final String LOGISTICS_METHOD = "logistics_method";

    public static final String RECEIVE_TIME = "receive_time";

    public static final String ESTIMATED_ARRIVAL_DATE = "estimated_arrival_date";

    public static final String FINISH_REASON = "finish_reason";

    public static final String FINISH_STATUS = "finish_status";

    public static final String OVERSEAS_WAREHOUSE_INBOUND_ID = "overseas_warehouse_inbound_id";
}