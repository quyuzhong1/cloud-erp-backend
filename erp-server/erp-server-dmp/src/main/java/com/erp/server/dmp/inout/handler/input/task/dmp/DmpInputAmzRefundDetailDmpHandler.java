package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpSoRefundInfoEntity;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;
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
public class DmpInputAmzRefundDetailDmpHandler extends DmpInputDoNextDmpHandler {

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
        log.debug("DmpInputAmzRefundDetailDmpHandler getDetailList 处理");



        return Collections.singletonList(dmpInputMongoEntity);
    }


    @Override
    protected void afterConvertData(Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("DmpInputAmzRefundDetailDmpHandler afterConvertData：begin");
        String parentTableName = SqlHelper.table(DmpSoRefundInfoEntity.class).getTableName();
        ServiceImpl parentServiceImpl = this.getServiceImpl(parentTableName);
        QueryWrapper<?> wrapper = new QueryWrapper<>();
        wrapper.eq(INPUT_TASK_ID, inputTaskId);
        List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);
        Map<String, String> dmpRefundIdMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(listMaps)) {
            for(Map<String, Object> listMap : listMaps) {
                dmpRefundIdMap.put(listMap.get("third_code").toString(), listMap.get(BaseEntity.ID).toString());
            }
        }
        for (List<TreeMap<String, Object>> dmpInputMongoList : dmpInputDataDmpRelationMaps.values()) {
            for (TreeMap<String, Object> detailMap : dmpInputMongoList) {
                String refundOrderId = detailMap.get("thirdOrderCode").toString();
                String dmpId = dmpRefundIdMap.get(refundOrderId);
                detailMap.put("mainId", dmpId);
            }
        }
        log.debug("DmpInputAmzRefundDetailDmpHandler afterConvertData：end");
    }

}
