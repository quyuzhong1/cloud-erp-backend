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
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
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
import java.util.Collection;
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
        for (Map<String, Object> data : inputMongoEntityList) {
            String status = Objects.toString(data.get(PLATFORM_ORIGINAL_STATUS), "");
            String thirdCode = Objects.toString(data.get("thirdCode"), "");
            Long deliveryTime = parseLong(data.get(DELIVERY_TIME));
            if (!SHIPPED_STATUS_LIST.contains(status) || StringUtils.isAnyBlank(thirdCode) || Objects.isNull(deliveryTime)) {
                continue;
            }
            DmpSoInfoEntity dmpSoInfoEntity = dmpSoInfoService.getOne(new LambdaQueryWrapper<DmpSoInfoEntity>()
                    .eq(DmpSoInfoEntity::getThirdCode, thirdCode)
                    .eq(DmpSoInfoEntity::getSourcePlatform, PlatformDictEnum.SHOPEE.getCode())
                    .eq(DmpSoInfoEntity::getIsDeleted, Boolean.FALSE)
                    .last("limit 1"));
            if (Objects.isNull(dmpSoInfoEntity) || !isPlatformWarehouseOrder(dmpSoInfoEntity)) {
                continue;
            }
            dmpSoInfoEntity.setPlatformOriginalStatus(status);
            dmpSoInfoEntity.setDeliveryStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            dmpSoInfoEntity.setOrderStatus(ApproveStatusEnum.APPROVE.getCode());
            dmpSoInfoEntity.setDeliveryTime(LocalDateTime.ofInstant(Instant.ofEpochSecond(deliveryTime), DEFAULT_ZONE));
            dmpSoInfoEntity.setInputTaskId(inputTaskId);
            dmpSoInfoEntity.setConvertId(convertId);
            dmpSoInfoService.updateById(dmpSoInfoEntity);
            resultList.add(dmpSoInfoEntity);
            changeConvertInputDmpBaseEntityList.add(dmpSoInfoEntity);
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
