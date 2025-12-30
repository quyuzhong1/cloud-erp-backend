package com.erp.server.dmp.inout.handler.input.task.init;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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

import java.util.*;

/**
 * dmp输入init任务基础处理器下的api获取数据方式
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputLxApiInitHandler extends DmpInputInitHandler {

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

        JSONObject dataResultMap = JSON.parseObject(JSON.toJSONString(result));
        // 兼容不同接口返回不同个数
        Object totalObj = dataResultMap.get("total");
        Object countObj = dataResultMap.get("count");
        Integer total = null;
        if (null != totalObj){
            total = Integer.parseInt(totalObj.toString());
        }
        if (null != countObj) {
            total = Integer.parseInt(countObj.toString());
        }

        Object listObj = dataResultMap.get("list");
        JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(listObj));
        if (CollectionUtils.isEmpty(jsonArray) || (null!= total && 0 == total)) {
            return Collections.emptyList();
        }
        resultList.addAll(jsonArray);

        if (null != total && 500 <= total){
            int totalPageSize = (total + pageSize - 1) / pageSize; // 计算总页数
            for (int i = 1; i < totalPageSize; i++) {
                // 从第二页开始请求
                requestMap.put("offset", i);
                // 当前请求
                Result<Object> curResult = LingxingApiUtils.postRequestDataAndRetry(apiType, requestMap);
                Map<String, Object> curDataResultMap = (Map<String, Object>) curResult.getData();
                Object curListObj = curDataResultMap.get("list");
                JSONArray curJsonArray = JSONArray.parseArray(JSON.toJSONString(curListObj));
                resultList.addAll(curJsonArray);
            }
        }
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(resultList)));
    }


}
