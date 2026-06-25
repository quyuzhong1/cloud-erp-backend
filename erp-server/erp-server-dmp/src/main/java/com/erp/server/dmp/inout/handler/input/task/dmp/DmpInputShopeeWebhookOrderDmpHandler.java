package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputDmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.service.DmpSoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 虾皮订单状态 webhook 更新已存在的平台仓 DMP 订单。
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputShopeeWebhookOrderDmpHandler extends DmpInputDbConvertDmpHandler {

    private static final List<String> SHIPPED_STATUS_LIST = Arrays.asList("SHIPPED", "TO_CONFIRM_RECEIVE", "COMPLETED");
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    private static final String PLATFORM_ORIGINAL_STATUS = "platformOriginalStatus";
    private static final String DELIVERY_TIME = "deliveryTime";
    private static final int BATCH_UPDATE_SIZE = 500;

    @Autowired
    private DmpSoInfoService dmpSoInfoService;

    @Override
    public List<Map<String, Object>> convertMongoToDmp(DmpInputDmpRequest dmpRequest, DmpInputMongoResponse dmpResponse) {
        List<Map<String, Object>> mongoRows = new ArrayList<>();
        Collection<List<Map<String, Object>>> values = dmpResponse.getConvertInputMongoEntityListMaps().values();
        for (List<Map<String, Object>> value : values) {
            if (CollUtil.isNotEmpty(value)) {
                mongoRows.addAll(value);
            }
        }
        if (CollUtil.isNotEmpty(mongoRows)) {
            return mongoRows;
        }
        return super.convertInitToDmp(dmpRequest, dmpResponse);
    }

    @Override
    protected List<BaseEntity> convertToDmp(List<Map<String, Object>> inputMongoEntityList) {
        List<BaseEntity> resultList = new ArrayList<>();
        if (CollUtil.isEmpty(inputMongoEntityList)) {
            return resultList;
        }
        Set<String> thirdCodes = inputMongoEntityList.stream()
                .filter(data -> SHIPPED_STATUS_LIST.contains(Objects.toString(data.get(PLATFORM_ORIGINAL_STATUS), "")))
                .filter(data -> Objects.nonNull(parseLong(data.get(DELIVERY_TIME))))
                .map(data -> Objects.toString(data.get("thirdCode"), ""))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(thirdCodes)) {
            return resultList;
        }
        Map<String, DmpSoInfoEntity> dmpSoInfoMap = dmpSoInfoService.list(new LambdaQueryWrapper<DmpSoInfoEntity>()
                        .in(DmpSoInfoEntity::getThirdCode, thirdCodes)
                        .eq(DmpSoInfoEntity::getSourcePlatform, PlatformDictEnum.SHOPEE.getCode())
                        .eq(DmpSoInfoEntity::getIsDeleted, Boolean.FALSE))
                .stream()
                .collect(Collectors.toMap(DmpSoInfoEntity::getThirdCode, Function.identity(), (left, right) -> left));
        Map<String, DmpSoInfoEntity> updateMap = new LinkedHashMap<>();
        for (Map<String, Object> data : inputMongoEntityList) {
            String status = Objects.toString(data.get(PLATFORM_ORIGINAL_STATUS), "");
            String thirdCode = Objects.toString(data.get("thirdCode"), "");
            Long deliveryTime = parseLong(data.get(DELIVERY_TIME));
            // Shopee webhook 上游只下发发货状态；update_time 在该链路中按发货时间落库。
            if (!SHIPPED_STATUS_LIST.contains(status) || StringUtils.isAnyBlank(thirdCode) || Objects.isNull(deliveryTime)) {
                continue;
            }
            DmpSoInfoEntity dmpSoInfoEntity = dmpSoInfoMap.get(thirdCode);
            if (Objects.isNull(dmpSoInfoEntity) || !isPlatformWarehouseOrder(dmpSoInfoEntity)) {
                continue;
            }
            dmpSoInfoEntity.setPlatformOriginalStatus(status);
            dmpSoInfoEntity.setDeliveryStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            // Shopee平台仓发货回告只处理已发货订单，DMP侧需推进到已审核态，后续OMS发货同步依赖该状态。
            dmpSoInfoEntity.setOrderStatus(ApproveStatusEnum.APPROVE.getCode());
            dmpSoInfoEntity.setDeliveryTime(LocalDateTime.ofInstant(Instant.ofEpochSecond(deliveryTime), DEFAULT_ZONE));
            dmpSoInfoEntity.setInputTaskId(inputTaskId);
            dmpSoInfoEntity.setConvertId(convertId);
            updateMap.put(dmpSoInfoEntity.getId(), dmpSoInfoEntity);
        }
        if (CollUtil.isEmpty(updateMap)) {
            return resultList;
        }
        List<DmpSoInfoEntity> updateList = new ArrayList<>(updateMap.values());
        if (!dmpSoInfoService.updateBatchById(updateList, BATCH_UPDATE_SIZE)) {
            throw new ServiceException("Webhook订单批量更新失败,count:" + updateList.size());
        }
        resultList.addAll(updateList);
        changeConvertInputDmpBaseEntityList.addAll(updateList);
        return resultList;
    }

    private boolean isPlatformWarehouseOrder(DmpSoInfoEntity dmpSoInfoEntity) {
        String extendData = dmpSoInfoEntity.getExtendData();
        if (StringUtils.isBlank(extendData)) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(JSONUtil.parseObj(extendData).getBool("isPlatformWarehouseOrder"));
        } catch (Exception e) {
            log.warn("Webhook订单平台仓标识解析失败,thirdCode:{},id:{},extendData:{}",
                    dmpSoInfoEntity.getThirdCode(), dmpSoInfoEntity.getId(), extendData, e);
            return false;
        }
    }

    private Long parseLong(Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
