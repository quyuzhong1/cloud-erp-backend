package com.erp.server.dmp.inout.handler.input.task.init;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.entity.ThirdShopEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.ThirdShopService;
import com.sdk.third.lingxing.dto.Result;
import com.sdk.third.lingxing.utils.LingxingApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器下的api获取数据方式
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputLxCountListApiInitHandler extends DmpInputInitHandler {

    @Resource
    private ThirdShopService thirdShopService;

    /**
     * 公共入库
     */
    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String typeId = dmpCfgInputEntity.getTypeId();
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
        String apiType = dmpCfgApiEntity.getApiType();
        String extendJson = dmpCfgInputDetailEntity.getExtendJson();
        List<String> storeIds = new ArrayList<>();
        if (StringUtils.isNotBlank(extendJson)){
            JSONObject jsonObject = JSON.parseObject(extendJson);
            JSONArray jsonArray = jsonObject.getJSONArray("storeIds");
            if (CollectionUtils.isNotEmpty(jsonArray)){
                storeIds = jsonArray.stream().map(Object::toString).collect(Collectors.toList());;
            }
        }
        // 查下所有绑定店铺
        if (CollectionUtils.isEmpty(storeIds)){
            List<ThirdShopEntity> list = thirdShopService.lambdaQuery()
                    .eq(ThirdShopEntity::getSysType, DmpBasicSystemCodeEnum.LING_XING.getCode())
                    .eq(ThirdShopEntity::getPlatformId, "10027")
                    .eq(ThirdShopEntity::getDisabled, false)
                    .list();
            storeIds = list.stream().map(ThirdShopEntity::getSubPlatformId).collect(Collectors.toList());
        }
        TreeMap<String, Object> requestMap = new TreeMap<>();
        if (CollectionUtils.isNotEmpty(storeIds)){
            requestMap.put("store_ids", storeIds);
        }

        Result<Object> result = LingxingApiUtils.postRequestData(apiType, requestMap);
        Object data = result.getData();
        if (null == data){
            return Collections.emptyList();
        }
        // 分页参数
        int page = 0;
        int length = 1000;

        JSONObject dataResultMap = JSON.parseObject(JSON.toJSONString(data));
        Object listObj = dataResultMap.get("list");
        Object countObj = dataResultMap.get("count");

        JSONArray resultList = JSONArray.parseArray(JSON.toJSONString(listObj));

        int total = Integer.parseInt(countObj.toString());
        if (length <= total){
            int totalPageSize = (total + length - 1) / length; // 计算总页数
            for (int i = 1; i < totalPageSize; i++) {
                // 从第二页开始请求
                requestMap.put("offset", i);
                // 当前请求
                Result<Object> curResult = LingxingApiUtils.postRequestDataAndRetry(apiType, requestMap);
                Object curData = curResult.getData();
                if (null == curData){
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
