package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Scope("prototype")
public class ShopifySkuDmpHandler extends DmpInputDoChildDmpHandler {


    @Override
    protected List<Map<String, Object>> getDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoEntityList, String childMongoStorageName) {
        List<Map<String, Object>> dmpInputMongoChildEntityList = new ArrayList<>();
        List<ParamData> paramDataList = new ArrayList<>();
        List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getParentTaskId, inputTaskId).list();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, list.get(0).getId()));
        List<Map<String, Object>> dmpInputMongoChildList = mongoService.findMongoData(paramDataList, childMongoStorageName);

        if (CollUtil.isNotEmpty(dmpInputMongoChildList)) {
            for (Map<String, Object> dmpInputMongoChild : dmpInputMongoChildList) {
                Object variantsObj = dmpInputMongoChild.get("variants");
                if (variantsObj != null) {
                    List<Map<String, Object>> variantsList = (List<Map<String, Object>>) variantsObj;

                    variantsList.forEach(l -> {
                        l.put("sku_no", dmpInputMongoChild.get("sku"));
                        l.put("name", dmpInputMongoChild.get("title"));
                        l.put("sell_price", dmpInputMongoChild.get("price"));

                        //图片
                        List<Map<String, Object>> imagesObj = (List<Map<String, Object>>) dmpInputMongoChild.get("images");
                        if (CollectionUtil.isNotEmpty(imagesObj)) {
                            l.put("imageUrls", String.valueOf(imagesObj.get(0).get("source")));
                        }

                        //状态
                        Object statusObj = dmpInputMongoChild.get("status");
                        if (statusObj != null) {
                            String status = String.valueOf(statusObj);
                            if ("active".equalsIgnoreCase(status)) {
                                l.put("status", "1");
                            } else if ("archived".equalsIgnoreCase(status)) {
                                l.put("status", "3");
                            } else if ("draft".equalsIgnoreCase(status)) {
                                l.put("status", "5");
                            }
                        }

                        l.put(DmpInputMongoHandler.MONGO_BASE_ID, dmpInputMongoChild.get(DmpInputMongoHandler.MONGO_BASE_ID));
                        l.put(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, dmpInputMongoChild.get(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID));
                    });
                    dmpInputMongoChildEntityList.addAll(variantsList);
                }
            }
        }
        return dmpInputMongoChildEntityList;
    }

    @Override
    protected void putDmpId(List<Map<String, Object>> dmpInputMongoChildEntityList) {
        DmpCfgInputConvertEntity mainConvertId = this.getMainConvertId();
        String parentStorageName = mainConvertId.getStorageName();
        ServiceImpl parentServiceImpl = this.getServiceImpl(parentStorageName);
        QueryWrapper<?> wrapper = new QueryWrapper<>();
        wrapper.eq(INPUT_TASK_ID, inputTaskId);
        List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);
        Map<String, String> billNoIdMap = new HashMap<>();
        if (CollUtil.isNotEmpty(listMaps)) {
            for (Map<String, Object> listMap : listMaps) {
                billNoIdMap.put(listMap.get("spu_id").toString(), listMap.get(BaseEntity.ID).toString());
            }
        }
        for (Map<String, Object> dmpInputMongoChildEntity : dmpInputMongoChildEntityList) {
            String billNo = dmpInputMongoChildEntity.get("spuId").toString();
            String dmpId = billNoIdMap.get(billNo);
            dmpInputMongoChildEntity.put(MAIN_ID, dmpId);
        }
    }

}
