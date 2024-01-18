package com.erp.oms.aliexpress.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 订单明细
 * @Author yl
 * @Date 2023-11-29 10:27
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class AliExpressOrderDetail implements Serializable {

    /**
     * 买家备注（订单级别）
     */
    @SerializedName("memo")
    private String memo;


    /**
     * 加密oaid
     */
    @SerializedName("oaid")
    private String oaid;


    /**
     * 收货地址信息
     */
    @SerializedName("receipt_address")
    private ReceiptInfo receiptAddress;


    /**
     * 买家信息
     */
    @SerializedName("buyer_info")
    private BuyerInfo  buyerInfo;

    /**
     * 买家全名
     */
    @SerializedName("buyer_signer_fullname")
    private String buyerSignerFullname;


    /**
     * 买家登录id
     */
    @SerializedName("buyerloginid")
    private String buyerloginid;


    /**
     * 卖家名称
     */
    @SerializedName("seller_signer_fullname")
    private String sellerSignerFullname;

    /**
     * 订单金额
     */
    @SerializedName("order_amount")
    private AmountInfo  orderAmount;


    /**
     * 物流费用
     */
    @SerializedName("logistics_amount")
    private AmountInfo  logisticsAmount;


    /**
     * 物流信息
     */
    @SerializedName("logistic_info_list")
    private List<LogisitcsDTO> logisticInfoList;



    /**
     * 子订单列表
     */
    @SerializedName("child_order_list")
    private List<OrderItemDetail>  childOrderList;













}
