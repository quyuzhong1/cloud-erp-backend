package com.erp.server.dmp.inout.handler.input.task.dmp.jifeng;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDoNextDmpHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Service
@Scope("prototype")
public class JiFengSkuInfoDmpHandler extends DmpInputDoNextDmpHandler {

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
        List<Map<String, Object>> resultList = new LinkedList<>();
        Map<String, Object> map = dmpInputMongoEntity;
        map.put("skuId",dmpInputMongoEntity.get("skuCode"));
        map.put("name",dmpInputMongoEntity.get("name"));
        map.put("platformUpdateTime",dmpInputMongoEntity.get("updateTime"));
        map.put("skuNo",dmpInputMongoEntity.get("sku"));
        map.put("platformCreateTime",dmpInputMongoEntity.get("createTime"));
        map.put("imageUrls",dmpInputMongoEntity.get("imgUrl"));
        resultList.add(map);
        return resultList;
    }
}
