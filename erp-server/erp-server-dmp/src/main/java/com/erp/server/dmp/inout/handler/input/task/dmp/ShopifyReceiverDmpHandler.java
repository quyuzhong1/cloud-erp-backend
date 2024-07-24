package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class ShopifyReceiverDmpHandler extends DmpInputDoNextDmpHandler{
    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){

        List<Map<String, Object>> detailList = super.getDetailList(dmpInputMongoEntity);
        if(CollUtil.isNotEmpty(detailList)) {
            for(Map<String, Object> detail : detailList) {

                //收获地址信息
                Object shippingAddressObj = detail.get("shippingAddress");
                if (ObjectUtil.isNotEmpty(shippingAddressObj)) {
                    Map<String, Object> shippingAddressMap = (Map<String, Object>) shippingAddressObj;
                    detail.put("buyer_name", shippingAddressMap.get("name"));
                    detail.put("province", shippingAddressMap.get("province"));
                    detail.put("city", shippingAddressMap.get("city"));
                    detail.put("main_street", shippingAddressMap.get("address1"));
                    detail.put("second_street", shippingAddressMap.get("address2"));
                    detail.put("main_phone", shippingAddressMap.get("phone"));
                    detail.put("second_phone", shippingAddressMap.get("second_phone"));
                }

                //买家电话2
                Object customerObj = detail.get("customer");
                if (ObjectUtil.isNotEmpty(customerObj)) {
                    Map<String, Object> customerMap = (Map<String, Object>) customerObj;
                    detail.put("second_phone", customerMap.get("phone"));
                }

            }
        }

        return detailList;
    }
}
