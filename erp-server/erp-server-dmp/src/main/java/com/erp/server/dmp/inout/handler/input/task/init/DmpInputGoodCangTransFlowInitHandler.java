package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.core.exception.ServiceException;
import com.sdk.wms.goodcang.dto.request.GoodCangInventoryRequestDTO;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.goodcang.utils.GoodCangUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public abstract class DmpInputGoodCangTransFlowInitHandler extends DmpInputInitHandler {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    protected List<JSONObject> requestFLowByNoList(List<String> noList, int batchSize, GoodCangInventoryRequestDTO requestDTO, String apiType, String authId) {
        // 退货单号列表使用stream按200个分组
        List<List<String>> partitionedList = IntStream.range(0, (noList.size() + batchSize - 1) / batchSize)
                .mapToObj(i -> noList.subList(i * batchSize, Math.min((i + 1) * batchSize, noList.size())))
                .collect(Collectors.toList());

        List<JSONObject> allResult = new ArrayList<>();
        for (List<String> partReturnOrderIds : partitionedList) {
            requestDTO.setReference_no_list(partReturnOrderIds);
            requestDTO.setPage(1);
            log.warn("请求谷仓库存流水请求:{}", JSON.toJSONString(requestDTO));
            String response = GoodCangUtils.sendPost(apiType, JSON.toJSONString(requestDTO));
            log.warn("请求谷仓库存流水响应:{}", CharSequenceUtil.sub(response, 0, 1000));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // 空数据处理
            // {"ask":"Failure","message":"没有数据(ERROR ID 99-UVU8HX)","Error":{"errCode":"400","errMessage":"没有数据(ERROR ID 99-UVU8HX)"}}
            JSONObject jsonObject = JSONObject.parseObject(response);
            JSONObject errorObj = jsonObject.getJSONObject("Error");
            if (null != errorObj) {
                String errCode = errorObj.getString("errCode");
                String errMessage = errorObj.getString("errMessage");
                if ("400" .equalsIgnoreCase(errCode) && errMessage.contains("没有数据")) {
                    continue;
                }
                ServiceException.runError("谷仓接口返回异常:" + response);
            }

            GoodCangResponse<List<JSONObject>> result = JSONObject.parseObject(response, new TypeReference<GoodCangResponse<List<Object>>>() {
            }.getType());
            List<?> data = result.getData();
            int size = data.size();
            if (size == 0) {
                continue;
            }
            List<JSONObject> jsonObjList = data.stream().map(e -> {
                        JSONObject jsonItemObj = (JSONObject) JSON.toJSON(e);
                        jsonItemObj.put("authId", authId);
                        return jsonItemObj;
                    }
            ).collect(Collectors.toList());
            allResult.addAll(jsonObjList);
        }
        return allResult;
    }

    /**
     * 时间区间按月分组
     * @param createDateFrom 开始时间
     * @param createDateEnd  结束时间
     * @return 间隔1个月的时间区间
     */
    protected List<Pair<LocalDateTime, LocalDateTime>> splitDateRangeByMonth(LocalDateTime createDateFrom, LocalDateTime createDateEnd) {
        // 按每个月最后一天分组
        List<Pair<LocalDateTime, LocalDateTime>> dateRanges = new ArrayList<>();
        LocalDateTime current = createDateFrom;
        while (!current.isAfter(createDateEnd)) {
            LocalDateTime start = current; // 当前月的第一天
            LocalDateTime end = current.withDayOfMonth(current.toLocalDate().lengthOfMonth()) // 当前月的最后一天
                    .withHour(23).withMinute(59).withSecond(59); // 设置为当天结束时间
            if (end.isAfter(createDateEnd)) {
                end = createDateEnd; // 如果最后一天超过结束时间，则设置为结束时间
            }
            dateRanges.add(Pair.of(start, end));
            current = current.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).plusMonths(1); // 移动到下一个月
        }
        return dateRanges;
    }

}
