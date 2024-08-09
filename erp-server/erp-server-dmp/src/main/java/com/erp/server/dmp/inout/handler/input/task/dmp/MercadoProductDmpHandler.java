package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.response.AliExpressOrder;
import com.erp.oms.aliexpress.dto.response.OrderItemDetail;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.DmpSoDetailService;
import com.erp.server.dmp.service.DmpSoInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class MercadoProductDmpHandler extends DmpInputDbConvertDmpHandler{

    @Autowired
    private DmpSoInfoService dmpSoInfoService;

    @Autowired
    private DmpSoDetailService dmpSoDetailService;

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        String parentTaskId = dmpInputTaskEntity.getParentTaskId();
        List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getId, parentTaskId).list();
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            List<Map<String, Object>> mongoDataMaps = dmpInputDataDmpRelationMap.getKey();
            Map<String, Object> mongoDataMap = mongoDataMaps.get(0);
            for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                dmpDataMap.put("nextLevelId", list.get(0).getNextLevelId());
                dmpDataMap.put("shopId", list.get(0).getNextLevelId());

                //创建时间
                Object createTimeObj = mongoDataMap.get("dateCreated");
                if(createTimeObj != null) {
                    dmpDataMap.put("platformCreateTime", createTimeObj);
                }

                //修改时间
                Object updateTimeObj = mongoDataMap.get("lastUpdated");
                if(updateTimeObj != null) {
                    dmpDataMap.put("platformUpdateTime", updateTimeObj);
                }
            }
        }
    }
}
