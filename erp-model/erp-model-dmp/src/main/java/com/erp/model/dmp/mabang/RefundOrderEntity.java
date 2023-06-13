package com.erp.model.dmp.mabang;

import com.erp.model.dmp.mabang.item.RefundOrderItemEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class RefundOrderEntity {
    /**
     * 唯一编号 示例：544786
     */
    private String id;
    /**
     * 退款订单编号 示例：0
     */
    private Integer refundOrderId;
    /**
     * 退款流水号 示例：null
     */
    private String refundPaypalId;
    /**
     * 退款货币类型 示例：RMB
     */
    private String refundMoneyType;
    /**
     * 退款金额 示例：1.0000
     */
    private String refundMoney;
    /**
     * 申请退款金额 示例：0.0000
     */
    private BigDecimal applyRefundMoney;
    /**
     * 退款类型：1、未收到货部分退款 2、未收到货全额退款 3、已收到货部分退款 4、已收到货全额退款 示例：1
     */
    private Integer refundType;
    /**
     * 退款原因 示例：444444444
     */
    private String refundReasonName;
    /**
     * 平台退款原因 示例：BUYER_CANCEL
     */
    private String refundReasonDesc;
    /**
     * 退款到平台的备注
     */
    private String note;
    /**
     * 本地的备注
     */
    private String content;
    /**
     * 是否通过接口:1、通过接口 其他的都为非接口
     */
    private Integer type;
    /**
     * 状态：1、新建退款 2、审核中（原主管审核）3、财务审核 4、成功 5、失败 6、作废 示例：2
     */
    private Integer flag;
    /**
     * 申请时间 示例：2023-11-27 11:14:48
     */
    private String createTime;
    /**
     * object 接口调用失败描述 示例：null
     */
    private String errorDescr;
    /**
     * 订单状态 （参考订单列表接口）示例：2
     */
    private Integer orderStatus;
    /**
     * 店铺ID 示例:2021188107
     */
    private String shopId;
    /**
     * 店铺名 示例：Ebay-GCXSZY
     */
    private String shopName;
    /**
     * 平台名 示例：ebay
     */
    private String platformName;
    /**
     * 销售订单编号 示例：wk20211102192657_5
     */
    private String platformOrderId;
    /**
     * 退款订单编号 示例：null
     */
    private String refundplatformOrderId;
    /**
     * 退款时间 示例：2023-11-10 15:28:24
     */
    private String refundTime;
    /**
     * 汇率 示例：1.0000000
     */
    private BigDecimal currencyRate;
    /**
     * 物流渠道id
     */
    private String expressType;
    /**
     * 渠道名 示例：null
     */
    private String logisticsChannelName;
    /**
     * 国家二字码 示例：US
     */
    private String countryCode;
    /**
     * 国家中文名 示例：美国
     */
    private String countryCn;
    /**
     * 国家英文名 示例：United States
     */
    private String countryEn;
    /**
     * 平台交易号 示例：123
     */
    private String salesRecordNumber;
    /**
     * 买家用户Id 示例：123
     */
    private String buyerUserId;
    /**
     * 买家名 示例
     */
    private String buyerName;
    /**
     * 币种 示例：RMB
     */
    private String currencyId;
    /**
     * 原始订单金额 示例：0.0000
     */
    private BigDecimal itemTotalOrigin;
    /**
     * 原始订单运费金额 示例：0.0000
     */
    private BigDecimal shippingTotalOrigin;
    /**
     * 付款流水号 示例：
     */
    private String paypalId;
    /**
     * 订单时间 示例：2023-11-30 00:00:00
     */
    private String orderTime;
    /**
     * 发货时间 示例：2023-01-24 20:26:22
     */
    private String expressTime;
    /**
     * 来源1:平台;2:手工 示例：2
     */
    private Integer source;
    /**
     * 平台图片json 示例：null
     */
    private String pictureUrl;
    /**
     * 最后更新时间 示例：2023-11-29 16:04:34
     */
    private String updateTime;
    /**
     * 退款描述 示例：null
     */
    private String refundText;
    /**
     * 客诉编号 示例：null
     */
    private String complaintId;
    /**
     * 包裹单号 示例：
     */
    private String trackNumber;
    private List<RefundOrderItemEntity> productList;
    /**
     * 清洗数据
     */
    private Boolean isClean;

    @Override
    public String toString() {
        return "RefundOrderEntity{" +
                "id='" + id + '\'' +
                ", refundOrderId=" + refundOrderId +
                ", refundPaypalId='" + refundPaypalId + '\'' +
                ", refundMoneyType='" + refundMoneyType + '\'' +
                ", refundMoney='" + refundMoney + '\'' +
                ", applyRefundMoney=" + applyRefundMoney +
                ", refundType=" + refundType +
                ", refundReasonName='" + refundReasonName + '\'' +
                ", refundReasonDesc='" + refundReasonDesc + '\'' +
                ", note='" + note + '\'' +
                ", content='" + content + '\'' +
                ", type=" + type +
                ", flag=" + flag +
                ", createTime='" + createTime + '\'' +
                ", errorDescr='" + errorDescr + '\'' +
                ", orderStatus=" + orderStatus +
                ", shopId='" + shopId + '\'' +
                ", shopName='" + shopName + '\'' +
                ", platformName='" + platformName + '\'' +
                ", platformOrderId='" + platformOrderId + '\'' +
                ", refundplatformOrderId='" + refundplatformOrderId + '\'' +
                ", refundTime='" + refundTime + '\'' +
                ", currencyRate=" + currencyRate +
                ", expressType='" + expressType + '\'' +
                ", logisticsChannelName='" + logisticsChannelName + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", countryCn='" + countryCn + '\'' +
                ", countryEn='" + countryEn + '\'' +
                ", salesRecordNumber='" + salesRecordNumber + '\'' +
                ", buyerUserId='" + buyerUserId + '\'' +
                ", buyerName='" + buyerName + '\'' +
                ", currencyId='" + currencyId + '\'' +
                ", itemTotalOrigin=" + itemTotalOrigin +
                ", shippingTotalOrigin=" + shippingTotalOrigin +
                ", paypalId='" + paypalId + '\'' +
                ", orderTime='" + orderTime + '\'' +
                ", expressTime='" + expressTime + '\'' +
                ", source=" + source +
                ", pictureUrl='" + pictureUrl + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", refundText='" + refundText + '\'' +
                ", complaintId='" + complaintId + '\'' +
                ", trackNumber='" + trackNumber + '\'' +
                ", productList=" + productList +
                '}';
    }
}
