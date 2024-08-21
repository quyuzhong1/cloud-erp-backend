package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class ShopifyReceiverDmpHandler extends DmpInputDoNextDmpHandler{
    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
        String id = dmpInputTaskEntity.getId();
        List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getParentTaskId, id).list();
        if (CollectionUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        //订单拓展
        DmpInputTaskEntity dmpInputTaskExtensionsEntity = list.stream().filter(req -> "1823265128548686459".equals(req.getCfgInputId())).findFirst().orElse(null);
        List<ParamData> extensionsDataList = new ArrayList<>();
        extensionsDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskExtensionsEntity.getId()));
        List<Map<String, Object>> dmpInputExtensionsMongoChildList = mongoService.findMongoData(extensionsDataList, "shopify_extensions_data");
        List<Map<String, Object>> detailList = super.getDetailList(dmpInputMongoEntity);
        if(CollUtil.isNotEmpty(detailList)) {
            for(Map<String, Object> detail : detailList) {

                //收获地址信息
                Object shippingAddressObj = detail.get("shippingAddress");
                if (ObjectUtil.isNotEmpty(shippingAddressObj)) {
                    Map<String, Object> shippingAddressMap = (Map<String, Object>) shippingAddressObj;
                    detail.put("buyerName", shippingAddressMap.get("name"));
                    detail.put("receiverName", shippingAddressMap.get("name"));
                    detail.put("province", shippingAddressMap.get("province"));
                    detail.put("city", shippingAddressMap.get("city"));
                    detail.put("mainStreet", shippingAddressMap.get("address1"));
                    detail.put("secondStreet", shippingAddressMap.get("address2"));
                    detail.put("receiverTelNumber", shippingAddressMap.get("phone"));
                    detail.put("secondPhone", shippingAddressMap.get("second_phone"));
                    detail.put("country", shippingAddressMap.get("countryCode"));
                    detail.put("postCode", shippingAddressMap.get("zip"));
                }

                //买家电话2
                Object customerObj = detail.get("customer");
                if (ObjectUtil.isNotEmpty(customerObj)) {
                    Map<String, Object> customerMap = (Map<String, Object>) customerObj;
                    detail.put("mainPhone", customerMap.get("phone"));
                }
                Map<String, Object> extensionsMap = dmpInputExtensionsMongoChildList.stream().filter(req -> String.valueOf(req.get("orderId")).equals(detail.get("orderId")+"")).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(extensionsMap)) {
                    detail.put("receiverTaxNo", extensionsMap.get("taxNo"));
                }
            }
        }
        return detailList;
    }
}
