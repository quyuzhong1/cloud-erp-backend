package com.erp.server.dmp.inout.handler.input.task.dmp.damai;

import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDoNextDmpHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

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
public class DaMaiSkuInfoDmpHandler extends DmpInputDoNextDmpHandler {

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
        List<Map<String, Object>> resultList = new LinkedList<>();
        Map<String, Object> map = dmpInputMongoEntity;
        map.put("status",dmpInputMongoEntity.get("status"));
        map.put("spuId",dmpInputMongoEntity.get("skuCode"));
        map.put("skuId",dmpInputMongoEntity.get("barCode"));
        map.put("name",dmpInputMongoEntity.get("skuName"));
        map.put("skuNo",dmpInputMongoEntity.get("customerSkuCode"));
        map.put("platformCreateTime",dmpInputMongoEntity.get("createTime"));
        map.put("imageUrls",dmpInputMongoEntity.get("skuUrl"));
        resultList.add(map);
        return resultList;
    }
}
