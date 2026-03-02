package com.erp.server.dmp.inout.handler.input.task.init;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.erp.model.dmp.constant.DmpInputConstant;
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

import java.time.ZoneId;
import java.util.*;

/**
 * dmp输入init任务基础处理器下的api获取数据方式
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputLxOrderApiInitHandler extends DmpInputInitHandler {

    /**
     * 订单列表
     */
    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String typeId = dmpCfgInputEntity.getTypeId();
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
        String apiType = dmpCfgApiEntity.getApiType();

        // 请求参数
        TreeMap<String, Object> requestMap = new TreeMap<>();
        // 主表扩展参数
        String extendJson = dmpCfgInputEntity.getExtendJson();
        if (StringUtils.isNotBlank(extendJson)){
            TreeMap<String, Object> mainTreeMap = JSON.parseObject(extendJson, TreeMap.class);
            requestMap.putAll(mainTreeMap);
        }

        // 明细扩展参数
        String detailExtendJson = dmpInputTaskEntity.getExtendJson();
        if (StringUtils.isNotBlank(detailExtendJson)){
            TreeMap<String, Object> detailTreeMap = JSON.parseObject(detailExtendJson, TreeMap.class);
            requestMap.putAll(detailTreeMap);
        }

        Object orderIdListObject = requestMap.get(DmpInputConstant.ORDER_ID_LIST);
        if(orderIdListObject == null) {
        	// 时间
            // start_time 开始时间，时间戳格式【单位：秒】，双开区间	是	[int]	1710925191
            long startEpochSecond = dmpInputTaskEntity.getStartTime().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
            requestMap.put("start_time", startEpochSecond);
            // end_time 结束时间，时间戳格式【单位：秒】，双开区间	是	[int]	1713430791
            long endEpochSecond = dmpInputTaskEntity.getEndTime().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
            requestMap.put("end_time", endEpochSecond);
        }
        
        // 结果
        JSONArray resultList = new JSONArray();
        // 分页参数
        int page = 0;
        int pageSize = 500;
        requestMap.put("offset", page);
        requestMap.put("length", pageSize);
        // 首次请求
        Result<Object> result = LingxingApiUtils.postRequestDataAndRetry(apiType, requestMap);
        Map<String, Object> dataResultMap = (Map<String, Object>) result.getData();
        Object totalObj = dataResultMap.get("total");
        int total =  Integer.parseInt(totalObj.toString());
        Object listObj = dataResultMap.get("list");
        String data = JSON.toJSONString(listObj);
//        data = data.replace("103532145800142442","303532145800142442");
//        data = data.replace("PO-211-18380728085032203","PO-211-13090935383591954");
//        data = data.replace("110522176760639488","110521466365329499");
        JSONArray jsonArray = JSONArray.parseArray(data);
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
                Map<String, Object> curDataResultMap = (Map<String, Object>) curResult.getData();
                Object curListObj = curDataResultMap.get("list");
                JSONArray curJsonArray = JSONArray.parseArray(JSON.toJSONString(curListObj));
                resultList.addAll(curJsonArray);
            }
        }

        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(resultList)));
    }
}
