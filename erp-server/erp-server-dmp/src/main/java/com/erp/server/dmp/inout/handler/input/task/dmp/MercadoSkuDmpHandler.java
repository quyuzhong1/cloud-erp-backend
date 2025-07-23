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
import com.erp.model.dmp.entity.DmpProductInfoEntity;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.DmpProductInfoService;
import com.erp.server.dmp.service.DmpSkuInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class MercadoSkuDmpHandler extends DmpInputDbConvertDmpHandler {

    @Resource
    private DmpProductInfoService dmpProductInfoService;

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        List<String> fidList = new ArrayList<>();
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> listListEntry : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = listListEntry.getValue();
            List<String> fids = dmpDataMaps.stream().map(req -> req.get("fid").toString()).distinct().collect(Collectors.toList());
            fidList.addAll(fids);
        }
        List<DmpProductInfoEntity> dmpProductInfoEntityList = dmpProductInfoService.lambdaQuery().in(DmpProductInfoEntity::getSpuId, fidList).list();

        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            List<Map<String, Object>> mongoDataMaps = dmpInputDataDmpRelationMap.getKey();
            Map<String, Object> mongoDataMap = mongoDataMaps.get(0);


            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                dmpDataMap.put("nextLevelId", nextLevelId);
                dmpDataMap.put("shopId", nextLevelId);

                List<DmpProductInfoEntity> productInfoEntities = dmpProductInfoEntityList.stream().filter(req -> req.getSpuId().equals(dmpDataMap.get("fid").toString())).collect(Collectors.toList());
                if (CollectionUtil.isEmpty(productInfoEntities)) {
                    continue;
                }
                dmpDataMap.put("mainId", productInfoEntities.get(0).getId());

                dmpDataMap.put("platformCreateTime", mongoDataMap.get("dateCreated"));
                dmpDataMap.put("platformUpdateTime", mongoDataMap.get("lastUpdated"));
                dmpDataMap.put("spuId", dmpDataMap.get("fid"));

                if (ObjectUtil.isNotEmpty(dmpDataMap.get("pictures"))) {
                    List<Map<String, Object>> pictures = (List<Map<String, Object>>) dmpDataMap.get("pictures");
                    if (CollectionUtil.isNotEmpty(pictures)) {
                        Object url = pictures.get(0).get("url");
                        dmpDataMap.put("imageUrls", url);
                    }
                }

                //状态
                Object statusObj = dmpDataMap.get("status");
                if (statusObj != null) {
                    String status = String.valueOf(statusObj);
                    if ("active".equalsIgnoreCase(status)) {
                        dmpDataMap.put("status", "1");
                    } else {
                        dmpDataMap.put("status", "3");
                    }
                }

                //父平台产品
                Object itemRelations = mongoDataMap.get("itemRelations");
                if (null != itemRelations) {
                    List<Object> itemRelationsList = (List<Object>) itemRelations;
                    if(CollectionUtil.isNotEmpty(itemRelationsList)){
                        Object itemRelation = itemRelationsList.get(0);
                        Map<String, Object> itemRelationMap = (Map<String, Object>) itemRelation;
                        Object parentId = itemRelationMap.get("id");
                        if(null != parentId){
                            dmpDataMap.put("platformParentSpuNo", String.valueOf(parentId));
                        }
                    }
                }

                //规格属性
                Object attributesObj = dmpDataMap.get("attributes");

                if (ObjectUtils.isNotEmpty(attributesObj)) {
                    List<Map<String, Object>> attributesList = (List<Map<String, Object>>) attributesObj;
                    if (CollectionUtil.isNotEmpty(attributesList)) {
                        for (Map<String, Object> map : attributesList) {
                            if ("BRAND".equalsIgnoreCase(String.valueOf(map.get("fid")))) {
                                dmpDataMap.put("brandName", map.get("valueName"));
                            }

                            if ("PACKAGE_LENGTH".equalsIgnoreCase(String.valueOf(map.get("fid")))) {
                                List<Map<String, Object>> valuesList = (List<Map<String, Object>>) map.get("values");
                                if (CollectionUtils.isNotEmpty(valuesList)) {
                                    Map<String, Object> structMap = (Map<String, Object>) valuesList.get(0).get("struct");
                                    dmpDataMap.put("packageLength", structMap.get("number"));
                                }
                            }
                            if ("PACKAGE_WIDTH".equalsIgnoreCase(String.valueOf(map.get("fid")))) {
                                List<Map<String, Object>> valuesList = (List<Map<String, Object>>) map.get("values");
                                if (CollectionUtils.isNotEmpty(valuesList)) {
                                    Map<String, Object> structMap = (Map<String, Object>) valuesList.get(0).get("struct");
                                    dmpDataMap.put("packageWidth", structMap.get("number"));
                                }
                            }
                            if ("PACKAGE_HEIGHT".equalsIgnoreCase(String.valueOf(map.get("fid")))) {
                                List<Map<String, Object>> valuesList = (List<Map<String, Object>>) map.get("values");
                                if (CollectionUtils.isNotEmpty(valuesList)) {
                                    Map<String, Object> structMap = (Map<String, Object>) valuesList.get(0).get("struct");
                                    dmpDataMap.put("packageHeight", structMap.get("number"));
                                    dmpDataMap.put("packageUnit", structMap.get("unit"));
                                }
                            }

                            if ("PACKAGE_WEIGHT".equalsIgnoreCase(String.valueOf(map.get("fid")))) {
                                List<Map<String, Object>> valuesList = (List<Map<String, Object>>) map.get("values");
                                if (CollectionUtils.isNotEmpty(valuesList)) {
                                    Map<String, Object> structMap = (Map<String, Object>) valuesList.get(0).get("struct");
                                    dmpDataMap.put("grossWeight", structMap.get("number"));
                                    dmpDataMap.put("weightUnit", structMap.get("unit"));
                                }
                            }

                            if ("SELLER_SKU".equalsIgnoreCase(String.valueOf(map.get("fid")))) {
                                dmpDataMap.put("skuNo", map.get("valueName"));
                            }
                        }
                    }
                }
            }
        }
    }
}
