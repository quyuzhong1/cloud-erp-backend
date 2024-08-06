package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.utils.CollectionUtils;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.entity.DmpProductInfoEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.DmpProductInfoService;
import jodd.util.StringUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class TikTokSkuDmpHandler extends DmpInputDbConvertDmpHandler {


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

                //创建时间
                Object createTimeObj = mongoDataMap.get("createTime");
                if (createTimeObj != null) {
                    // 使用Instant类将Unix时间戳转换为LocalDateTime对象
                    LocalDateTime payTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(createTimeObj + "")), ZoneId.systemDefault());
                    dmpDataMap.put("platformCreateTime", payTime);
                }

                //修改时间
                Object updateTimeObj = mongoDataMap.get("updateTime");
                if (updateTimeObj != null) {
                    // 使用Instant类将Unix时间戳转换为LocalDateTime对象
                    LocalDateTime payTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(updateTimeObj + "")), ZoneId.systemDefault());
                    dmpDataMap.put("platformUpdateTime", payTime);
                }

                dmpDataMap.put("spuId", mongoDataMap.get("fid"));

                List<Map<String, Object>> mainImages = (List<Map<String, Object>>) dmpDataMap.get("mainImages");
                if (CollectionUtil.isNotEmpty(mainImages)) {
                    List<String> urls = (List<String>) mainImages.get(0).get("urls");
                    dmpDataMap.put("imageUrls", urls.get(0));
                }

                //状态
                Object statusObj = dmpDataMap.get("status");
                if (statusObj != null) {
                    String status = String.valueOf(statusObj);
                    if ("ACTIVATE".equalsIgnoreCase(status)) {
                        dmpDataMap.put("status", "1");
                    } else {
                        dmpDataMap.put("status", "3");
                    }
                }

                //品牌
                Object brandObj = dmpDataMap.get("brand");
                if (brandObj != null) {
                    Map<String, String> brandMap = (Map<String, String>) brandObj;
                    dmpDataMap.put("brandName", brandMap.get("name"));
                }

                //类目
                Object categoryChainsObj = dmpDataMap.get("categoryChains");
                if (categoryChainsObj != null) {
                    List<Map<String, String>> categoryChainsList = (List<Map<String, String>>) categoryChainsObj;
                    if (CollectionUtil.isNotEmpty(categoryChainsList)) {
                        dmpDataMap.put("parent_category_name", categoryChainsList.get(0).get("localName"));
                        dmpDataMap.put("category_name", categoryChainsList.get(1).get("localName"));
                    }
                }


                //规格属性
                List<Map<String, Object>> salesAttributes = (List<Map<String, Object>>) dmpDataMap.get("salesAttributes");
                if (CollectionUtil.isNotEmpty(salesAttributes)) {
                    String specifics = "";
                    for (Map<String, Object> salesAttribute : salesAttributes) {
                        specifics = specifics + salesAttribute.get("name") + ":" + salesAttribute.get("valueName") +" ";
                    }
                    specifics = specifics.trim();
                    dmpDataMap.put("specifics", specifics);
                }

                //包装信息
                Object packageDimensionsObj = dmpDataMap.get("packageDimensions");
                if (packageDimensionsObj != null) {
                    Map<String, String> packageDimensionsMap = (Map<String, String>) packageDimensionsObj;
                    dmpDataMap.put("packageLength", packageDimensionsMap.get("length"));
                    dmpDataMap.put("packageWidth", packageDimensionsMap.get("width"));
                    dmpDataMap.put("packageHeight", packageDimensionsMap.get("height"));
                    dmpDataMap.put("packageUnit", packageDimensionsMap.get("unit"));
                }

                //产品重量
                Object packageWeightObj = dmpDataMap.get("packageWeight");
                if (packageWeightObj != null) {
                    Map<String, String> packageWeightMap = (Map<String, String>) packageWeightObj;
                    dmpDataMap.put("grossWeight", packageWeightMap.get("value"));
                    dmpDataMap.put("packageUnit", packageWeightMap.get("unit"));
                }
            }
        }
    }
}
