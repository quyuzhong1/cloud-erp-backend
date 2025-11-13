package com.erp.server.oms.sdk.sob2c;

import com.alibaba.fastjson.JSON;
import com.common.business.enums.BusinessTypeEnum;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.server.oms.service.ISoB2cHandleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public abstract class AbstractSoB2cHandle implements ISoB2cHandleService {


    public static final String ORDER_ID_LIST = "orderIdList";

    public static final String NORMAL = "normal";

    /**
     * 转换订单拉取任务
     */
    public DmpInoutDTO.CreateInputDTO createInputDTO(List<SoB2cEntity> shopGroupedList) {
        SoB2cEntity soB2cEntity = shopGroupedList.get(0);
        DmpInoutDTO.CreateInputDTO dto = new DmpInoutDTO.CreateInputDTO();
        dto.setNextLevelId(soB2cEntity.getShopId());
        dto.setSystemCode(soB2cEntity.getDictPlatform());
        dto.setBillType(BusinessTypeEnum.ORDER.getCode());
        //  DmpInputTaskTaskTypeEnum	NORMAL("normal", "正常任务"),
        dto.setTaskType(NORMAL);

        // 构建 orderIdList 并封装为 JSON
        Map<String, List<String>> map = Collections.singletonMap(
                ORDER_ID_LIST,
                shopGroupedList.stream()
                        .map(SoB2cEntity::getPlatformCode)
                        .collect(Collectors.toList())
        );
        dto.setDetailExtendJson(JSON.toJSONString(map));
        return dto;
    }
}
