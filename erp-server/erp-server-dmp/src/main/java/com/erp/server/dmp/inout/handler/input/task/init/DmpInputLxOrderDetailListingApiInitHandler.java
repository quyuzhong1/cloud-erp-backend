package com.erp.server.dmp.inout.handler.input.task.init;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.model.dmp.enums.LingxingPlatformCodeEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.sdk.third.lingxing.dto.Result;
import com.sdk.third.lingxing.utils.LingxingApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器下的api获取数据方式
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputLxOrderDetailListingApiInitHandler extends DmpInputInitHandler {


    public static final String LINGXING_TEMU_LISTING_DATA = "lingxing_temu_listing_data";

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
        if (StringUtils.isBlank(parentStorageName)) {
            return Collections.emptyList();
        }
        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
        List<Map<String, Object>> orderMongoData = mongoService.findMongoData(paramDataList, parentStorageName);
        // 校验TeMu订单产品信息是否已存在
        List<Pair<String, String>> skuIdAndStoreList = new LinkedList<>();

        for (Map<String, Object> orderMongoDatum : orderMongoData) {
            Object platformInfoObj = orderMongoDatum.getOrDefault("platform_info", "[]");
            JSONArray parseArray = JSON.parseArray(JSON.toJSONString(platformInfoObj));
            if (CollectionUtils.isEmpty(parseArray)) {
                continue;
            }
            String platformCode = parseArray.getJSONObject(0).getString("platform_code");
            if (!LingxingPlatformCodeEnum.TEMU_FBP.getCode().equalsIgnoreCase(platformCode)) {
                // 非TeMu平台
                continue;
            }
            String storeId = (String) orderMongoDatum.getOrDefault("store_id", "");
            Object itemInfoObj = orderMongoDatum.getOrDefault("item_info", "[]");
            JSONArray itemArray = JSONArray.parseArray(JSON.toJSONString(itemInfoObj));
            for (Object itemObj : itemArray) {
                String productNo = JSON.parseObject(JSON.toJSONString(itemObj)).getString("product_no");
                if (StringUtils.isBlank(productNo)) {
                    continue;
                }
                skuIdAndStoreList.add(Pair.of(productNo, storeId));
            }
        }
        if (CollectionUtils.isEmpty(skuIdAndStoreList)) {
            return Collections.emptyList();
        }
        List<String> skuIds = skuIdAndStoreList.stream().map(Pair::getLeft).collect(Collectors.toList());
        List<String> storeIds = skuIdAndStoreList.stream().map(Pair::getRight).collect(Collectors.toList());
        // 查询sku信息
        List<ParamData> skuParamDataList = new ArrayList<>();
        skuParamDataList.add(new ParamData("storeId", "storeId", PannoEnum.IN, storeIds));
        skuParamDataList.add(new ParamData("mskuId", "mskuId", PannoEnum.IN, skuIds));
        List<Map<String, Object>> skuMongoData = mongoService.findMongoData(skuParamDataList, LINGXING_TEMU_LISTING_DATA);
        if (CollectionUtils.isNotEmpty(orderMongoData)) {
            skuIdAndStoreList = skuIdAndStoreList.stream()
                    .filter(e -> skuMongoData.stream()
                            .noneMatch(sku -> sku.get("mskuId").toString().equals(e.getLeft()) && sku.get("storeId").toString().equals(e.getRight())))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(skuIdAndStoreList)) {
            return Collections.emptyList();
        }

        // 查询TEMU未拉取的listing
        String typeId = dmpCfgInputEntity.getTypeId();
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
        String apiType = dmpCfgApiEntity.getApiType();
        // 分页参数
        int length = 1000;
        List<String> querySkuIds = skuIdAndStoreList.stream().map(Pair::getLeft).collect(Collectors.toList());
        TreeMap<String, Object> requestMap = new TreeMap<>();
        requestMap.put("length", length);
        requestMap.put("searchField","7");
        requestMap.put("searchValues",querySkuIds);

        Result<Object> result = LingxingApiUtils.postAndSignCheckListConvert(apiType, requestMap);
        Object data = result.getData();
        if (null == data) {
            return Collections.emptyList();
        }

        JSONObject dataResultMap = JSON.parseObject(JSON.toJSONString(data));
        Object listObj = dataResultMap.get("list");
        Object countObj = dataResultMap.get("count");

        JSONArray resultList = JSONArray.parseArray(JSON.toJSONString(listObj));

        int total = Integer.parseInt(countObj.toString());
        if (length <= total) {
            int totalPageSize = (total + length - 1) / length; // 计算总页数
            for (int i = 1; i < totalPageSize; i++) {
                // 从第二页开始请求
                requestMap.put("offset", i);
                // 当前请求
                Result<Object> curResult = LingxingApiUtils.postRequestDataAndRetry(apiType, requestMap);
                Object curData = curResult.getData();
                if (null == curData) {
                    break;
                }
                JSONObject curDataResultMap = JSON.parseObject(JSON.toJSONString(data));
                Object curListObj = curDataResultMap.get("list");
                JSONArray curJsonArray = JSONArray.parseArray(JSON.toJSONString(curListObj));
                resultList.addAll(curJsonArray);
            }
        }
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(resultList)));
    }
}
