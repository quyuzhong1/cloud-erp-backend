package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Scope("prototype")
public class MercadoSkuDmpHandler extends DmpInputDoChildDmpHandler {


    @Override
    protected List<Map<String, Object>> getDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoEntityList, String childMongoStorageName) {
        List<Map<String, Object>> dmpInputMongoChildEntityList = new ArrayList<>();
        List<ParamData> paramDataList = new ArrayList<>();
        List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getParentTaskId, inputTaskId).list();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, list.get(0).getId()));
        List<Map<String, Object>> dmpInputMongoChildList = mongoService.findMongoData(paramDataList, childMongoStorageName);

        if (CollUtil.isNotEmpty(dmpInputMongoChildList)) {
            for (Map<String, Object> dmpInputMongoChild : dmpInputMongoChildList) {
                Object skuObj = dmpInputMongoChild.get("skus");
                if (skuObj != null) {
                    List<Map<String, Object>> skuList = (List<Map<String, Object>>) skuObj;

                    skuList.forEach(l -> {
                        l.put("platformCreateTime", dmpInputMongoChild.get("dateCreated"));
                        l.put("platformUpdateTime", dmpInputMongoChild.get("lastUpdated"));
                        l.put("spuId", dmpInputMongoChild.get("fid"));

                        if (ObjectUtil.isNotEmpty(dmpInputMongoChild.get("pictures"))) {
                            List<Map<String, Object>> pictures = (List<Map<String, Object>>) dmpInputMongoChild.get("pictures");
                            if (CollectionUtil.isNotEmpty(pictures)) {
                                Object url = pictures.get(0).get("url");
                                l.put("imageUrls", url);
                            }
                        }

                        //状态
                        Object statusObj = dmpInputMongoChild.get("status");
                        if (statusObj != null) {
                            String status = String.valueOf(statusObj);
                            if ("active".equalsIgnoreCase(status)) {
                                l.put("status", "1");
                            } else {
                                l.put("status", "3");
                            }
                        }

                        //规格属性
                        Object attributesObj = dmpInputMongoChild.get("attributes");

                        if (ObjectUtils.isNotEmpty(attributesObj)) {
                            List<Map<String, Object>> attributesList = (List<Map<String, Object>>) attributesObj;
                            if (CollectionUtil.isNotEmpty(attributesList)) {
                                for (Map<String, Object> map : attributesList) {
                                    if ("BRAND".equalsIgnoreCase(String.valueOf(map.get("fid")))) {
                                        l.put("brandName", map.get("valueName"));
                                    }

                                    if ("PACKAGE_LENGTH".equalsIgnoreCase(String.valueOf(map.get("fid")))) {
                                        List<Map<String, Object>> valuesList = (List<Map<String, Object>>) map.get("values");
                                        if (CollectionUtils.isNotEmpty(valuesList)) {
                                            Map<String, Object> structMap = (Map<String, Object>) valuesList.get(0).get("struct");
                                            l.put("packageLength", structMap.get("number"));
                                        }
                                    }
                                    if ("PACKAGE_WIDTH".equalsIgnoreCase(String.valueOf(map.get("fid")))) {
                                        List<Map<String, Object>> valuesList = (List<Map<String, Object>>) map.get("values");
                                        if (CollectionUtils.isNotEmpty(valuesList)) {
                                            Map<String, Object> structMap = (Map<String, Object>) valuesList.get(0).get("struct");
                                            l.put("packageWidth", structMap.get("number"));
                                        }
                                    }
                                    if ("PACKAGE_HEIGHT".equalsIgnoreCase(String.valueOf(map.get("fid")))) {
                                        List<Map<String, Object>> valuesList = (List<Map<String, Object>>) map.get("values");
                                        if (CollectionUtils.isNotEmpty(valuesList)) {
                                            Map<String, Object> structMap = (Map<String, Object>) valuesList.get(0).get("struct");
                                            l.put("packageHeight", structMap.get("number"));
                                            l.put("packageUnit", structMap.get("unit"));
                                        }
                                    }

                                    if ("PACKAGE_WEIGHT".equalsIgnoreCase(String.valueOf(map.get("fid")))) {
                                        List<Map<String, Object>> valuesList = (List<Map<String, Object>>) map.get("values");
                                        if (CollectionUtils.isNotEmpty(valuesList)) {
                                            Map<String, Object> structMap = (Map<String, Object>) valuesList.get(0).get("struct");
                                            l.put("grossWeight", structMap.get("number"));
                                        }
                                    }
                                }
                            }
                        }
                        l.put(DmpInputMongoHandler.MONGO_BASE_ID, dmpInputMongoChild.get(DmpInputMongoHandler.MONGO_BASE_ID));
                        l.put(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, dmpInputMongoChild.get(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID));
                    });
                    dmpInputMongoChildEntityList.addAll(skuList);
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
