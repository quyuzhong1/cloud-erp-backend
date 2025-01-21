package com.erp.server.dmp.inout.handler.input.task.init;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.sdk.third.lingxing.dto.Result;
import com.sdk.third.lingxing.utils.LingxingApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * dmp输入init任务基础处理器下的api获取数据方式
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputLxApiDataWithoutTotalInitHandler extends DmpInputInitHandler {

    /**
     * 公共入库
     */
    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String typeId = dmpCfgInputEntity.getTypeId();
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
        String apiType = dmpCfgApiEntity.getApiType();
        String extendJson = dmpCfgInputDetailEntity.getExtendJson();
        TreeMap<String, Object> requestMap = new TreeMap<>();
        if (StringUtils.isNotBlank(extendJson)){
            requestMap = JSON.parseObject(extendJson, TreeMap.class);
        }

        // 结果
        JSONArray resultList = new JSONArray();
        // 分页参数
        int page = 0;
        requestMap.put("offset", page);
        // 默认:200
        int pageSize = 200;
        // 配置优先
        Object lengthObj = requestMap.get("length");
        if (null != lengthObj){
            pageSize = (Integer) lengthObj;
        } else {
            requestMap.put("length", pageSize);
        }

        // 首次请求
        Result<Object> result = LingxingApiUtils.postRequestDataAndRetry(apiType, requestMap);

        Integer total = result.getTotal();
        JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(result.getData()));
        if (CollectionUtils.isEmpty(jsonArray) || 0 == total) {
            return Collections.emptyList();
        }
        resultList.addAll(jsonArray);

        if (500 <= total){
            int totalPageSize = (total + pageSize - 1) / pageSize; // 计算总页数
            for (int i = 1; i < totalPageSize; i++) {
                // 从第二页开始请求
                requestMap.put("offset", i);
                // 当前请求
                Result<Object> curResult = LingxingApiUtils.postRequestDataAndRetry(apiType, requestMap);
                JSONArray curJsonArray = JSONArray.parseArray(JSON.toJSONString(curResult.getData()));
                resultList.addAll(curJsonArray);
            }
        }
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(resultList)));
    }


}
