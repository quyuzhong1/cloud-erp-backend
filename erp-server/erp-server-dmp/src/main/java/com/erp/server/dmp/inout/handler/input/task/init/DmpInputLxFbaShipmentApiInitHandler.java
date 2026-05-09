package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.bean.BeanUtil;
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
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.entity.ShopInfoMappingEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.DmpInputTaskService;
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
import java.time.LocalDateTime;
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
    private DmpInputTaskService dmpInputTaskService;

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
        String retryCountStr = "3";
        String extendJson = dmpCfgInputEntity.getExtendJson();
        if (StringUtils.isNotBlank(extendJson)){
            JSONObject parseObject = JSON.parseObject(extendJson);
            if(parseObject != null) {
                String sourceLimitSecond = parseObject.getString("limitSecond");
                String retryCount = parseObject.getString("retryCount");
                if (StringUtils.isNotBlank(sourceLimitSecond)){
                    limitSecondStr = sourceLimitSecond;
                }

                if (StringUtils.isNotBlank(retryCount)){
                    retryCountStr = retryCount;
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

        // 获取10年前的日期
        LocalDate dateBefore10Years = today.minusYears(10);
        String startDate = dateBefore10Years.format(formatter);

        // 获取10年后的日期
        LocalDate dateAfter10Years = today.plusYears(10);
        String endDate = dateAfter10Years.format(formatter);

        Result<Object> resultData = null;
        List<JSONObject> allResultList = new ArrayList<>();
        boolean allShipmentFetched = true;
        for (Map<String, Object> mongoData : findMongoData) {
            String shipmentId = mongoData.getOrDefault("shipmentId", "").toString();
            if (StringUtils.isBlank(shipmentId)) {
                allShipmentFetched = false;
                resultData = null;
                allResultList.clear();
                break;
            }
            // 请求参数
            FbaShipmentReqDTO fbaShipmentReqDTO = new FbaShipmentReqDTO(sid, startDate, endDate, shipmentId);
            resultData = requestData(fbaShipmentReqDTO, false);
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

            List<JSONObject> shipmentList = extractShipmentList(resultData.getData());
            if (CollectionUtils.isEmpty(shipmentList)) {
                allShipmentFetched = false;
                allResultList.clear();
                break;
            }
            allResultList.addAll(shipmentList);
        }

        if (!allShipmentFetched || isEmptyShipmentData(resultData)) {
            int retryLimit = parseRetryLimit(retryCountStr);
            String retryTaskId = dmpInputTaskEntity.getParentTaskId();
            DmpInputTaskEntity retryTaskEntity = dmpInputTaskService.getById(retryTaskId);
            Integer oldErrorCount = retryTaskEntity == null ? 0 : retryTaskEntity.getErrorCount();
            int currentErrorCount = (oldErrorCount == null ? 0 : oldErrorCount) + 1;
            if (currentErrorCount >= retryLimit) {
                dmpInputTaskService.lambdaUpdate()
                        .set(DmpInputTaskEntity::getErrorCount, currentErrorCount)
                        .eq(DmpInputTaskEntity::getId, retryTaskId)
                        .update();
                log.warn("请求领星FBA货件为空且重试次数已达上限:taskId={},sid={},errorCount={},retryLimit={}", retryTaskId, sid, currentErrorCount, retryLimit);
            } else {
                DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                initDmpResponse.setDoNextStatus(false);
                dmpInputTaskService.lambdaUpdate()
                        .set(DmpInputTaskEntity::getNextExecTime, LocalDateTime.now().plusMinutes(20))
                        .set(DmpInputTaskEntity::getErrorCount, currentErrorCount)
                        .eq(DmpInputTaskEntity::getId, retryTaskId)
                        .update();
                String errorMsg = StrUtil.format("请求领星FBA货件失败:,sid={}, result={}, errorCount={}", sid, JSONUtil.toJsonStr(resultData), currentErrorCount);
                log.warn(errorMsg);
                return Collections.emptyList();
            }
        }
        allResultList.forEach(e -> e.put("shopId", shopId));
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(allResultList)));
    }

    /**
     * 请求领星接口
     */
    private Result<Object> requestData(FbaShipmentReqDTO fbaShipmentReqDTO, Boolean returnNullLimit) {
        Map<String, Object> requestObjectMap = BeanUtil.beanToMap(fbaShipmentReqDTO);
        Result<Object> currentResult = LingxingApiUtils.postAndSign(LingxingApiUtils.FBA_SHIPMENT_LIST_RUI, requestObjectMap);
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

    private boolean isEmptyShipmentData(Result<Object> resultData) {
        if (resultData == null || resultData.getData() == null) {
            return true;
        }
        return extractShipmentList(resultData.getData()).isEmpty();
    }

    private List<JSONObject> extractShipmentList(Object data) {
        if (data == null) {
            return Collections.emptyList();
        }
        if (data instanceof List) {
            return ((List<?>) data).stream()
                    .map(e -> (JSONObject) JSONObject.toJSON(e))
                    .collect(Collectors.toList());
        }
        JSONObject dataObject = JSON.parseObject(JSON.toJSONString(data));
        if (dataObject == null) {
            return Collections.emptyList();
        }
        JSONArray jsonArray = dataObject.getJSONArray("list");
        if (jsonArray == null || jsonArray.isEmpty()) {
            return Collections.emptyList();
        }
        List<JSONObject> resultList = new ArrayList<>(jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            resultList.add((JSONObject) JSONObject.toJSON(jsonArray.get(i)));
        }
        return resultList;
    }

    protected List<Map<String, Object>> getParentStorageMongoData() {
        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
        return mongoService.findMongoData(paramDataList, "amazon_fba_shipment_data");
    }

    private int parseRetryLimit(String retryCountStr) {
        if (StringUtils.isBlank(retryCountStr)) {
            return 3;
        }
        try {
            return Math.max(Integer.parseInt(retryCountStr.trim()), 1);
        } catch (Exception e) {
            log.warn("retryCount配置非法,使用默认重试次数3,retryCount={}", retryCountStr);
            return 3;
        }
    }

}
