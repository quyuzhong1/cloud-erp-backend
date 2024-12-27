package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputLxOrderDetailNextDmpHandler extends DmpInputDoNextDmpHandler {


    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
        Object detailListObj = dmpInputMongoEntity.get("item_info");
        if (null == detailListObj) {
            return Collections.emptyList();
        }
        JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(detailListObj));
        if (CollectionUtils.isEmpty(jsonArray)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> resultList = new LinkedList<>();

        for (Object detailObj : jsonArray) {
            JSONObject jsonObject = (JSONObject) JSON.toJSON(detailObj);
            resultList.add(jsonObject);
        }
        return resultList;
    }

}
