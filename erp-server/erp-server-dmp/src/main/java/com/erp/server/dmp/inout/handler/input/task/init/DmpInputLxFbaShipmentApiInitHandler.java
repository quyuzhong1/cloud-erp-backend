package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.ShopInfoMappingEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.ShopInfoMappingService;
import com.sdk.third.lingxing.dto.FbaShipmentReqDTO;
import com.sdk.third.lingxing.dto.Result;
import com.sdk.third.lingxing.utils.LingxingApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: wtr
 * @Date: 2025/12/22 9:10
 * @Param:
 * @Return:
 * @Description:
 **/
@Slf4j
@Service
@Scope("prototype")
public class DmpInputLxFbaShipmentApiInitHandler extends DmpInputInitHandler {

    @Resource
    private ShopInfoMappingService shopInfoMappingService;
    @Resource
    private RedisUtil redisUtil;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<Map<String, Object>> findMongoData = getParentStorageMongoData();
        if (CollectionUtils.isEmpty(findMongoData)) {
            // 主数据不存在明细无需处理
            log.warn("FBA明细下载主任务taskId={},结果为空明细无需处理", dmpInputTaskEntity.getParentTaskId());
            return Collections.emptyList();
        }
        String shopId = findMongoData.get(0).getOrDefault("nextLevelId", "").toString();
        if (StringUtils.isBlank(shopId)){
            ServiceException.runError("未找到mongo中nextLevelId信息:taskId=" + dmpInputTaskEntity.getId());
        }

        // 获取限流间隔时间配置
        String limitSecondStr = "4";
        String extendJson = dmpCfgInputEntity.getExtendJson();
        if (StringUtils.isNotBlank(extendJson)){
            JSONObject parseObject = JSON.parseObject(extendJson);
            if(parseObject != null) {
                String sourceLimitSecond = parseObject.getString("limitSecond");
                if (StringUtils.isNotBlank(sourceLimitSecond)){
                    limitSecondStr = sourceLimitSecond;
                }
            }
        }

