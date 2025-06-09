package com.erp.server.dmp.inout.handler.input.task.dmp.jifeng;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpThirdReturnInboundEntity;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDoNextDmpHandler;
import com.sdk.wms.goodcang.dto.response.GoodCangReturnInstockResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
public class JiFengReturnInstockDetailDmpHandler extends DmpInputDoNextDmpHandler {

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
        Object detailListObj = dmpInputMongoEntity.get("skuList");
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
    @Override
    protected void afterConvertData(Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("JiFengReturnInstockDetailDmpHandler afterConvertData：");
        String parentTableName = SqlHelper.table(DmpThirdReturnInboundEntity.class).getTableName();
        ServiceImpl parentServiceImpl = this.getServiceImpl(parentTableName);
        QueryWrapper<?> wrapper = new QueryWrapper<>();
        wrapper.eq(INPUT_TASK_ID, inputTaskId);
        List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);

        Map<String, String> dmpReturnIdMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(listMaps)) {
            for(Map<String, Object> listMap : listMaps) {
                String key = listMap.get("id").toString();
                dmpReturnIdMap.put(key, listMap.get(BaseEntity.FIELD_ID).toString());
            }
        }
        for (List<TreeMap<String, Object>> dmpInputMongoList : dmpInputDataDmpRelationMaps.values()) {
            for (TreeMap<String, Object> detailMap : dmpInputMongoList) {
                String returnOrderId = detailMap.get("mainId").toString();
                String dmpId = dmpReturnIdMap.get(returnOrderId);
                detailMap.put("mainId", dmpId);
            }
        }
        log.debug("DmpInputAmzReportFbaReturnInstockDetailDmpHandler afterConvertData：");
    }
}
