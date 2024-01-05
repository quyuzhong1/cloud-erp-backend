package com.erp.oms.aliexpress.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
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
    @JSONField(name = "memo")
    private String  memo;

    /**
     * 收货地址信息
     */
    @JSONField(name = "receipt_address")
    private ReceiptInfo  receiptInfo;


    /**
     * 买家信息
     */
    @JSONField(name = "buyer_info")
    private BuyerInfo  buyerInfo;

    /**
     * 买家全名
     */
    @JSONField(name = "buyer_signer_fullname")
    private String buyerSignerFullname;


    /**
     * 买家登录id
     */
    @JSONField(name = "buyerloginid")
    private String buyerLoginId;


    /**
     * 卖家名称
     */
    @JSONField(name = "seller_signer_fullname")
    private String sellerSignerFullname;

    /**
     * 订单金额
     */
    @JSONField(name = "order_amount")
    private AmountInfo  orderAmount;


    /**
     * 物流费用
     */
    @JSONField(name = "logistics_amount")
    private AmountInfo  logisticsAmount;






    /**
     * 子订单列表
     */
    @JSONField(name = "child_order_list")
    private List<OrderItemDetail>  childOrderList;













}
