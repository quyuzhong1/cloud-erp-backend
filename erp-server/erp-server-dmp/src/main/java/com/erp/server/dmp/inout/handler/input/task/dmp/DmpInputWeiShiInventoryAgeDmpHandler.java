package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import lombok.extern.slf4j.Slf4j;
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
public class DmpInputWeiShiInventoryAgeDmpHandler extends DmpInputDoChildDmpHandler {

    @Override
    protected List<Map<String, Object>> getDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoEntityList, String childMongoStorageName) {
        List<ParamData> paramDataList = new ArrayList<>();
        List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getParentTaskId, inputTaskId).list();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, list.get(0).getId()));
        return mongoService.findMongoData(paramDataList, childMongoStorageName);
    }

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("DmpInputGoodCangInventoryAgeDmpHandler afterConvertData");
//        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
//            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
//            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
//                // 2019-09-25
//                Object ibaFifoTimeObj = dmpDataMap.get("iba_fifo_time");
//                if (null != ibaFifoTimeObj){
//                    dmpDataMap.put("iba_fifo_time", LocalDate.parse(ibaFifoTimeObj.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
//                }
//            }
//        }
    }


    @Override
    protected void putDmpId(List<Map<String, Object>> dmpInputMongoChildEntityList) {
        DmpCfgInputConvertEntity mainConvertId = this.getMainConvertId();
        String parentStorageName = mainConvertId.getStorageName();
        ServiceImpl parentServiceImpl = this.getServiceImpl(parentStorageName);
        QueryWrapper<?> wrapper = new QueryWrapper<>();
        wrapper.eq(INPUT_TASK_ID, inputTaskId);
        List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);
        // 单号和店铺唯一
        Map<String, String> billNoIdMap = new HashMap<>();

        if (CollUtil.isNotEmpty(listMaps)) {
            for (Map<String, Object> listMap : listMaps) {
                String sourcePlatform = listMap.getOrDefault("source_platform", "").toString();
                String platformWarehouseCode = listMap.getOrDefault("platform_warehouse_code", "").toString();
                String productSku = listMap.getOrDefault("product_sku", "").toString();
                // 唯一
                String uniqueId = CharSequenceUtil.format("{}_{}_{}", sourcePlatform, platformWarehouseCode, productSku);
                // 单号配店铺
                billNoIdMap.put(uniqueId, listMap.get(BaseEntity.FIELD_ID).toString());
            }
        }
        for (Map<String, Object> dmpInputMongoChildEntity : dmpInputMongoChildEntityList) {
            String sourcePlatform = DmpBasicSystemCodeEnum.WEI_SHI.getCode();
            String warehouseCode = dmpInputMongoChildEntity.get("areaCode").toString();
            String product_sku = dmpInputMongoChildEntity.getOrDefault("referenceNo", "").toString();
            String uniqueId = CharSequenceUtil.format("{}_{}_{}", sourcePlatform, warehouseCode, product_sku);
            String dmpId = billNoIdMap.get(uniqueId);
            dmpInputMongoChildEntity.put(MAIN_ID, dmpId);
        }
    }
}
