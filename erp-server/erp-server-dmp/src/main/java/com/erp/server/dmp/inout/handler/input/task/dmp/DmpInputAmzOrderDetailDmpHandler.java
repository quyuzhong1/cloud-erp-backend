package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformOrderDetailDTO;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.sdk.oms.amz.spapi.model.orders.Money;
import com.erp.sdk.oms.amz.spapi.model.orders.Order;
import com.erp.sdk.oms.amz.spapi.model.orders.OrderItem;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
//        List<Map<String, Object>> dmpInputMongoChildEntityList = new ArrayList<>();
//        for (Map<String, Object> dmpInputMongoChild : dmpInputMongoChildList) {
//            OrderItem item = JSON.parseObject(JSON.toJSONString(dmpInputMongoChild), OrderItem.class);
//            // item总价
//            Money itemPrice = item.getItemPrice();
//            // 金额
//            detailDTO.setAmount(new java.math.BigDecimal(null == itemPrice ? "0" : itemPrice.getAmount()));
//
//            // 计算单价
//            java.math.BigDecimal price = java.math.BigDecimal.ZERO;
//            if (null != item.getQuantityOrdered() && 0 < item.getQuantityOrdered()){
//                price = detailDTO.getAmount().divide(java.math.BigDecimal.valueOf(item.getQuantityOrdered()), 2, RoundingMode.DOWN);
//            }
//
//            // 单价
//            detailDTO.setPrice(price);
//
//            // 币别（原币）
//            detailDTO.setCurrency(null == itemPrice ? "" : itemPrice.getCurrencyCode());
//            // 汇率
//            detailDTO.setExchangeRate(java.math.BigDecimal.ONE);
//            // 建议售价（本位币）
//            detailDTO.setAdvicePrice(java.math.BigDecimal.ZERO);
//            // 含税成本（本位币）
//            detailDTO.setTaxCost(BigDecimal.ZERO);
//            // 来源明细id
//            detailDTO.setSourceDetailId(item.getOrderItemId());
//            // 标签json
//            detailDTO.setLabelJson("");
//            // 库存组织id
//            detailDTO.setWarehouseOrgId("");
//            // 库存组织名称
//            detailDTO.setWarehouseOrgName("");
//            // 库位
//            detailDTO.setWarehouseLocation("");
//        }
//        return dmpInputMongoChildEntityList;
        return dmpInputMongoChildList;
    }

}
