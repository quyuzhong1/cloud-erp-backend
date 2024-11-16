package com.erp.tms.aliexpress.model.handover.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.erp.tms.aliexpress.model.handover.AddressInfo;
import com.erp.tms.aliexpress.model.handover.Features;
import com.erp.tms.aliexpress.model.handover.SellerParcelOrder;
import com.erp.tms.aliexpress.model.handover.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author zdy
 * @ClassName CommitRequest
 * @description: TODO
 * @date 2024年02月01日
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommitRequest implements Serializable {

    /**
     * 店铺LP单号列表
     */
    @JSONField(name = "seller_parcel_order_list")
    private List<SellerParcelOrder> sellerParcelOrderList;
    /**
     * 是否跳过不允许组包的小包（包括：状态不允许组包，已经组过包，揽收仓不一致，发货方式不一致）
     */
    @JSONField(name = "skip_invalid_parcel")
    private Boolean skipInvalidParcel;

    /**
     * 大包备注
     */
    @JSONField(name = "remark")
    private String remark;

    /**
     * 退件信息
     */
    @JSONField(name = "return_info")
    private AddressInfo returnInfo;

    /**
     * 揽收信息
     */
    @NotNull(message = "揽收信息不能为空")
    @JSONField(name = "pickup_info")
    private AddressInfo pickInfo;

    /**
     *
     * 要创建交接单的小包编码集合，数量上限1000(即将下线，请使用seller_parcel_order_list)
     */
    @JSONField(name = "order_code_list")
    private List<String> orderCodeList;
    /**
     * 大包重量
     */
    @JSONField(name = "weight")
    private BigDecimal weight;
    /**
     * 交接单id
     */
    @JSONField(name = "handover_order_id")
    private String handoverOrderId;
    /**
     * 用户信息
     */
    @NotNull(message = "用户信息不能为空")
    @JSONField(name = "user_info")
    private UserInfo userInfo;
    /**
     * 重量单位，克:g, 千克:kg，默认g
     */
    @JSONField(name = "weight_unit")
    private String weightUnit;
    /**
     * 交接单类型：cainiao_pickup(菜鸟揽收)、self_post(自寄)、self_send(自送)
     */
    @JSONField(name = "type")
    private String type;
    /**
     *
     * ISV名称，ISV：ISV-ISV英文或拼音名称、商家ERP：SELLER-商家英文或拼音名称
     */
    @NotBlank(message = "ISV名称不能为空")
    @JSONField(name = "client")
    private String client;
    /**
     * 多语言
     */
    @NotBlank(message = "多语言不能为空")
    @JSONField(name = "locale")
    private String locale;
    /**
     * 扩展字段
     */
    @JSONField(name = "features")
    private Features features;
    /**
     * 预约交货方式（bigbag：大包预约，batch：批次预约））
     */
    @JSONField(name = "appointment_type")
    private String appointmentType;
    /**
     * 国内运单号（交接单类型type为self_post(自寄)时必填）
     */
    @JSONField(name = "domestic_tracking_no")
    private String domesticTrackingNo;
    /**
     * 国内物流公司id（交接单类型type为self_post(自寄)时必填）
     */
    @JSONField(name = "domestic_logistics_company_id")
    private String domesticLogisticsCompanyId;
    /**
     *
     * 国内物流公司编码（交接单类型type为self_post(自寄)时必填）
     */
    @JSONField(name = "domestic_logistics_company")
    private String domesticLogisticsCompany;
}
