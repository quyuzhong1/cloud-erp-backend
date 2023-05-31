package com.erp.model.dmp.mabang;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class ReturnOrderEntity {

    private String _id;
    /**
     * 订单编号
     */
    private String platformOrderId;
    /**
     * 平台退货单编号
     */
    @SerializedName("platform_return_order")
    private String platformReturnOrder;
    /**
     * 马帮退货单编号
     */
    @SerializedName("return_orderId")
    private String returnOrderId;
    /**
     * 店铺编号
     */
    private String shopId;
    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 付款时间
     */
    private String paidTime;
    /**
     * 发货时间
     */
    private String expressTime;
    /**
     * 状态：1待处理;2已退款;3已重发;4已完成;5已作废
     */
    private Integer status;
    /**
     * 平台交易号
     */
    private String salesRecordNumber;
    /**
     * 订单金额
     */
    private BigDecimal orderFee;
    /**
     * 订单重量
     */
    private BigDecimal orderWeight;
    /**
     * 物流渠道编号
     */
    private Integer myLogisticsChannelId;
    /**
     * 物流渠道名称
     */
    private String myLogisticsChannelName;
    /**
     * 物流公司编号
     */
    private Integer myLogisticsId;
    /**
     * 物流公司名称
     */
    private String myLogisticsName;
    /**
     * 物流单号
     */
    private String trackNumber;
    /**
     * 平台编号
     */
    private String platformId;
    /**
     * 退包类型：1邮局退包;2买家退包 其余为自定义分类
     */
    private Integer type;
    /**
     * 国家二字码
     */
    private String countryCode;
    /**
     * 国家英文名称
     */
    private String countryNameEN;
    /**
     * 国家中文名称
     */
    private String countryNameCN;
    /**
     * 买家账号
     */
    private String buyerUserId;
    /**
     * 买家姓名
     */
    private String buyerName;
    /**
     * 登记人编号
     */
    private String employeeId;
    /**
     * 登记人姓名
     */
    private String employeeName;
    /**
     * 备注
     */
    private String remark;
    /**
     * 币种
     */
    private String currencyId;
    /**
     * 汇率
     */
    private BigDecimal currencyRate;
    /**
     * 创建时间
     */
    private String createDate;
    /**
     * 退款时间
     */
    private String refundTime;
    /**
     * 最近一次入库时间
     */
    private String inTime;
    /**
     * 更新时间
     */
    @SerializedName("update_time")
    private String updateTime;
    @SerializedName("item")
    private List<ReturnOrderItemEntity> item;
}
