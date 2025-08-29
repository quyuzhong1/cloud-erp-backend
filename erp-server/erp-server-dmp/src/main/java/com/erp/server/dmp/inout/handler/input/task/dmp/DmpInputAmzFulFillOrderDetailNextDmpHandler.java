package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.LingxingPlatformCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzFulFillOrderDetailNextDmpHandler extends DmpInputDoNextDmpHandler {

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
        Object detailListObj = dmpInputMongoEntity.get("fulfillmentOrderItems");
        if (null == detailListObj) {
            return Collections.emptyList();
        }
        JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(detailListObj));
        if (CollectionUtils.isEmpty(jsonArray)) {
            return Collections.emptyList();
        }
        Object platformInfoObj = dmpInputMongoEntity.get("fulfillmentOrder");
        if (null == platformInfoObj) {
            return Collections.emptyList();
        }
        JSONObject platformInfoJson = JSON.parseObject(JSON.toJSONString(platformInfoObj));
        String code = platformInfoJson.getString("sellerFulfillmentOrderId");

        JSONArray parseArray = JSON.parseArray(JSON.toJSONString(platformInfoObj));
        if (CollectionUtils.isEmpty(parseArray)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> resultList = new LinkedList<>();
        for (Object detailObj : jsonArray) {
            JSONObject jsonObject = (JSONObject) JSON.toJSON(detailObj);
            jsonObject.put("code", code);
            Integer quantity = jsonObject.getInteger("quantity");
            Integer cancelledQuantity = jsonObject.getInteger("cancelledQuantity");
            Integer unfulfillableQuantity = jsonObject.getInteger("unfulfillableQuantity");
            jsonObject.put("qty", quantity - cancelledQuantity - unfulfillableQuantity);
            resultList.add(jsonObject);
        }
        return resultList;
    }

}
