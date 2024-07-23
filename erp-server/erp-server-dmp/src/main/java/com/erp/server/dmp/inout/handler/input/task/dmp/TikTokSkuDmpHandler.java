package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.utils.CollectionUtils;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope("prototype")
public class TikTokSkuDmpHandler extends DmpInputDoChildDmpHandler {


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
                        l.put("platformCreateTime", dmpInputMongoChild.get("createTime"));
                        l.put("platformUpdateTime", dmpInputMongoChild.get("updateTime"));
                        l.put("spuId", dmpInputMongoChild.get("fid"));

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
                                l.put("parent_category_name", categoryChainsList.get(0).get("localName"));
                                l.put("category_name", categoryChainsList.get(1).get("localName"));
                            }
                        }
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
