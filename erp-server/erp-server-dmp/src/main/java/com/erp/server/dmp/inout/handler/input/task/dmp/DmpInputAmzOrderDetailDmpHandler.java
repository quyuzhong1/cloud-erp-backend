package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.erp.sdk.oms.amz.spapi.model.orders.Money;
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
                dmpInputMongoChild.put("afterAmount", amount);

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
                    dmpInputMongoChild.put("sellPrice", price);
                }
            }

        }
        return dmpInputMongoChildList;
    }

}