        // 校验当前是否限流,领星按接口限流
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, PlatformDictEnum.LING_XING.getCode(), dmpInputTaskEntity.getCfgInputId(), BusinessTypeEnum.FBA_SHIPMENT.getCode());
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj) {
            log.warn("领星FBA货件明细列表,存在限流等待恢复:放弃当前请求任务");
            DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
            initDmpResponse.setDoNextStatus(false);
            return Collections.emptyList();
        }

        // 查询映射关系
        ShopInfoMappingEntity mappingEntity = shopInfoMappingService.getByShopIdAndType(shopId, PlatformEnum.LINGXING.getName());
        if (null == mappingEntity) {
            throw new ServiceException("数据异常:找不到领星映射关系, 店铺id=" + shopId);
        }
        // 领星店铺ID
        String sid = mappingEntity.getThirdPlatformShopId();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        System.out.println("当前日期: " + today.format(formatter));

        // 获取30天前的日期
        LocalDate dateBefore30Days = today.minusDays(30);
        String startDate = dateBefore30Days.format(formatter);

        // 获取30天后的日期
        LocalDate dateAfter30Days = today.plusDays(30);
        String endDate = dateAfter30Days.format(formatter);

        String shipmentId = findMongoData.get(0).getOrDefault("shipmentId", "").toString();
        if (StringUtils.isBlank(shipmentId)) {
            ServiceException.runError("未找到mongo中shipmentId信息:taskId=" + dmpInputTaskEntity.getId());
        }
        // 请求参数
        FbaShipmentReqDTO fbaShipmentReqDTO = new FbaShipmentReqDTO(sid, shipmentId);
        Result<List<Object>> resultData = requestData(fbaShipmentReqDTO, false);
        if (null == resultData) {
            String errorMsg = StrUtil.format("请求领星FBA货件明细列表失败:,sid={}, result={}", sid, JSONUtil.toJsonStr(resultData));
            log.error(errorMsg);
            throw new ServiceException(errorMsg);
        }
        if ("3001008".equalsIgnoreCase(resultData.getCode())) {
            String errorMsg = StrUtil.format("请求领星FBA货件明细触发限流停止当前:,sid={}, result={}", sid, JSONUtil.toJsonStr(resultData));
            log.warn(errorMsg);
            // 设置限流等待时间,
            BigDecimal timeOut = BigDecimal.ONE.max(new BigDecimal(limitSecondStr));
            redisUtil.set(limitKey, dmpInputTaskEntity.getCfgInputId(), timeOut.longValue());
            DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
            initDmpResponse.setDoNextStatus(false);
            return Collections.emptyList();
        }

        Object data = resultData.getData();
        List<JSONObject> allResultList = new ArrayList<>();

        if (data != null) {
            // 无论 data 是 List、JSONArray 还是单个对象，都先转成 JSON 字符串再解析
            String jsonStr = JSON.toJSONString(data);
            // 解析 JSON 字符串
            JSONObject jsonObject = JSON.parseObject(jsonStr);

            // 获取 list 数组
            JSONArray jsonArray = jsonObject.getJSONArray("list");

            // 遍历 JSONArray，将每个元素添加到结果列表中
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                allResultList.add(item);
            }
        }


        if (resultData.getTotal() < 1000) {
            allResultList.forEach(e -> e.put("shopId", shopId));
            return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(allResultList)));
        }
        int totalCount = resultData.getTotal() / 1000;

        for (int offset = 1; offset < totalCount; offset++) {
            Result<List<Object>> curResultData = requestList(sid, startDate, endDate , offset);
            if (null == curResultData) {
                String errorMsg = StrUtil.format("请求领星FBA货件明细列表分页失败:,sid={}, offset={}, result={}", sid, offset, JSONUtil.toJsonStr(resultData));
                log.error(errorMsg);
                throw new ServiceException(errorMsg);
            }
            List<JSONObject> curList = curResultData.getData().stream()
                    .map(e -> (JSONObject) JSONObject.toJSON(e))
                    .collect(Collectors.toList());
            allResultList.addAll(curList);
        }

        allResultList.forEach(e -> e.put("shopId", shopId));
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(allResultList)));
    }

    /**
     * 请求领星接口
     */
    private Result<List<Object>> requestList(String sid, String startDate,String endDate, int offset) {
        FbaShipmentReqDTO currentReceivedDTO = new FbaShipmentReqDTO(sid, startDate, endDate, offset);
        Result<List<Object>> resultData = null;
        long sleepTime = 1000;
        // 一页最多请求10次
        for (int count = 1; count <= 10; count++) {
            resultData = this.requestData(currentReceivedDTO, true);
            if (resultData == null) {
                if (10 == count) {
                    throw new ServiceException("调用领星FBA记录接口重试" + count + "失败");
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    log.error("拉取领星货件明细数据睡眠异常:e={}", ExceptionUtil.stacktraceToString(e));
                    Thread.currentThread().interrupt();
                }
                sleepTime = sleepTime + 1000;
                count = count + 1;
            } else {
                break;
            }
        }
        return resultData;
    }


    /**
     * 请求领星接口
     */
    private Result<List<Object>> requestData(FbaShipmentReqDTO fbaShipmentReqDTO, Boolean returnNullLimit) {
        Map<String, Object> requestObjectMap = BeanUtil.beanToMap(fbaShipmentReqDTO);
        Result<List<Object>> currentResult = LingxingApiUtils.postAndSign(LingxingApiUtils.FBA_SHIPMENT_LIST_RUI, requestObjectMap);
        if (!"0".equalsIgnoreCase(currentResult.getCode()) && !"3001008".equalsIgnoreCase(currentResult.getCode())) {
            String errorMsg = StrUtil.format("请求领星FBA货件明细列表失败:,sid={}, result={}", fbaShipmentReqDTO.getSid(), JSONUtil.toJsonStr(currentResult));
            log.error(errorMsg);
            throw new ServiceException(errorMsg);
        }
        if ("3001008".equalsIgnoreCase(currentResult.getCode()) && returnNullLimit) {
            String errorMsg = StrUtil.format("请求领星FBA货件明细触发限流不执行当前:,sid={}, result={}", fbaShipmentReqDTO.getSid(), JSONUtil.toJsonStr(currentResult));
            log.warn(errorMsg);
            return null;
        }
        return currentResult;
    }

    protected List<Map<String, Object>> getParentStorageMongoData() {
        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
        return mongoService.findMongoData(paramDataList, "amazon_fba_shipment_data");
    }

}
