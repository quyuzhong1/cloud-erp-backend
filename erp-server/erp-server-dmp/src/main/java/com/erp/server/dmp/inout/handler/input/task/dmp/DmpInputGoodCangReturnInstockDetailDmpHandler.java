package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sdk.oms.shopify.api.rest.model.ShopifyLineItem;
import com.sdk.oms.shopify.api.rest.model.ShopifyRefund;
import com.sdk.oms.shopify.api.rest.model.ShopifyRefundLineItem;
import com.sdk.wms.goodcang.dto.response.GoodCangReturnInstockResp;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Service
@Scope("prototype")
public class DmpInputGoodCangReturnInstockDetailDmpHandler extends DmpInputDoNextDmpHandler {

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
        Object detailListObj = dmpInputMongoEntity.get("product_detail");
        if (null == detailListObj) {
            return Collections.emptyList();
        }
        // 明细信息
        List<GoodCangReturnInstockResp.Product> productList = JSON.parseArray(JSON.toJSONString(detailListObj), GoodCangReturnInstockResp.Product.class);
        if (CollectionUtils.isEmpty(productList)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> resultList = new LinkedList<>();

        for (GoodCangReturnInstockResp.Product product : productList) {
            int receiveQty = product.getSellableQty() + product.getUnsellableQty() + product.getDestructionQty();
            JSONObject jsonObject = (JSONObject) JSON.toJSON(product);
            // 计算签收数量receive_qty
            jsonObject.put("receiveQty", receiveQty);
            resultList.add(jsonObject);
        }
        return resultList;
    }
}
