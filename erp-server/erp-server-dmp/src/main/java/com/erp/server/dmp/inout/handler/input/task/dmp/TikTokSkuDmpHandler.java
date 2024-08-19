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
import com.common.core.utils.ObjectUtils;
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
public class TikTokSkuDmpHandler extends DmpInputDoChildDmpHandler {


    @Resource
    private DmpProductInfoService dmpProductInfoService;


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
                    //状态
                    skuList.forEach(l -> {
                        //创建时间
                        Object createTimeObj = dmpInputMongoChild.get("createTime");
                        if (createTimeObj != null) {
                            // 使用Instant类将Unix时间戳转换为LocalDateTime对象
                            LocalDateTime payTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(createTimeObj + "")), ZoneId.systemDefault());
                            l.put("platformCreateTime", payTime);
                        }

                        //修改时间
                        Object updateTimeObj = dmpInputMongoChild.get("updateTime");
                        if (updateTimeObj != null) {
                            // 使用Instant类将Unix时间戳转换为LocalDateTime对象
                            LocalDateTime payTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(updateTimeObj + "")), ZoneId.systemDefault());
                            l.put("platformUpdateTime", payTime);
                        }

                        l.put("spuId", dmpInputMongoChild.get("fid"));
                        l.put("skuNo", l.get("sellerSku"));

                        Object price = l.get("price");
                        if (ObjectUtil.isNotEmpty(price)) {
                            Map<String,Object> priceMap = (Map<String,Object>) price;
                            l.put("sellPrice", priceMap.get("salePrice"));
                        }

                        l.put("name", dmpInputMongoChild.get("title"));

                        List<Map<String, Object>> mainImages = (List<Map<String, Object>>) dmpInputMongoChild.get("mainImages");
                        if (CollectionUtil.isNotEmpty(mainImages)) {
                            List<String> urls = (List<String>) mainImages.get(0).get("urls");
                            l.put("imageUrls", urls.get(0));
                        }

                        //状态
                        Object statusObj = dmpInputMongoChild.get("status");
                        if (statusObj != null) {
                            String status = String.valueOf(statusObj);
                            if ("ACTIVATE".equalsIgnoreCase(status)) {
                                l.put("status", "1");
                            } else {
                                l.put("status", "3");
                            }
                        }

                        //品牌
                        Object brandObj = dmpInputMongoChild.get("brand");
                        if (brandObj != null) {
                            Map<String, String> brandMap = (Map<String, String>) brandObj;
                            l.put("brandName", brandMap.get("name"));
                        }

                        //类目
                        Object categoryChainsObj = dmpInputMongoChild.get("categoryChains");
                        if (categoryChainsObj != null) {
                            List<Map<String, String>> categoryChainsList = (List<Map<String, String>>) categoryChainsObj;
                            if (CollectionUtil.isNotEmpty(categoryChainsList)) {
                                l.put("parentCategoryName", categoryChainsList.get(0).get("localName"));
                                l.put("categoryName", categoryChainsList.get(1).get("localName"));
                            }
                        }


                        //规格属性
                        List<Map<String, Object>> salesAttributes = (List<Map<String, Object>>) l.get("salesAttributes");
                        if (CollectionUtil.isNotEmpty(salesAttributes)) {
                            String specifics = "";
                            for (Map<String, Object> salesAttribute : salesAttributes) {
                                specifics = specifics + salesAttribute.get("name") + ":" + salesAttribute.get("valueName") + " ";
                            }
                            specifics = specifics.trim();
                            l.put("specifics", specifics);
                        }

                        //包装信息
                        Object packageDimensionsObj = dmpInputMongoChild.get("packageDimensions");
                        if (packageDimensionsObj != null) {
                            Map<String, String> packageDimensionsMap = (Map<String, String>) packageDimensionsObj;
                            l.put("packageLength", packageDimensionsMap.get("length"));
                            l.put("packageWidth", packageDimensionsMap.get("width"));
                            l.put("packageHeight", packageDimensionsMap.get("height"));
                            l.put("packageUnit", packageDimensionsMap.get("unit"));
                        }

                        //产品重量
                        Object packageWeightObj = dmpInputMongoChild.get("packageWeight");
                        if (packageWeightObj != null) {
                            Map<String, String> packageWeightMap = (Map<String, String>) packageWeightObj;
                            l.put("grossWeight", packageWeightMap.get("value"));
                            l.put("weightUnit", packageWeightMap.get("unit"));
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
