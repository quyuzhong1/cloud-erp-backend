package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputDmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.service.DmpSoInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 虾皮订单状态 webhook 更新已存在的平台仓 DMP 订单。
 */
@Service
@Scope("prototype")
public class DmpInputShopeeWebhookOrderDmpHandler extends DmpInputDbConvertDmpHandler {

    private static final List<String> SHIPPED_STATUS_LIST = Arrays.asList("SHIPPED", "TO_CONFIRM_RECEIVE", "COMPLETED");
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    private static final String PLATFORM_ORIGINAL_STATUS = "platformOriginalStatus";
    private static final String DELIVERY_TIME = "deliveryTime";

    @Autowired
    private DmpSoInfoService dmpSoInfoService;

    @Override
    protected List<BaseEntity> convertToDmp(List<Map<String, Object>> inputMongoEntityList) {
        List<BaseEntity> resultList = new ArrayList<>();
        if (CollUtil.isEmpty(inputMongoEntityList)) {
            return resultList;
        }
        for (Map<String, Object> data : inputMongoEntityList) {
            DmpSoInfoEntity dmpSoInfoEntity = dmpSoInfoService.getById(Objects.toString(data.get(BaseEntity.FIELD_ID), ""));
            if (Objects.isNull(dmpSoInfoEntity)) {
                continue;
            }
            dmpSoInfoEntity.setPlatformOriginalStatus(Objects.toString(data.get(PLATFORM_ORIGINAL_STATUS), ""));
            dmpSoInfoEntity.setDeliveryStatus(Objects.toString(data.get("deliveryStatus"), ""));
            dmpSoInfoEntity.setOrderStatus(Objects.toString(data.get("orderStatus"), ""));
            dmpSoInfoEntity.setDeliveryTime((LocalDateTime) data.get(DELIVERY_TIME));
            dmpSoInfoEntity.setInputTaskId(inputTaskId);
            dmpSoInfoEntity.setConvertId(convertId);
            dmpSoInfoService.updateById(dmpSoInfoEntity);
            resultList.add(dmpSoInfoEntity);
            changeConvertInputDmpBaseEntityList.add(dmpSoInfoEntity);
        }
        return resultList;
    }

    @Override
    public List<Map<String, Object>> convertInitToDmp(DmpInputDmpRequest dmpRequest, DmpInputInitResponse dmpResponse) {
        List<Map<String, Object>> dataList = super.convertInitToDmp(dmpRequest, dmpResponse);
        if (CollUtil.isEmpty(dataList)) {
            return dataList;
        }
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Map<String, Object> data : dataList) {
            String status = Objects.toString(data.get(PLATFORM_ORIGINAL_STATUS), "");
            String thirdCode = Objects.toString(data.get("thirdCode"), "");
            String nextLevelId = Objects.toString(data.get("nextLevelId"), "");
            Long deliveryTime = parseLong(data.get(DELIVERY_TIME));
            if (!SHIPPED_STATUS_LIST.contains(status) || StringUtils.isAnyBlank(thirdCode, nextLevelId) || Objects.isNull(deliveryTime)) {
                continue;
            }
            DmpSoInfoEntity dmpSoInfoEntity = dmpSoInfoService.getOne(new LambdaQueryWrapper<DmpSoInfoEntity>()
                    .eq(DmpSoInfoEntity::getThirdCode, thirdCode)
                    .eq(DmpSoInfoEntity::getSourcePlatform, PlatformDictEnum.SHOPEE.getCode())
                    .eq(DmpSoInfoEntity::getNextLevelId, nextLevelId)
                    .eq(DmpSoInfoEntity::getIsDeleted, Boolean.FALSE)
                    .last("limit 1"));
            if (Objects.isNull(dmpSoInfoEntity) || !isPlatformWarehouseOrder(dmpSoInfoEntity)) {
                continue;
            }
            data.put(BaseEntity.FIELD_ID, dmpSoInfoEntity.getId());
            data.put("thirdCode", dmpSoInfoEntity.getThirdCode());
            data.put("platformCode", dmpSoInfoEntity.getPlatformCode());
            data.put(PLATFORM_ORIGINAL_STATUS, status);
            data.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            data.put("orderStatus", ApproveStatusEnum.APPROVE.getCode());
            data.put(DELIVERY_TIME, LocalDateTime.ofInstant(Instant.ofEpochSecond(deliveryTime), DEFAULT_ZONE));
            data.put("sourcePlatform", dmpSoInfoEntity.getSourcePlatform());
            data.put("sourceSystem", dmpSoInfoEntity.getSourceSystem());
            data.put("nextLevelId", dmpSoInfoEntity.getNextLevelId());
            resultList.add(data);
        }
        return resultList;
    }

    private boolean isPlatformWarehouseOrder(DmpSoInfoEntity dmpSoInfoEntity) {
        String extendData = dmpSoInfoEntity.getExtendData();
        return StringUtils.isNotBlank(extendData)
                && Boolean.TRUE.equals(JSONUtil.parseObj(extendData).getBool("isPlatformWarehouseOrder"));
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
