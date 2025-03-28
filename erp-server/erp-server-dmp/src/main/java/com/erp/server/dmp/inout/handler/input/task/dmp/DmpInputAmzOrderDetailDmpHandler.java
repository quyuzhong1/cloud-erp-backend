package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.erp.sdk.oms.amz.spapi.model.orders.Money;
import com.google.gson.annotations.SerializedName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * dmp处理金蝶明细子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Service
@Scope("prototype")
public class DmpInputAmzOrderDetailDmpHandler extends DmpInputAmzOrderDoChildDmpHandler {


    @Override
    protected List<Map<String, Object>> afterDoDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoChildList) {
        if (CollUtil.isEmpty(dmpInputMongoChildList)) {
            return dmpInputMongoChildList;
        }
        for (Map<String, Object> dmpInputMongoChild : dmpInputMongoChildList) {
            Object itemPriceObj = dmpInputMongoChild.get("itemPrice");
            if (null != itemPriceObj) {
                // 订单买家信息
                Money itemPrice = JSON.parseObject(JSON.toJSONString(itemPriceObj), Money.class);
                // 金额
                BigDecimal amount = new BigDecimal(null == itemPrice ? "0" : itemPrice.getAmount());
                dmpInputMongoChild.put("amount", amount);

                Object quantityOrderedObj = dmpInputMongoChild.get("quantityOrdered");
                if (null != quantityOrderedObj){
                     int quantityOrdered = Integer.parseInt(quantityOrderedObj.toString());
                    // 计算单价
                    BigDecimal price = BigDecimal.ZERO;
                    if (0 < quantityOrdered) {
                        price = amount.divide(BigDecimal.valueOf(quantityOrdered), 2, RoundingMode.DOWN);
                    }
                    // 单价
                    dmpInputMongoChild.put("price", price);
                    dmpInputMongoChild.put("sellPriceOrigin", price);
                }
            }

            //优惠
            Object promotionDiscountObj = dmpInputMongoChild.get("promotionDiscount");
            if (promotionDiscountObj != null) {
                Map<String, Object> promotionDiscountMap = (Map<String, Object>) promotionDiscountObj;
                dmpInputMongoChild.put("discount", promotionDiscountMap.get("amount"));
            }

            // 产品销售税
            Object itemTaxObj = dmpInputMongoChild.get("itemTax");
            if (null != itemTaxObj){
                Money itemTax = JSON.parseObject(JSON.toJSONString(itemTaxObj), Money.class);
                dmpInputMongoChild.put("itemTax", itemTax.getAmount());
            }

            // 运费税
            Object shippingTaxObj = dmpInputMongoChild.get("shippingTax");
            if (null != shippingTaxObj){
                Money shippingTax = JSON.parseObject(JSON.toJSONString(shippingTaxObj), Money.class);
                dmpInputMongoChild.put("shippingTax", shippingTax.getAmount());
            }

            Object buyerInfoObj = dmpInputMongoChild.get("buyerInfo");
            if (null != buyerInfoObj){
                JSONObject buyerInfoJson = (JSONObject) JSON.toJSON(buyerInfoObj);
                Object giftWrapPriceObj = buyerInfoJson.get("giftWrapPrice");
                Object giftWrapTaxObj = buyerInfoJson.get("giftWrapTax");
                if (null != giftWrapPriceObj){
                    // 礼品包装费(备用)
                    Money giftWrapPrice= JSON.parseObject(JSON.toJSONString(giftWrapPriceObj), Money.class);
                    dmpInputMongoChild.put("giftWrapPrice", giftWrapPrice.getAmount());
                }
                if (null != giftWrapTaxObj){
                    // 礼品包装税
                    Money giftWrapTax = JSON.parseObject(JSON.toJSONString(giftWrapTaxObj), Money.class);
                    dmpInputMongoChild.put("giftWrapTax", giftWrapTax.getAmount());
                }
            }

            // 促销折扣税
            Object promotionDiscountTaxObj = dmpInputMongoChild.get("promotionDiscountTax");
            if (null != promotionDiscountTaxObj){
                Money promotionDiscountTax = JSON.parseObject(JSON.toJSONString(promotionDiscountTaxObj), Money.class);
                dmpInputMongoChild.put("promotionDiscountTax", promotionDiscountTax.getAmount());
            }

            // 运费折扣税
            Object shippingDiscountTaxObj = dmpInputMongoChild.get("shippingDiscountTax");
            if (null != promotionDiscountTaxObj){
                Money shippingDiscountTax = JSON.parseObject(JSON.toJSONString(shippingDiscountTaxObj), Money.class);
                dmpInputMongoChild.put("shippingDiscountTax", shippingDiscountTax.getAmount());
            }

        }
        return dmpInputMongoChildList;
    }
}
